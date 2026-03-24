package com.ekart.payment.service;

import com.ekart.payment.client.InventoryClient;
import com.ekart.payment.client.OrderClient;
import com.ekart.payment.entity.Payment;
import com.ekart.payment.enums.PaymentStatus;
import com.ekart.payment.repository.PaymentRepository;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class RazorpayWebhookService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private InventoryClient inventoryClient;

    @Value("${razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    public void processWebhook(String signature, String payload) {
        verifySignature(signature,payload);
    }

    private void verifySignature(String signature, String payload) {
        try{
            Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);

            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");
            log.info("Received Razorpay Webhook Event: " + eventType);

            switch (eventType) {
                case "payment.captured" -> handlePaymentSuccess(event);
                case "payment.failed" -> handlePaymentFailure(event);
                default -> throw new RazorpayException("Invalid Razorpay Webhook Event");
            }
        } catch (RazorpayException e) {
            log.error("Razorpay Webhook Signature Verification Failed");
            throw new SecurityException("Invalid razorpay webhook signature");
        }
    }

    private JSONObject extractPaymentEntity(JSONObject event) {
        return event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");
    }

    @Transactional
    private void handlePaymentSuccess(JSONObject event) {
        JSONObject paymentEntity = extractPaymentEntity(event);
        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id ");
        Payment payment = paymentRepository.findByTransactionId(razorpayOrderId).orElseThrow(() -> new IllegalStateException("Razorpay Order Not Found"));
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayOrderId(razorpayPaymentId);
        orderClient.confirmOrder(payment.getOrderId());
        inventoryClient.commitInventory(payment.getOrderId());
        paymentRepository.save(payment);
    }

    @Transactional
    private void handlePaymentFailure(JSONObject event) {
        JSONObject paymentEntity = extractPaymentEntity(event);
        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id ");
        Payment payment = paymentRepository.findByTransactionId(razorpayOrderId).orElseThrow(() -> new IllegalStateException("Razorpay Order Not Found"));
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setRazorpayOrderId(razorpayPaymentId);
        orderClient.failOrder(payment.getOrderId());
        inventoryClient.rollbackInventory(payment.getOrderId());
    }

}

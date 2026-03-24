package com.ekart.payment.service;


import com.ekart.payment.client.OrderClient;
import com.ekart.payment.dto.*;
import com.ekart.payment.entity.Payment;
import com.ekart.payment.enums.PaymentMethod;
import com.ekart.payment.enums.PaymentStatus;
import com.ekart.payment.exception.PaymentNotFoundException;
import com.ekart.payment.repository.PaymentRepository;
import com.ekart.payment.utils.TransactionIdGenerator;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RazorpayPaymentGateway razorpayPaymentGateway;

    @Value("${razorpay.api.secret}")
    private String KEY_SECRET;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private OrderUpdateService orderUpdateService;


    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {

        Optional<Payment> existingPayment = paymentRepository.findByOrderId(paymentRequest.getOrderId());
        Payment payment;
        if (existingPayment.isPresent()) {
            payment = existingPayment.get();
            return mapToResponse(payment);
        }else{
            // 1️⃣ Save payment first
            payment =  Payment.builder().orderId(paymentRequest.getOrderId())
                    .amount(paymentRequest.getAmount())
                    .paymentMethod(PaymentMethod.PENDING)
                    .paymentStatus(PaymentStatus.PENDING)
                    .transactionId(TransactionIdGenerator.generate())
                    .build();
            Payment savedPayment = paymentRepository.save(payment);

            // 2️⃣ Create Razorpay order
            try {
                // Assuming paymentRequest.getAmount() is 500.00 (Rupees)
                BigDecimal amountInRupees = paymentRequest.getAmount();

                // Use longValue() to ensure no decimals are sent to Razorpay
                long amountInPaise = amountInRupees.multiply(new BigDecimal(100)).longValue();

                GatewayOrderResponse razorpayOrder = razorpayPaymentGateway.createOrder(savedPayment.getOrderId(), amountInPaise);
                savedPayment.setRazorpayOrderId(razorpayOrder.getRazorpayOrderId());

                // save payment method is pending
                paymentRepository.save(savedPayment);
                return mapToResponse(savedPayment);
            } catch (RazorpayException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new PaymentNotFoundException("Payment details not found for Order id: "+orderId));
        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentUpdateStatus request) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment details not found for Payment id: "+paymentId));
        payment.setPaymentStatus(request.getPaymentStatus());
        paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    @Override
    public Map<String,BigDecimal> findTotalAmountByOrderId(String orderId) {
        log.info("Calling order client with Order Id: {}", orderId);
       return  orderClient.findTotalAmountByOrderId(orderId);
    }

    // Mapper Method
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setGatewayPaymentId(payment.getRazorpayOrderId());
        paymentResponse.setOrderId(payment.getOrderId());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setPaymentStatus(payment.getPaymentStatus());
        paymentResponse.setTransactionId(payment.getTransactionId());
        return paymentResponse;
    }


    @Transactional
    public PaymentResponse razorpayCallBack(RazorpayRequest request) throws RazorpayException {
        try {
            // Verify the payment signature here
            String signature = request.getRazorpay_order_id() + "|" + request.getRazorpay_payment_id();
            boolean isValid = Utils.verifySignature(signature, request.getRazorpay_signature(), KEY_SECRET);
            log.info("Razorpay Signature: {}", isValid);

//            razorpayOrderId is gatewayOrderId
            Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpay_order_id()).orElseThrow(() -> new PaymentNotFoundException("Payment details not found for Payment id: "+request.getRazorpay_order_id()));
            log.info("Payment details found for Payment id: {}", payment.getRazorpayOrderId());

            if (!isValid) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                orderClient.failOrder(payment.getOrderId());
                return mapToResponse(payment);
            }

            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                log.info("Payment already processed: {}", payment.getRazorpayOrderId());
                return mapToResponse(payment);
            }

            // 2. Fetch Payment details using the ID
            com.razorpay.Payment paymentDetails = razorpayClient.payments.fetch(request.getRazorpay_payment_id());
            String razorpayStatus = paymentDetails.get("status"); // "captured", "authorized", "failed"

            Object amountObj = paymentDetails.get("amount"); // Amount in paise
            long amountPaid = ((Number) amountObj).longValue();
            long expectedAmount = payment.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            // SECURITY CHECK: Does the amount match your DB? (DB amount * 100 for paise)
            if (amountPaid != expectedAmount) {
                log.error("Amount mismatch! Expected: {}, Got: {}", expectedAmount, amountPaid);
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                return mapToResponse(payment);
            }

            log.info("Razorpay Status: {}", razorpayStatus);
            PaymentStatus finalStatus;

            switch (razorpayStatus.toLowerCase().trim()) {
                case "captured", "authorized" -> finalStatus = PaymentStatus.SUCCESS; // Treat both as success

//                    case "captured" -> finalStatus = PaymentStatus.SUCCESS;
//                    case "authorized" -> finalStatus = PaymentStatus.PENDING;
                default -> finalStatus = PaymentStatus.FAILED;
            }


            log.info("The Switch chose finalStatus: {}", finalStatus);
            payment.setPaymentStatus(finalStatus);


            // 3. Extract the method (e.g., "card", "netbanking", "wallet", "upi")
            String method = paymentDetails.get("method");

            if (method != null) {
                PaymentMethod mappedMethod = switch (method.toLowerCase()) {
                    case "card"       -> PaymentMethod.CARD;
                    case "netbanking" -> PaymentMethod.NET_BANKING;
                    case "upi"        -> PaymentMethod.UPI;
                    case "wallet"     -> PaymentMethod.WALLET;
                    case "paylater"   -> PaymentMethod.PAY_LATER;
                    case "emi"        -> PaymentMethod.EMI;
                    default           -> PaymentMethod.OTHER; // Always have a fallback!
                };
                payment.setPaymentMethod(mappedMethod);
            }
            log.info("Payment method: {}", payment.getPaymentMethod());

            // Payment successful
//                payment.setPaymentStatus(PaymentStatus.SUCCESS);

            // Save and force immediate write to DB
            Payment saved = paymentRepository.saveAndFlush(payment);
            log.info("Payment status after saving into db: {}", saved.getPaymentStatus());

            // Only confirm the order if it's actually successful
            // We trigger this and immediately return the response
            if (finalStatus == PaymentStatus.SUCCESS) {
                orderUpdateService.confirmOrderAsync(saved.getOrderId());
            } else {
                orderUpdateService.failOrderAsync(saved.getOrderId());
            }

            return mapToResponse(saved);
        } catch (RazorpayException e) {
            System.err.println("Razorpay Exception during callback: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("General Exception during callback: " + e.getMessage());
            throw new RazorpayException("General exception during callback");
        }
    }
}

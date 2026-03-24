package com.ekart.payment.service;

import com.ekart.payment.dto.GatewayOrderResponse;
import com.ekart.payment.service.PaymentGateway;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentGateway implements PaymentGateway {

    private final RazorpayClient razorpayClient;


    @Override
    public GatewayOrderResponse createOrder(String orderId, long amount) throws RazorpayException {

        try{
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderId);
            orderRequest.put("payment_capture", 1);

            Order orderResponse = razorpayClient.orders.create(orderRequest);



            return new GatewayOrderResponse(orderResponse.get("id"), orderResponse.get("currency"), amount, orderResponse.get("status"), "RAZORPAY");
        }catch (RazorpayException e){
            throw new RuntimeException("Error creating Razorpay order", e);
        }
    }
}

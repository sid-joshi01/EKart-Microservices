package com.ekart.payment.service;

import com.ekart.payment.dto.GatewayOrderResponse;
import com.razorpay.RazorpayException;

import java.math.BigDecimal;

public interface PaymentGateway {
    GatewayOrderResponse createOrder(String orderId, long amount) throws RazorpayException;
}

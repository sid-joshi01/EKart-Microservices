package com.ekart.payment.dto;

import com.ekart.payment.enums.PaymentStatus;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private String gatewayPaymentId;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String transactionId;
}

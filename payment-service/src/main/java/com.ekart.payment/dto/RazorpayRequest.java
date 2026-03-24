package com.ekart.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayRequest {
    private String razorpay_payment_id;
    private String razorpay_order_id;
    private String razorpay_signature;
}

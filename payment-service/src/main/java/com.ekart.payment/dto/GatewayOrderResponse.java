package com.ekart.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayOrderResponse {
    private String razorpayOrderId;
    private String currency;
    private long amount;
    private String status;
    private String gatewayName;
}

package com.ekart.payment.dto;

import com.ekart.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentUpdateStatus {

    @NotNull
    private PaymentStatus paymentStatus;
}

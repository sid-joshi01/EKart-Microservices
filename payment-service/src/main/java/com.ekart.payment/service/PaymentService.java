package com.ekart.payment.service;

import com.ekart.payment.dto.PaymentRequest;
import com.ekart.payment.dto.PaymentResponse;
import com.ekart.payment.dto.PaymentUpdateStatus;
import com.ekart.payment.entity.Payment;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Map;


public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest payment);

    PaymentResponse getPaymentByOrderId(String orderId);
    PaymentResponse updatePaymentStatus(Long paymentId, PaymentUpdateStatus request);

    Map<String,BigDecimal> findTotalAmountByOrderId(String orderId);




}

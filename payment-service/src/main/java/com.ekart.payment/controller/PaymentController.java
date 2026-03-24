package com.ekart.payment.controller;

import com.ekart.payment.dto.PaymentRequest;
import com.ekart.payment.dto.PaymentResponse;
import com.ekart.payment.dto.PaymentUpdateStatus;
import com.ekart.payment.dto.RazorpayRequest;
import com.ekart.payment.entity.Payment;
import com.ekart.payment.enums.PaymentStatus;
import com.ekart.payment.service.PaymentService;
import com.ekart.payment.service.PaymentServiceImpl;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
//@CrossOrigin(origins = "http://localhost:8080") // To allow your HTML to call the API
public class PaymentController {

    @Autowired
    private PaymentServiceImpl paymentService;



    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(paymentRequest));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String orderId){
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.getPaymentByOrderId(orderId));
    }

    @PutMapping("/status/{paymentId}")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long paymentId, @Valid @RequestBody PaymentUpdateStatus paymentUpdateStatus){
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.updatePaymentStatus(paymentId,paymentUpdateStatus));
    }

    @PostMapping("/payment-callback")
    public PaymentResponse paymentCallback(@RequestBody RazorpayRequest razorpayRequest) throws RazorpayException {
       return paymentService.razorpayCallBack(razorpayRequest);
    }

    @GetMapping("/amount")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,BigDecimal> fetchAmount(@RequestParam(required = true) String orderId){
        return paymentService.findTotalAmountByOrderId(orderId);
    }
}

// call pay now button page
// http://localhost:8086/index.html?orderId=51676fda-4279-4317-b352-a9699319e788
// http://localhost:8080/index.html?orderId=1ff0d9d3-c6e7-48db-9521-c74365ee474f
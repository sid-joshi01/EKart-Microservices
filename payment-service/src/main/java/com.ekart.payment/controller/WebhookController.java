package com.ekart.payment.controller;

import com.ekart.payment.service.RazorpayWebhookService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/razorpay")
public class WebhookController {

    @Autowired
    private RazorpayWebhookService razorpayWebhookService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhookRazorpay(@RequestHeader("X-Razorpay-Signature") String signature, @RequestBody String payload) {
        razorpayWebhookService.processWebhook(signature,payload);
        return ResponseEntity.ok().build();
    }
}

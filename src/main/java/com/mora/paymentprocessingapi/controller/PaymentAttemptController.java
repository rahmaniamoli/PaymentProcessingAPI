package com.mora.paymentprocessingapi.controller;

import com.mora.paymentprocessingapi.dto.PaymentAttemptResponse;
import com.mora.paymentprocessingapi.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentAttemptController {

    private final PaymentService paymentService;

    public PaymentAttemptController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/{paymentId}/attempts")
    public PaymentAttemptResponse createAttempt(@PathVariable UUID paymentId, Authentication authentication) {

        return paymentService.createAttempt(paymentId , authentication.getName());
    }

    @PostMapping("/payment-attempts/{attemptId}/success")
    public PaymentAttemptResponse markSuccess(@PathVariable UUID attemptId, Authentication authentication) {

        return paymentService.markAttemptAsSuccess(attemptId, authentication.getName());
    }

    @PostMapping("/payment-attempts/{attemptId}/fail")
    public PaymentAttemptResponse markFailed(@PathVariable UUID attemptId, @RequestParam String reason, Authentication authentication) {

        return paymentService.markAttemptAsFailed(attemptId, reason, authentication.getName());
    }

    @GetMapping("/payments/{paymentId}/attempts")
    public List<PaymentAttemptResponse> getAttempts(@PathVariable UUID paymentId, Authentication authentication) {

        return paymentService.getAttempts(paymentId , authentication.getName());
    }
    @PostMapping("/payments/{paymentId}/retry")
    public PaymentAttemptResponse retryPayment(@PathVariable UUID paymentId, Authentication authentication) {

        return paymentService.retryPayment(paymentId, authentication.getName());
    }

}
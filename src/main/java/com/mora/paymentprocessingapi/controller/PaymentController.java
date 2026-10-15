package com.mora.paymentprocessingapi.controller;

import com.mora.paymentprocessingapi.dto.CreatePaymentRequest;
import com.mora.paymentprocessingapi.dto.PaymentResponse;
import com.mora.paymentprocessingapi.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody CreatePaymentRequest request) {

        return paymentService.createPayment(idempotencyKey, request);
    }


    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id, Authentication authentication) {

        return paymentService.getPayment(id , authentication.getName());
    }

    @GetMapping
    public List<PaymentResponse> getPayments(@RequestParam UUID customerId, Authentication authentication) {

        return paymentService.getPayments(customerId , authentication.getName());
    }
}

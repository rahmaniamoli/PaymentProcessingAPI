package com.mora.paymentprocessingapi.controller;


import com.mora.paymentprocessingapi.dto.AuthResponse;
import com.mora.paymentprocessingapi.dto.LoginRequest;
import com.mora.paymentprocessingapi.dto.RegisterRequest;
import com.mora.paymentprocessingapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
package com.mora.paymentprocessingapi.security;

import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();

        String secret = Encoders.BASE64.encode(key.getEncoded());
        jwtService = new JwtService(secret, 15);

        user = User.withUsername("test@example.com")
                .password("unused")
                .roles("USER")
                .build();
    }

    @Test
    void shouldGenerateTokenWithCorrectUsername() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void shouldValidateTokenForMatchingUser() {
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken(user);

        UserDetails otherUser = User.withUsername("other@example.com")
                .password("unused")
                .roles("USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}
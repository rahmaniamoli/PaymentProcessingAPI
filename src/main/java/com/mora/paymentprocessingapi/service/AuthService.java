package com.mora.paymentprocessingapi.service;

import com.mora.paymentprocessingapi.domain.AppUser;
import com.mora.paymentprocessingapi.domain.Customer;
import com.mora.paymentprocessingapi.dto.AuthResponse;
import com.mora.paymentprocessingapi.dto.LoginRequest;
import com.mora.paymentprocessingapi.dto.RegisterRequest;
import com.mora.paymentprocessingapi.repository.AppUserRepository;
import com.mora.paymentprocessingapi.repository.CustomerRepository;
import com.mora.paymentprocessingapi.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = normalizeEmail(email);

        AppUser appUser = userRepository.findByEmail(normalizedEmail).orElseThrow(() ->
                new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(appUser.getRole())
                .build();
    }

    @Transactional
    @AuditEvent("CUSTOMER_REGISTERED")
    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Customer customer = new Customer("first name", "last name", email);
        customer = customerRepository.save(customer);

        AppUser user = new AppUser(email, passwordEncoder.encode(request.password()), customer);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        UserDetails user = loadUserByUsername(email);

        return new AuthResponse(jwtService.generateToken(user), "Bearer");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
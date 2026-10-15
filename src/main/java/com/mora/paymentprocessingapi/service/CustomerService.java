package com.mora.paymentprocessingapi.service;

import com.mora.paymentprocessingapi.domain.Customer;
import com.mora.paymentprocessingapi.dto.CreateCustomerRequest;
import com.mora.paymentprocessingapi.dto.CustomerResponse;
import com.mora.paymentprocessingapi.exception.ResourceConflictException;
import com.mora.paymentprocessingapi.exception.ResourceNotFoundException;
import com.mora.paymentprocessingapi.repository.CustomerRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        if (customerRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("Customer email already exists");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());

        Customer savedCustomer = customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "customers", key = "#id")
    public CustomerResponse getCustomer(UUID id) {

        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getCreatedAt());
    }
}
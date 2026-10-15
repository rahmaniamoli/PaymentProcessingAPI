package com.mora.paymentprocessingapi.repository;

import com.mora.paymentprocessingapi.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmail(String email);

    boolean existsByIdAndEmail(UUID id, String email);

    Optional<Customer> findByEmail(String email);
}
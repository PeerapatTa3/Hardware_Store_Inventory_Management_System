package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByPhone(String phone);

    java.util.Optional<Customer> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);
}


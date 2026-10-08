package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByPhoneIgnoreCase(String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);
}
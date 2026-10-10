package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findTop5ByOrderByCreatedAtDesc();
}

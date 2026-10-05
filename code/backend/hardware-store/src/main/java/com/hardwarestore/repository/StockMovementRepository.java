package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}

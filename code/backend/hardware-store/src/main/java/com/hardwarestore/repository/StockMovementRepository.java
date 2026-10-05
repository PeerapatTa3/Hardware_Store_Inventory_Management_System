package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.StockMovement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductId(Long productId, Sort sort);
}

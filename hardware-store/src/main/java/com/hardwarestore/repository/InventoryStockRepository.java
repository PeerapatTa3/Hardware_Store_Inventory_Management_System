package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByProductId(Long productId);
}

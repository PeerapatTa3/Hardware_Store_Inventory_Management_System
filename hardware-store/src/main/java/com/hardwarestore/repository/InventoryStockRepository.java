package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from InventoryStock stock where stock.product.id = :productId")
    Optional<InventoryStock> findByProductIdForUpdate(@Param("productId") Long productId);
}

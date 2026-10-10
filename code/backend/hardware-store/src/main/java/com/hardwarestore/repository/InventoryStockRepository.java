package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryStock i WHERE i.product.id = :productId")
    Optional<InventoryStock> findByProductIdForUpdate(@Param("productId") Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InventoryStock i")
    Long sumTotalStockUnits();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(i) FROM InventoryStock i WHERE i.quantity <= i.product.minimumStock")
    Long countLowStockItems();

    @org.springframework.data.jpa.repository.Query("SELECT i FROM InventoryStock i WHERE i.quantity <= i.product.minimumStock")
    java.util.List<InventoryStock> findLowStockItems();
}

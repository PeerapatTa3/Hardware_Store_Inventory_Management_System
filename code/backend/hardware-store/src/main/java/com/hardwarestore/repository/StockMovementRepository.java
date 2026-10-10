package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.StockMovement;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.domain.enums.StockMovementStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductId(Long productId, Sort sort);

    List<StockMovement> findByStatus(com.hardwarestore.domain.enums.StockMovementStatus status);

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM StockMovement m
            WHERE m.product.id = :productId
              AND m.id <> :adjustmentId
              AND m.status = :approvedStatus
              AND (
                    m.id > :adjustmentId
                    OR (m.movementType = :adjustmentType AND m.updatedAt > :requestedAt)
                  )
            """)
    boolean existsApprovedMovementAfterAdjustment(
            @Param("productId") Long productId,
            @Param("adjustmentId") Long adjustmentId,
            @Param("requestedAt") java.time.LocalDateTime requestedAt,
            @Param("approvedStatus") StockMovementStatus approvedStatus,
            @Param("adjustmentType") StockMovementType adjustmentType);
}

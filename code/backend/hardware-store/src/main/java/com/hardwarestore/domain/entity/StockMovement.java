package com.hardwarestore.domain.entity;

import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.domain.enums.StockMovementStatus;
import jakarta.persistence.*;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 100)
    private String referenceNo;

    @Column(length = 500)
    private String note;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime movementAt;

    @Column(name = "updated_at")
    @org.springframework.data.annotation.LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    @org.springframework.data.annotation.CreatedBy
    private String createdBy;

    @Column(name = "updated_by")
    @org.springframework.data.annotation.LastModifiedBy
    private String updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StockMovementStatus status = StockMovementStatus.APPROVED;

    @Column(name = "approved_by")
    private String approvedBy;

    @PrePersist
    public void prePersist() {
        if (movementAt == null) {
            movementAt = LocalDateTime.now();
        }
    }
}

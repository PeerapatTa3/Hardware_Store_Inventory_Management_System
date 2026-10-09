package com.hardwarestore.domain.entity;

import jakarta.persistence.*;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.hardwarestore.domain.enums.PurchaseOrderStatus;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_purchase_order_number", columnList = "purchase_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_number", nullable = false, unique = true, length = 50)
    private String purchaseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status = PurchaseOrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @org.springframework.data.annotation.LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    @org.springframework.data.annotation.CreatedBy
    private String createdBy;

    @Column(name = "updated_by")
    @org.springframework.data.annotation.LastModifiedBy
    private String updatedBy;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PurchaseItem> items = new ArrayList<>();

    public void calculateTotal() {
        if (items == null || items.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            return;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseItem item : items) {
            if (item != null) {
                item.calculateSubtotal();
                total = total.add(item.getSubtotal());
            }
        }
        this.totalAmount = total;
    }
    
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = PurchaseOrderStatus.PENDING;
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

    }
}



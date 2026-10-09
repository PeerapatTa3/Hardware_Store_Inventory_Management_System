package com.hardwarestore.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.domain.state.CancelledState;
import com.hardwarestore.domain.state.CompletedState;
import com.hardwarestore.domain.state.ConfirmedState;
import com.hardwarestore.domain.state.ShippedState;
import com.hardwarestore.domain.state.OrderState;
import com.hardwarestore.domain.state.PendingState;
import com.hardwarestore.service.strategy.BulkDiscount;
import com.hardwarestore.service.strategy.DiscountStrategy;
import com.hardwarestore.service.strategy.MemberDiscount;
import com.hardwarestore.service.strategy.NormalDiscount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sales_orders", indexes = {
        @Index(name = "idx_sales_order_number", columnList = "order_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalesOrderStatus status = SalesOrderStatus.PENDING;

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

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SalesOrderItems> items = new ArrayList<>();

    @Transient
    private transient OrderState state;

    @Transient
    private DiscountStrategy discountStrategy;

    @PostLoad
    public void initializeState() {
        if (state == null) {
            state = createState(status);
        }
    }

    public void confirm() {
        getCurrentState().confirm(this);
    }

    public void cancel() {
        getCurrentState().cancel(this);
    }

    public void ship() {
        getCurrentState().ship(this);
    }

    public void complete() {
        getCurrentState().complete(this);
    }

    public OrderState getCurrentState() {
        if (state == null) {
            state = createState(status);
        }
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
        if (state != null) {
            this.status = state.getStatus();
        }
    }

    public void setStatus(SalesOrderStatus status) {
        this.status = status;
        this.state = createState(status);
    }

    public DiscountStrategy getDiscountStrategy() {
        if (discountStrategy == null || discountStrategy instanceof NormalDiscount) {
            DiscountStrategy resolved = resolveDiscountStrategy();
            if (discountStrategy == null || discountStrategy instanceof NormalDiscount) {
                discountStrategy = resolved;
            }
            return resolved;
        }
        return discountStrategy;
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public DiscountStrategy resolveDiscountStrategy() {
        if (customer != null && customer.isMember()) {
            return new MemberDiscount();
        }

        int totalQuantity = items == null ? 0 : items.stream()
                .filter(item -> item != null)
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();

        if (totalQuantity >= 10) {
            return new BulkDiscount();
        }

        return new NormalDiscount();
    }

    public BigDecimal calculateTotal() {
        if (items == null || items.isEmpty()) {
            return totalAmount == null ? BigDecimal.ZERO : totalAmount;
        }

        DiscountStrategy strategy = getDiscountStrategy();
        BigDecimal total = items.stream()
                .filter(item -> item != null)
                .map(item -> {
                    BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    return strategy.apply(unitPrice, quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        this.totalAmount = total;
        return total;
    }

    public void applyPricing() {
        this.totalAmount = calculateTotal();
    }

    private OrderState createState(SalesOrderStatus status) {
        if (status == null) {
            return new PendingState();
        }

        return switch (status) {
            case PENDING -> new PendingState();
            case CONFIRMED -> new ConfirmedState();
            case SHIPPED -> new ShippedState();
            case COMPLETED -> new CompletedState();
            case CANCELLED -> new CancelledState();
        };
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = SalesOrderStatus.PENDING;
        }
        if (state == null) {
            state = createState(status);
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

    }

}



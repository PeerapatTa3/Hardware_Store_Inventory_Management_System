package com.hardwarestore.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}

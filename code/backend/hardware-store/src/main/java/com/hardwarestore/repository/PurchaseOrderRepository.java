package com.hardwarestore.repository;

import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    java.util.List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
}

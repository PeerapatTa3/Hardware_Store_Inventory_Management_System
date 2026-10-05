package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseItem;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.entity.PurchaseOrderStatus;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseItemRequest;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.response.PurchaseItemResponse;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrder toEntity(
            PurchaseOrderRequest request,
            String purchaseNumber,
            Supplier supplier,
            Map<Long, Product> productsById) {
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNumber(purchaseNumber);
        order.setSupplier(supplier);
        order.setStatus(PurchaseOrderStatus.PENDING);
        updatePendingOrder(order, request, supplier, productsById);
        return order;
    }

    public void updatePendingOrder(
            PurchaseOrder order,
            PurchaseOrderRequest request,
            Supplier supplier,
            Map<Long, Product> productsById) {
        order.setSupplier(supplier);
        order.getItems().clear();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            Product product = productsById.get(itemRequest.getProductId());
            if (product == null) {
                throw new IllegalArgumentException(
                        "Product must be resolved before mapping purchase item: " + itemRequest.getProductId());
            }

            BigDecimal subtotal = itemRequest.getUnitCost()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            PurchaseItem item = new PurchaseItem();
            item.setPurchaseOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitCost(itemRequest.getUnitCost());
            item.setSubtotal(subtotal);
            order.getItems().add(item);
            totalAmount = totalAmount.add(subtotal);
        }
        order.setTotalAmount(totalAmount);
    }

    public PurchaseOrderResponse toResponse(PurchaseOrder order) {
        if (order == null) {
            return null;
        }

        List<PurchaseItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .purchaseNumber(order.getPurchaseNumber())
                .supplierId(order.getSupplier() != null ? order.getSupplier().getId() : null)
                .supplierName(order.getSupplier() != null ? order.getSupplier().getName() : null)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private PurchaseItemResponse toItemResponse(PurchaseItem item) {
        Product product = item.getProduct();
        return PurchaseItemResponse.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .quantity(item.getQuantity())
                .unitCost(item.getUnitCost())
                .subtotal(item.getSubtotal())
                .build();
    }
}

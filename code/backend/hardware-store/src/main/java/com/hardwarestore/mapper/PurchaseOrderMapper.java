package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseItem;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.enums.PurchaseOrderStatus;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseItemRequest;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.response.PurchaseItemResponse;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class PurchaseOrderMapper {

    public PurchaseOrder toEntity(
            PurchaseOrderRequest request,
            String purchaseNumber,
            Supplier supplier,
            Map<Long, Product> productsById) {
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNumber(purchaseNumber);
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

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    public abstract PurchaseOrderResponse toResponse(PurchaseOrder order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    protected abstract PurchaseItemResponse toItemResponse(PurchaseItem item);
}

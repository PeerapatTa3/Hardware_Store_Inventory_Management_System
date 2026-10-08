package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.entity.SalesOrder.SalesOrderStatus;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.response.SalesOrderItemResponse;
import com.hardwarestore.dto.response.SalesOrderResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SalesOrderMapper {

    public SalesOrder toEntity(SalesOrderRequest request, Customer customer, Map<Long, Product> productsById) {
        SalesOrder order = new SalesOrder();
        order.setOrderNumber("SO-" + UUID.randomUUID());
        order.setCustomer(customer);
        order.setStatus(SalesOrderStatus.PENDING);
        updatePendingOrder(order, request, customer, productsById);
        return order;
    }

    public void updatePendingOrder(
            SalesOrder order,
            SalesOrderRequest request,
            Customer customer,
            Map<Long, Product> productsById) {
        order.setCustomer(customer);
        order.getItems().clear();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderItemRequest itemRequest : request.getItems()) {
            Product product = productsById.get(itemRequest.getProductId());
            if (product == null) {
                throw new IllegalArgumentException(
                        "Product must be resolved before mapping sales order item: " + itemRequest.getProductId());
            }

            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;

            BigDecimal subtotal = unitPrice
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            SalesOrderItems item = new SalesOrderItems();
            item.setSalesOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP));
            item.setSubtotal(subtotal);
            order.getItems().add(item);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
    }

    public SalesOrderResponse toResponse(SalesOrder order) {
        if (order == null) {
            return null;
        }

        List<SalesOrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return SalesOrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private SalesOrderItemResponse toItemResponse(SalesOrderItems item) {
        Product product = item.getProduct();
        return SalesOrderItemResponse.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}

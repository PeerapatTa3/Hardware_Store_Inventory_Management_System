package com.hardwarestore.mapper;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.response.SalesOrderItemResponse;
import com.hardwarestore.dto.response.SalesOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class SalesOrderMapper {

    public SalesOrder toEntity(SalesOrderRequest request, Customer customer, Map<Long, Product> productsById) {
        SalesOrder order = new SalesOrder();
        order.setOrderNumber("SO-" + UUID.randomUUID());
        order.setStatus(SalesOrderStatus.COMPLETED);
        updatePendingOrder(order, request, customer, productsById);
        return order;
    }

    public void updatePendingOrder(
            SalesOrder order,
            SalesOrderRequest request,
            Customer customer,
            Map<Long, Product> productsById) {
        order.setCustomer(customer);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.getItems().clear();
        for (SalesOrderItemRequest itemRequest : request.getItems()) {
            Product product = productsById.get(itemRequest.getProductId());
            if (product == null) {
                throw new IllegalArgumentException(
                        "Product must be resolved before mapping sales order item: " + itemRequest.getProductId());
            }

            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;

            SalesOrderItems item = new SalesOrderItems();
            item.setSalesOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP));
            
            order.getItems().add(item);
        }

        // Delegate all calculations (subtotal, totalAmount, discounts) to the Domain Entity
        order.applyPricing();
    }

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    public abstract SalesOrderResponse toResponse(SalesOrder order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    protected abstract SalesOrderItemResponse toItemResponse(SalesOrderItems item);
}

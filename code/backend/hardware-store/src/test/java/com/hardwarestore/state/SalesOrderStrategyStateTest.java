package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.strategy.BulkDiscount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesOrderStrategyStateTest {

    @Test
    void pendingOrderShouldApplyBulkDiscountWhenConfirmedAndCompleted() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrderStatus.PENDING);
        order.setDiscountStrategy(new BulkDiscount());

        SalesOrderItems item = new SalesOrderItems();
        item.setQuantity(10);
        item.setUnitPrice(new BigDecimal("100.00"));
        order.getItems().add(item);

        order.confirm();
        assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());

        order.applyPricing();
        order.complete();
        assertEquals(SalesOrderStatus.COMPLETED, order.getStatus());
        assertEquals(new BigDecimal("900.00"), order.getTotalAmount().setScale(2));
    }

    @Test
    void completeShouldResolveBulkDiscountFromOrderQuantityWhenStrategyWasNotPersisted() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrderStatus.PENDING);
        order.setDiscountStrategy(null);

        SalesOrderItems item = new SalesOrderItems();
        item.setQuantity(10);
        item.setUnitPrice(new BigDecimal("100.00"));
        order.getItems().add(item);

        order.confirm();
        order.applyPricing();
        order.complete();

        assertEquals(new BigDecimal("900.00"), order.getTotalAmount().setScale(2));
    }

    @Test
    void completeShouldResolveMemberDiscountBeforeBulkDiscountWhenCustomerIsMember() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrderStatus.PENDING);

        Customer customer = new Customer();
        customer.setMember(true);
        order.setCustomer(customer);

        SalesOrderItems item = new SalesOrderItems();
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        order.getItems().add(item);

        order.confirm();
        order.applyPricing();
        order.complete();

        assertEquals(new BigDecimal("180.00"), order.getTotalAmount().setScale(2));
    }
}

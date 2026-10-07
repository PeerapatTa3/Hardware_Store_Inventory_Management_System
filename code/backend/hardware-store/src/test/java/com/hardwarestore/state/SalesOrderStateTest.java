package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesOrderStateTest {

    @Test
    void pendingOrderShouldTransitionToConfirmedAndCompleted() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrder.SalesOrderStatus.PENDING);

        order.confirm();
        assertEquals(SalesOrder.SalesOrderStatus.CONFIRMED, order.getStatus());

        order.complete();
        assertEquals(SalesOrder.SalesOrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void pendingOrderShouldCancel() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrder.SalesOrderStatus.PENDING);

        order.cancel();

        assertEquals(SalesOrder.SalesOrderStatus.CANCELLED, order.getStatus());
    }
}

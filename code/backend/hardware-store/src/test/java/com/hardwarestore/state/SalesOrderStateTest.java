package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void invalidTransitionsShouldThrowIllegalStateException() {
        SalesOrder pendingOrder = new SalesOrder();
        pendingOrder.setStatus(SalesOrder.SalesOrderStatus.PENDING);
        assertThrows(IllegalStateException.class, pendingOrder::complete);

        SalesOrder confirmedOrder = new SalesOrder();
        confirmedOrder.setStatus(SalesOrder.SalesOrderStatus.CONFIRMED);
        assertThrows(IllegalStateException.class, confirmedOrder::confirm);

        SalesOrder cancelledOrder = new SalesOrder();
        cancelledOrder.setStatus(SalesOrder.SalesOrderStatus.CANCELLED);
        assertThrows(IllegalStateException.class, cancelledOrder::confirm);
        assertThrows(IllegalStateException.class, cancelledOrder::cancel);
        assertThrows(IllegalStateException.class, cancelledOrder::complete);

        SalesOrder completedOrder = new SalesOrder();
        completedOrder.setStatus(SalesOrder.SalesOrderStatus.COMPLETED);
        assertThrows(IllegalStateException.class, completedOrder::confirm);
        assertThrows(IllegalStateException.class, completedOrder::cancel);
        assertThrows(IllegalStateException.class, completedOrder::complete);
    }
}

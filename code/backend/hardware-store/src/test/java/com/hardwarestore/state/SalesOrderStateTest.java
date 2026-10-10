package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.exception.InvalidSalesOrderStateException;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesOrderStateTest {

    @Test
    void pendingOrderShouldTransitionToConfirmedAndCompleted() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrderStatus.PENDING);

        order.confirm();
        assertEquals(SalesOrderStatus.CONFIRMED, order.getStatus());

        order.complete();
        assertEquals(SalesOrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void pendingOrderShouldCancel() {
        SalesOrder order = new SalesOrder();
        order.setStatus(SalesOrderStatus.PENDING);

        order.cancel();

        assertEquals(SalesOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void invalidTransitionsShouldThrowIllegalStateException() {
        SalesOrder pendingOrder = new SalesOrder();
        pendingOrder.setStatus(SalesOrderStatus.PENDING);
        assertThrows(InvalidSalesOrderStateException.class, pendingOrder::complete);

        SalesOrder confirmedOrder = new SalesOrder();
        confirmedOrder.setStatus(SalesOrderStatus.CONFIRMED);
        assertThrows(InvalidSalesOrderStateException.class, confirmedOrder::confirm);

        SalesOrder cancelledOrder = new SalesOrder();
        cancelledOrder.setStatus(SalesOrderStatus.CANCELLED);
        assertThrows(InvalidSalesOrderStateException.class, cancelledOrder::confirm);
        assertThrows(InvalidSalesOrderStateException.class, cancelledOrder::cancel);
        assertThrows(InvalidSalesOrderStateException.class, cancelledOrder::complete);

        SalesOrder completedOrder = new SalesOrder();
        completedOrder.setStatus(SalesOrderStatus.COMPLETED);
        assertThrows(InvalidSalesOrderStateException.class, completedOrder::confirm);
        
        assertThrows(InvalidSalesOrderStateException.class, completedOrder::complete);
    }
}



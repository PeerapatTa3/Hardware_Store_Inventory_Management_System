package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;

public class CancelledState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new IllegalStateException("Cancelled orders cannot be confirmed.");
    }

    @Override
    public void cancel(SalesOrder order) {
        throw new IllegalStateException("Order is already cancelled.");
    }

    @Override
    public void complete(SalesOrder order) {
        throw new IllegalStateException("Cancelled orders cannot be completed.");
    }

    @Override
    public SalesOrder.SalesOrderStatus getStatus() {
        return SalesOrder.SalesOrderStatus.CANCELLED;
    }
}

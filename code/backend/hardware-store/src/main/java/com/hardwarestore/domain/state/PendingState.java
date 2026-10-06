package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;

public class PendingState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        order.setStatus(SalesOrder.SalesOrderStatus.CONFIRMED);
        order.setState(new ConfirmedState());
    }

    @Override
    public void cancel(SalesOrder order) {
        order.setStatus(SalesOrder.SalesOrderStatus.CANCELLED);
        order.setState(new CancelledState());
    }

    @Override
    public void complete(SalesOrder order) {
        throw new IllegalStateException("Only confirmed orders can be completed.");
    }

    @Override
    public SalesOrder.SalesOrderStatus getStatus() {
        return SalesOrder.SalesOrderStatus.PENDING;
    }
}

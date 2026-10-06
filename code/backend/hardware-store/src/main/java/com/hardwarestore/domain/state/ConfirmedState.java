package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;

public class ConfirmedState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new IllegalStateException("Order is already confirmed.");
    }

    @Override
    public void cancel(SalesOrder order) {
        order.setStatus(SalesOrder.SalesOrderStatus.CANCELLED);
        order.setState(new CancelledState());
    }

    @Override
    public void complete(SalesOrder order) {
        order.setStatus(SalesOrder.SalesOrderStatus.COMPLETED);
        order.setState(new CompletedState());
    }

    @Override
    public SalesOrder.SalesOrderStatus getStatus() {
        return SalesOrder.SalesOrderStatus.CONFIRMED;
    }
}

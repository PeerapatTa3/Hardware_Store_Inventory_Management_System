package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;

public class ConfirmedState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new IllegalStateException("Order is already confirmed.");
    }

    @Override
    public void cancel(SalesOrder order) {
        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setState(new CancelledState());
    }

    @Override
    public void complete(SalesOrder order) {
        order.applyPricing();
        order.setStatus(SalesOrderStatus.COMPLETED);
        order.setState(new CompletedState());
    }

    @Override
    public SalesOrderStatus getStatus() {
        return SalesOrderStatus.CONFIRMED;
    }
}

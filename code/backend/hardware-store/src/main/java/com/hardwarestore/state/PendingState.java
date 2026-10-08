package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;

public class PendingState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setState(new ConfirmedState());
    }

    @Override
    public void cancel(SalesOrder order) {
        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setState(new CancelledState());
    }

    @Override
    public void complete(SalesOrder order) {
        throw new IllegalStateException("Only confirmed orders can be completed.");
    }

    @Override
    public SalesOrderStatus getStatus() {
        return SalesOrderStatus.PENDING;
    }
}

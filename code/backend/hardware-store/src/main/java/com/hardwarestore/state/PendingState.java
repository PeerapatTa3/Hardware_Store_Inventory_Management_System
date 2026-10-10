package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.exception.InvalidSalesOrderStateException;

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
    public void ship(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Cannot ship order from status " + getStatus());
    }

    @Override
    public void complete(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Only confirmed orders can be completed.");
    }

    @Override
    public SalesOrderStatus getStatus() {
        return SalesOrderStatus.PENDING;
    }
}




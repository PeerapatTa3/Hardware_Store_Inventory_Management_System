package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.exception.InvalidSalesOrderStateException;

public class ConfirmedState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Order is already confirmed.");
    }

    @Override
    public void cancel(SalesOrder order) {
        order.setStatus(SalesOrderStatus.CANCELLED);
        order.setState(new CancelledState());
    }

    @Override
    public void ship(SalesOrder order) {
        order.setStatus(SalesOrderStatus.SHIPPED);
        order.setState(new ShippedState());
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




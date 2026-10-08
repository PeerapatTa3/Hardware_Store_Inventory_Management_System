package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.exception.InvalidSalesOrderStateException;

public class ShippedState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Already shipped, cannot confirm again.");
    }

    @Override
    public void ship(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Already shipped.");
    }

    @Override
    public void cancel(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Cannot cancel an order that is already shipped.");
    }

    @Override
    public void complete(SalesOrder order) {
        order.setStatus(SalesOrderStatus.COMPLETED);
        order.setState(new CompletedState());
    }

    @Override
    public SalesOrderStatus getStatus() {
        return SalesOrderStatus.SHIPPED;
    }
}


package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.exception.InvalidSalesOrderStateException;

public class CompletedState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Completed orders cannot be confirmed again.");
    }

    @Override
    public void cancel(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Completed orders cannot be cancelled.");
    }

    @Override
    public void ship(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Cannot ship order from status " + getStatus());
    }

    @Override
    public void complete(SalesOrder order) {
        throw new InvalidSalesOrderStateException("Order is already completed.");
    }

    @Override
    public SalesOrderStatus getStatus() {
        return SalesOrderStatus.COMPLETED;
    }
}




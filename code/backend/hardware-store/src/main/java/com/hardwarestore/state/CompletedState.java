package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;

public class CompletedState implements OrderState {
    @Override
    public void confirm(SalesOrder order) {
        throw new IllegalStateException("Completed orders cannot be confirmed again.");
    }

    @Override
    public void cancel(SalesOrder order) {
        throw new IllegalStateException("Completed orders cannot be cancelled.");
    }

    @Override
    public void complete(SalesOrder order) {
        throw new IllegalStateException("Order is already completed.");
    }

    @Override
    public SalesOrderStatus getStatus() {
        return SalesOrderStatus.COMPLETED;
    }
}

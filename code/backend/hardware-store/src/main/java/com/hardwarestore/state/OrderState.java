package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;

public interface OrderState {
    void confirm(SalesOrder order);

    void cancel(SalesOrder order);

    void complete(SalesOrder order);

    SalesOrder.SalesOrderStatus getStatus();
}

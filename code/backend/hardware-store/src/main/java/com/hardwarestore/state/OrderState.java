package com.hardwarestore.domain.state;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;

public interface OrderState {
    void confirm(SalesOrder order);

    void cancel(SalesOrder order);

    void ship(SalesOrder order);

    void complete(SalesOrder order);

    SalesOrderStatus getStatus();
}


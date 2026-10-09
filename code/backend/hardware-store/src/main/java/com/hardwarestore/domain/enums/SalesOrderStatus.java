package com.hardwarestore.domain.enums;

import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.exception.InvalidSalesOrderStateException;

public enum SalesOrderStatus {
    PENDING {
        @Override
        public void apply(SalesOrder order) {
            throw new InvalidSalesOrderStateException("Cannot explicitly transition to PENDING");
        }
    },
    CONFIRMED {
        @Override
        public void apply(SalesOrder order) {
            order.confirm();
        }
    },
    SHIPPED {
        @Override
        public void apply(SalesOrder order) {
            order.ship();
        }
    },
    COMPLETED {
        @Override
        public void apply(SalesOrder order) {
            order.complete();
        }
    },
    CANCELLED {
        @Override
        public void apply(SalesOrder order) {
            order.cancel();
        }
    };

    public abstract void apply(SalesOrder order);
}

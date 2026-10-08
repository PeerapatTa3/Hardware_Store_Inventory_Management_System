package com.hardwarestore.listener;

import com.hardwarestore.event.LowStockEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer that reacts to {@link LowStockEvent}. The stock service does not know
 * about this class; more observers (email, LINE, dashboard) can be added
 * without modifying the publisher.
 */
@Slf4j
@Component
public class LowStockAlertListener {

    @EventListener
    public void onLowStock(LowStockEvent event) {
        log.warn("LOW STOCK: product id={} sku={} name='{}' available={} minimum={}",
                event.productId(), event.sku(), event.productName(),
                event.availableQuantity(), event.minimumStock());
    }
}

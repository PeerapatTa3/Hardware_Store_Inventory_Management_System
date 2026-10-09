package com.hardwarestore.domain.enums;

import com.hardwarestore.domain.entity.InventoryStock;

public enum StockMovementType {
    IN {
        @Override
        public void process(InventoryStock inventory, int quantity) {
            if (inventory.getQuantity() > Integer.MAX_VALUE - quantity) {
                throw new IllegalArgumentException(
                        "Inbound quantity exceeds supported stock quantity for product id: " + inventory.getProduct().getId());
            }
            inventory.setQuantity(inventory.getQuantity() + quantity);
        }
    },
    OUT {
        @Override
        public void process(InventoryStock inventory, int quantity) {
            int available = inventory.getAvailableQuantity();
            if (available < quantity) {
                throw new IllegalArgumentException("Insufficient available quantity for product id: " + inventory.getProduct().getId());
            }
            inventory.setQuantity(inventory.getQuantity() - quantity);
        }
    },
    ADJUSTMENT {
        @Override
        public void process(InventoryStock inventory, int quantity) {
            if (quantity < inventory.getReservedQuantity()) {
                throw new IllegalArgumentException(
                        "Adjusted quantity cannot be less than reserved quantity for product id: "
                                + inventory.getProduct().getId());
            }
            inventory.setQuantity(quantity);
        }
    };

    public abstract void process(InventoryStock inventory, int quantity);
}

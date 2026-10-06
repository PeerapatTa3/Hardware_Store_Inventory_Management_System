package com.hardwarestore.domain.entity;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderEntityTest {

    @Test
    void salesOrderShouldHaveCustomerAndItemsRelations() throws NoSuchFieldException {
        Field customerField = SalesOrder.class.getDeclaredField("customer");
        assertNotNull(customerField.getAnnotation(ManyToOne.class));
        assertEquals(Customer.class, customerField.getType());

        Field itemsField = SalesOrder.class.getDeclaredField("items");
        assertNotNull(itemsField.getAnnotation(OneToMany.class));
        assertEquals(List.class, itemsField.getType());
    }

    @Test
    void salesOrderShouldSupportTheDocumentedLifecycleStates() {
        assertArrayEquals(
                new String[]{"PENDING", "CONFIRMED", "SHIPPED", "COMPLETED", "CANCELLED"},
                java.util.Arrays.stream(SalesOrder.SalesOrderStatus.values())
                        .map(Enum::name)
                        .toArray(String[]::new));
    }
}

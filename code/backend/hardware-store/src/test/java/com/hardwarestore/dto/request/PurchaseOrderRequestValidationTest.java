package com.hardwarestore.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderRequestValidationTest {

    private static Validator validator;
    private static AutoCloseable validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory::close;
    }

    @AfterAll
    static void closeValidatorFactory() throws Exception {
        validatorFactory.close();
    }

    @Test
    void shouldRejectMissingSupplierAndEmptyItems() {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setItems(List.of());

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("supplierId", "items"), invalidProperties);
    }

    @Test
    void shouldRejectInvalidNestedPurchaseItemValues() {
        PurchaseItemRequest item = new PurchaseItemRequest();
        item.setProductId(0L);
        item.setQuantity(-2);
        item.setUnitCost(BigDecimal.ZERO);
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(-1L);
        request.setItems(List.of(item));

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("supplierId", "items[0].productId", "items[0].quantity",
                "items[0].unitCost"), invalidProperties);
    }

    @Test
    void shouldAcceptValidPurchaseOrderRequest() {
        PurchaseItemRequest item = new PurchaseItemRequest();
        item.setProductId(4L);
        item.setQuantity(2);
        item.setUnitCost(new BigDecimal("12.50"));
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(3L);
        request.setItems(List.of(item));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectNullPurchaseItem() {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(3L);
        request.setItems(Arrays.asList((PurchaseItemRequest) null));

        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getPropertyPath().toString().startsWith("items[0]"));
    }
}

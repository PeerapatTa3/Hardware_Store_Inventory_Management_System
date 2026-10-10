package com.hardwarestore.validation;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.dto.request.SalesOrderRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Data passed along the validation chain. Handlers read the request and put the
 * entities they resolve back into the context for later handlers and the caller.
 */
public class OrderValidationContext {

    private final SalesOrderRequest request;
    private final Map<Long, Integer> previousQuantitiesByProduct;
    private Customer customer;
    private Map<Long, Product> productsById = new HashMap<>();

    public OrderValidationContext(SalesOrderRequest request, Map<Long, Integer> previousQuantitiesByProduct) {
        this.request = request;
        this.previousQuantitiesByProduct = previousQuantitiesByProduct;
    }

    public SalesOrderRequest getRequest() {
        return request;
    }

    public Map<Long, Integer> getPreviousQuantitiesByProduct() {
        return previousQuantitiesByProduct;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Map<Long, Product> getProductsById() {
        return productsById;
    }

    public void setProductsById(Map<Long, Product> productsById) {
        this.productsById = productsById;
    }
}

package com.hardwarestore.validation;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.repository.ProductRepository;

import java.util.HashMap;
import java.util.Map;

/** Checks that every product in the request exists and resolves them into the context. */
public class ProductsExistHandler extends OrderValidationHandler {

    private final ProductRepository productRepository;

    public ProductsExistHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    protected void validate(OrderValidationContext context) {
        Map<Long, Product> productsById = new HashMap<>();
        for (SalesOrderItemRequest itemRequest : context.getRequest().getItems()) {
            Long productId = itemRequest.getProductId();
            if (!productsById.containsKey(productId)) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product not found with id: " + productId));
                productsById.put(productId, product);
            }
        }
        context.setProductsById(productsById);
    }
}

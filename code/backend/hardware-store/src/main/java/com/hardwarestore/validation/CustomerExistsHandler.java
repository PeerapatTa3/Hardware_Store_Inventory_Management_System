package com.hardwarestore.validation;

import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.repository.CustomerRepository;

/** Checks that the customer in the request exists. */
public class CustomerExistsHandler extends OrderValidationHandler {

    private final CustomerRepository customerRepository;

    public CustomerExistsHandler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    protected void validate(OrderValidationContext context) {
        Long customerId = context.getRequest().getCustomerId();
        context.setCustomer(customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId)));
    }
}

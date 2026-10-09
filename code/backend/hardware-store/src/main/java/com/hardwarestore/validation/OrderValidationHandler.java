package com.hardwarestore.validation;

/**
 * Base handler of the Chain of Responsibility. Each handler performs one check,
 * then forwards the context to the next handler in the chain.
 */
public abstract class OrderValidationHandler {

    private OrderValidationHandler next;

    /** Links the next handler and returns it so calls can be chained fluently. */
    public OrderValidationHandler setNext(OrderValidationHandler next) {
        this.next = next;
        return next;
    }

    public final void handle(OrderValidationContext context) {
        validate(context);
        if (next != null) {
            next.handle(context);
        }
    }

    /** Performs this handler's check; throws an exception to stop the chain. */
    protected abstract void validate(OrderValidationContext context);
}

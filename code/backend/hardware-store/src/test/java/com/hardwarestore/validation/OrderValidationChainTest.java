package com.hardwarestore.validation;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.repository.CustomerRepository;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Verifies the Chain of Responsibility: customer -> products -> stock. */
@ExtendWith(MockitoExtension.class)
class OrderValidationChainTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryStockRepository inventoryStockRepository;

    private OrderValidationHandler chain;
    private SalesOrderRequest request;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        chain = new CustomerExistsHandler(customerRepository);
        chain.setNext(new ProductsExistHandler(productRepository))
                .setNext(new StockAvailableHandler(inventoryStockRepository));

        SalesOrderItemRequest item = new SalesOrderItemRequest();
        item.setProductId(10L);
        item.setQuantity(3);
        request = new SalesOrderRequest();
        request.setCustomerId(1L);
        request.setItems(List.of(item));

        customer = new Customer();
        customer.setId(1L);
        product = new Product();
        product.setId(10L);
    }

    private InventoryStock stock(int quantity) {
        InventoryStock stock = new InventoryStock();
        stock.setProduct(product);
        stock.setQuantity(quantity);
        stock.setReservedQuantity(0);
        return stock;
    }

    @Test
    void handle_shouldPassAndPopulateContextWhenAllChecksSucceed() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock(5)));

        OrderValidationContext context = new OrderValidationContext(request, Map.of());
        chain.handle(context);

        assertSame(customer, context.getCustomer());
        assertSame(product, context.getProductsById().get(10L));
    }

    @Test
    void handle_shouldStopAtCustomerHandlerWhenCustomerMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chain.handle(new OrderValidationContext(request, Map.of())));
        verifyNoInteractions(productRepository, inventoryStockRepository);
    }

    @Test
    void handle_shouldStopAtProductHandlerWhenProductMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chain.handle(new OrderValidationContext(request, Map.of())));
        verifyNoInteractions(inventoryStockRepository);
    }

    @Test
    void handle_shouldRejectWhenStockIsInsufficient() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock(2)));

        assertThrows(IllegalArgumentException.class,
                () -> chain.handle(new OrderValidationContext(request, Map.of())));
    }

    @Test
    void handle_shouldAddBackPreviousQuantityWhenEditingOrder() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryStockRepository.findByProductId(10L)).thenReturn(Optional.of(stock(1)));

        // available 1 + previously held 2 = 3, enough for requested 3
        OrderValidationContext context = new OrderValidationContext(request, Map.of(10L, 2));
        chain.handle(context);

        assertEquals(1, context.getProductsById().size());
    }
}

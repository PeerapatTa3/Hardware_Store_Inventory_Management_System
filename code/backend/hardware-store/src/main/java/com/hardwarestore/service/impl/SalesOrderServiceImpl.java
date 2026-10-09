package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.enums.SalesOrderStatus;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.SalesOrderResponse;
import com.hardwarestore.exception.InvalidSalesOrderStateException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.SalesOrderMapper;
import com.hardwarestore.repository.CustomerRepository;
import com.hardwarestore.repository.InventoryStockRepository;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.SalesOrderRepository;
import com.hardwarestore.service.SalesOrderService;
import com.hardwarestore.service.StockMovementService;
import com.hardwarestore.validation.CustomerExistsHandler;
import com.hardwarestore.validation.OrderValidationContext;
import com.hardwarestore.validation.OrderValidationHandler;
import com.hardwarestore.validation.ProductsExistHandler;
import com.hardwarestore.validation.StockAvailableHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final StockMovementService stockMovementService;

    @Override
    @Transactional
    public SalesOrderResponse create(SalesOrderRequest request) {
        OrderValidationContext context = new OrderValidationContext(request, Map.of());
        buildValidationChain().handle(context);

        SalesOrder order = salesOrderMapper.toEntity(request, context.getCustomer(), context.getProductsById());

        SalesOrder savedOrder = salesOrderRepository.save(order);
        applyStockOut(savedOrder);

        return salesOrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderResponse> findAll() {
        return salesOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(salesOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderResponse findById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
        return salesOrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public SalesOrderResponse update(Long id, SalesOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        if (order.getStatus() != SalesOrderStatus.PENDING) {
            throw new InvalidSalesOrderStateException(
                    "Only pending sales orders can be edited; order id " + id
                            + " has status " + order.getStatus());
        }

        Map<Long, Integer> previousQuantitiesByProduct = order.getItems().stream()
                .collect(HashMap::new, (map, item) -> map.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum), HashMap::putAll);

        OrderValidationContext context = new OrderValidationContext(request, previousQuantitiesByProduct);
        buildValidationChain().handle(context);

        salesOrderMapper.updatePendingOrder(order, request, context.getCustomer(), context.getProductsById());

        SalesOrder savedOrder = salesOrderRepository.save(order);
        applyPendingOrderStockAdjustment(savedOrder, previousQuantitiesByProduct);
        return salesOrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public SalesOrderResponse updateStatus(Long id, SalesOrderStatus status) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));

        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }

        // Delegate all transition rules to the State pattern.
        // Each State class (PendingState, ConfirmedState, etc.) enforces valid
        // transitions and throws InvalidSalesOrderStateException on illegal moves.
        switch (status) {
            case CONFIRMED -> order.confirm();
            case SHIPPED   -> order.ship();
            case COMPLETED -> order.complete();
            case CANCELLED -> order.cancel();
            case PENDING   -> throw new InvalidSalesOrderStateException(
                    "Cannot revert order id " + id + " back to PENDING.");
            default        -> throw new IllegalArgumentException("Unsupported sales order status: " + status);
        }

        SalesOrder savedOrder = salesOrderRepository.save(order);
        if (savedOrder.getStatus() == SalesOrderStatus.CANCELLED) {
            applyStockReturnOnCancel(savedOrder);
        }
        return salesOrderMapper.toResponse(savedOrder);
    }

    /** Builds the Chain of Responsibility: customer -> products -> stock. */
    private OrderValidationHandler buildValidationChain() {
        OrderValidationHandler first = new CustomerExistsHandler(customerRepository);
        first.setNext(new ProductsExistHandler(productRepository))
                .setNext(new StockAvailableHandler(inventoryStockRepository));
        return first;
    }

    private void applyPendingOrderStockAdjustment(SalesOrder order, Map<Long, Integer> previousQuantitiesByProduct) {
        Map<Long, Integer> newQuantitiesByProduct = new HashMap<>();
        for (SalesOrderItems item : order.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }
            newQuantitiesByProduct.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
        }

        for (Long productId : java.util.stream.Stream.concat(
                previousQuantitiesByProduct.keySet().stream(),
                newQuantitiesByProduct.keySet().stream())
                .distinct()
                .toList()) {
            int previousQuantity = previousQuantitiesByProduct.getOrDefault(productId, 0);
            int newQuantity = newQuantitiesByProduct.getOrDefault(productId, 0);
            int delta = newQuantity - previousQuantity;
            if (delta == 0) {
                continue;
            }

            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(productId);
            movementRequest.setMovementType(delta > 0 ? StockMovementType.OUT : StockMovementType.IN);
            movementRequest.setQuantity(Math.abs(delta));
            movementRequest.setReferenceNo(order.getOrderNumber());
            movementRequest.setNote("Sales order update " + order.getOrderNumber());
            stockMovementService.create(movementRequest);
        }
    }

    private void applyStockOut(SalesOrder order) {
        for (SalesOrderItems item : order.getItems()) {
            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(item.getProduct().getId());
            movementRequest.setMovementType(StockMovementType.OUT);
            movementRequest.setQuantity(item.getQuantity());
            movementRequest.setReferenceNo(order.getOrderNumber());
            movementRequest.setNote("Sales order " + order.getOrderNumber());
            stockMovementService.create(movementRequest);
        }
    }

    private void applyStockReturnOnCancel(SalesOrder order) {
        for (SalesOrderItems item : order.getItems()) {
            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(item.getProduct().getId());
            movementRequest.setMovementType(StockMovementType.IN);
            movementRequest.setQuantity(item.getQuantity());
            movementRequest.setReferenceNo(order.getOrderNumber());
            movementRequest.setNote("Cancelled sales order " + order.getOrderNumber());
            stockMovementService.create(movementRequest);
        }
    }
}


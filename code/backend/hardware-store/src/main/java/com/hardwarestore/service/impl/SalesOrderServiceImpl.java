package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Customer;
import com.hardwarestore.domain.entity.InventoryStock;
import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.SalesOrder;
import com.hardwarestore.domain.entity.SalesOrder.SalesOrderStatus;
import com.hardwarestore.domain.entity.SalesOrderItems;
import com.hardwarestore.domain.entity.StockMovementType;
import com.hardwarestore.dto.request.SalesOrderItemRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        Map<Long, Product> productsById = resolveProducts(request);
        SalesOrder order = salesOrderMapper.toEntity(request, customer, productsById);
        validateStockAvailability(order);

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

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        Map<Long, Product> productsById = resolveProducts(request);
        salesOrderMapper.updatePendingOrder(order, request, customer, productsById);
        validateStockAvailability(order);

        SalesOrder savedOrder = salesOrderRepository.save(order);
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

        switch (status) {
            case PENDING -> {
                if (order.getStatus() != SalesOrderStatus.PENDING) {
                    throw new InvalidSalesOrderStateException(
                            "Order id " + id + " is already in status " + order.getStatus());
                }
                order.setStatus(SalesOrderStatus.PENDING);
            }
            case CONFIRMED -> {
                if (order.getStatus() == SalesOrderStatus.CANCELLED || order.getStatus() == SalesOrderStatus.COMPLETED) {
                    throw new InvalidSalesOrderStateException(
                            "Cannot confirm order id " + id + " from status " + order.getStatus());
                }
                order.confirm();
            }
            case SHIPPED -> {
                if (order.getStatus() != SalesOrderStatus.CONFIRMED) {
                    throw new InvalidSalesOrderStateException(
                            "Only confirmed orders can be marked as shipped; order id " + id
                                    + " has status " + order.getStatus());
                }
                order.setStatus(SalesOrderStatus.SHIPPED);
            }
            case COMPLETED -> {
                if (order.getStatus() != SalesOrderStatus.SHIPPED && order.getStatus() != SalesOrderStatus.CONFIRMED) {
                    throw new InvalidSalesOrderStateException(
                            "Only shipped or confirmed orders can be completed; order id " + id
                                    + " has status " + order.getStatus());
                }
                order.complete();
            }
            case CANCELLED -> {
                if (order.getStatus() == SalesOrderStatus.COMPLETED) {
                    throw new InvalidSalesOrderStateException(
                            "Completed order id " + id + " cannot be cancelled.");
                }
                order.cancel();
            }
            default -> throw new IllegalArgumentException("Unsupported sales order status: " + status);
        }

        SalesOrder savedOrder = salesOrderRepository.save(order);
        return salesOrderMapper.toResponse(savedOrder);
    }

    private Map<Long, Product> resolveProducts(SalesOrderRequest request) {
        Map<Long, Product> productsById = new HashMap<>();
        for (SalesOrderItemRequest itemRequest : request.getItems()) {
            Long productId = itemRequest.getProductId();
            if (!productsById.containsKey(productId)) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product not found with id: " + productId));
                productsById.put(productId, product);
            }
        }
        return productsById;
    }

    private void validateStockAvailability(SalesOrder order) {
        for (SalesOrderItems item : order.getItems()) {
            InventoryStock inventory = inventoryStockRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product id: " + item.getProduct().getId()));

            int available = inventory.getAvailableQuantity();
            if (available < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product id: " + item.getProduct().getId()
                                + ". Available: " + available + ", requested: " + item.getQuantity());
            }
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
}

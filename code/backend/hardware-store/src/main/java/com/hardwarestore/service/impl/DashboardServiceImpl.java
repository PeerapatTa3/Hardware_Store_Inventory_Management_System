package com.hardwarestore.service.impl;

import com.hardwarestore.dto.response.DashboardSummaryResponse;
import com.hardwarestore.dto.response.SalesOrderResponse;
import com.hardwarestore.mapper.SalesOrderMapper;
import com.hardwarestore.repository.*;
import com.hardwarestore.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final com.hardwarestore.mapper.InventoryStockMapper inventoryStockMapper;
    private final com.hardwarestore.mapper.StockMovementMapper stockMovementMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long products = productRepository.count();
        long stockUnits = inventoryStockRepository.sumTotalStockUnits();
        long lowStock = inventoryStockRepository.countLowStockItems();
        long purchases = purchaseOrderRepository.count();
        long orders = salesOrderRepository.count();
        long suppliers = supplierRepository.count();
        long customers = customerRepository.count();

        List<SalesOrderResponse> recentOrders = salesOrderRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(salesOrderMapper::toResponse)
                .collect(Collectors.toList());

        List<com.hardwarestore.dto.response.InventoryStockResponse> lowStockItems = inventoryStockRepository.findLowStockItems()
                .stream()
                .map(inventoryStockMapper::toResponse)
                .collect(Collectors.toList());

        List<com.hardwarestore.dto.response.StockMovementResponse> pendingApprovals = stockMovementRepository.findByStatus(com.hardwarestore.domain.enums.StockMovementStatus.PENDING)
                .stream()
                .map(stockMovementMapper::toResponse)
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .products(products)
                .stockUnits(stockUnits)
                .lowStock(lowStock)
                .purchases(purchases)
                .orders(orders)
                .suppliers(suppliers)
                .customers(customers)
                .recentOrders(recentOrders)
                .lowStockItems(lowStockItems)
                .pendingApprovals(pendingApprovals)
                .build();
    }
}
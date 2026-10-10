package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.enums.PurchaseOrderStatus;
import com.hardwarestore.domain.enums.StockMovementType;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.request.ReceivePurchaseRequest;
import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import com.hardwarestore.exception.InvalidPurchaseStateException;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.PurchaseOrderMapper;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.PurchaseOrderRepository;
import com.hardwarestore.repository.SupplierRepository;
import com.hardwarestore.service.PurchaseOrderService;
import com.hardwarestore.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final StockMovementService stockMovementService;

    @Override
    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + request.getSupplierId()));

        Map<Long, Product> productsById = resolveProducts(request);
        PurchaseOrder order = purchaseOrderMapper.toEntity(
                request, "PO-" + UUID.randomUUID(), supplier, productsById);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse findById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
        return purchaseOrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new InvalidPurchaseStateException(
                    "Only pending purchases can be edited; purchase id " + id
                            + " has status " + order.getStatus());
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + request.getSupplierId()));
        Map<Long, Product> productsById = resolveProducts(request);
        purchaseOrderMapper.updatePendingOrder(order, request, supplier, productsById);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(order));
    }

    
    @Override
    @Transactional
    public PurchaseOrderResponse approve(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
        if (order.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new InvalidPurchaseStateException("Only PENDING purchases can be approved.");
        }
        order.setStatus(PurchaseOrderStatus.APPROVED);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(order));
    }


    @Override
    @Transactional
    public PurchaseOrderResponse receive(Long id, ReceivePurchaseRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));
        
        if (order.getStatus() != PurchaseOrderStatus.APPROVED && order.getStatus() != PurchaseOrderStatus.PARTIAL) {
            throw new InvalidPurchaseStateException(
                    "Only APPROVED or PARTIAL purchases can be received; purchase id " + id
                            + " has status " + order.getStatus());
        }

        boolean allFullyReceived = true;

        for (var reqItem : request.getItems()) {
            var itemOpt = order.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(reqItem.getProductId()))
                    .findFirst();
            
            if (itemOpt.isPresent()) {
                var item = itemOpt.get();
                if (reqItem.getReceivedQuantity() > 0) {
                    item.setReceivedQuantity(item.getReceivedQuantity() + reqItem.getReceivedQuantity());
                    
                    StockMovementRequest movementRequest = new StockMovementRequest();
                    movementRequest.setProductId(item.getProduct().getId());
                    movementRequest.setMovementType(StockMovementType.IN);
                    movementRequest.setQuantity(reqItem.getReceivedQuantity());
                    movementRequest.setReferenceNo(order.getPurchaseNumber());
                    movementRequest.setNote("Received purchase " + order.getPurchaseNumber());
                    stockMovementService.create(movementRequest);
                }
                if (item.getReceivedQuantity() < item.getQuantity()) {
                    allFullyReceived = false;
                }
            }
        }

        if (allFullyReceived) {
            order.setStatus(PurchaseOrderStatus.COMPLETED);
        } else {
            order.setStatus(PurchaseOrderStatus.PARTIAL);
        }
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(order));
    }


    private Map<Long, Product> resolveProducts(PurchaseOrderRequest request) {
        Map<Long, Product> productsById = new HashMap<>();
        for (var itemRequest : request.getItems()) {
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
}

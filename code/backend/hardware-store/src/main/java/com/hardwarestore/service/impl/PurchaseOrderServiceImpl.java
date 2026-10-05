package com.hardwarestore.service.impl;

import com.hardwarestore.domain.entity.Product;
import com.hardwarestore.domain.entity.PurchaseOrder;
import com.hardwarestore.domain.entity.Supplier;
import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import com.hardwarestore.exception.ResourceNotFoundException;
import com.hardwarestore.mapper.PurchaseOrderMapper;
import com.hardwarestore.repository.ProductRepository;
import com.hardwarestore.repository.PurchaseOrderRepository;
import com.hardwarestore.repository.SupplierRepository;
import com.hardwarestore.service.PurchaseOrderService;
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

    @Override
    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + request.getSupplierId()));

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
}

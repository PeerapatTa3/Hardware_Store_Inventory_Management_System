package com.hardwarestore.controller.api;

import com.hardwarestore.dto.request.PurchaseOrderRequest;
import com.hardwarestore.dto.request.ReceivePurchaseRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import com.hardwarestore.dto.response.PurchaseOrderResponse;
import com.hardwarestore.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.create(request);
    }

    @GetMapping
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderService.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse findById(@PathVariable Long id) {
        return purchaseOrderService.findById(id);
    }

    @PutMapping("/{id}")
    public PurchaseOrderResponse update(
            @PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.update(id, request);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public PurchaseOrderResponse approve(@PathVariable Long id) {
        return purchaseOrderService.approve(id);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receive(@PathVariable Long id, @Valid @RequestBody ReceivePurchaseRequest request) {
        return purchaseOrderService.receive(id, request);
    }
}

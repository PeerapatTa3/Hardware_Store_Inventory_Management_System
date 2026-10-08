package com.hardwarestore.controller.api;

import com.hardwarestore.dto.request.SalesOrderRequest;
import com.hardwarestore.dto.request.SalesOrderStatusRequest;
import com.hardwarestore.dto.response.SalesOrderResponse;
import com.hardwarestore.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderResponse create(@Valid @RequestBody SalesOrderRequest request) {
        return salesOrderService.create(request);
    }

    @GetMapping
    public List<SalesOrderResponse> findAll() {
        return salesOrderService.findAll();
    }

    @GetMapping("/{id}")
    public SalesOrderResponse findById(@PathVariable Long id) {
        return salesOrderService.findById(id);
    }

    @PutMapping("/{id}")
    public SalesOrderResponse update(@PathVariable Long id, @Valid @RequestBody SalesOrderRequest request) {
        return salesOrderService.update(id, request);
    }

    @PostMapping("/{id}/status")
    public SalesOrderResponse updateStatus(
            @PathVariable Long id, @Valid @RequestBody SalesOrderStatusRequest request) {
        return salesOrderService.updateStatus(id, request.getStatus());
    }
}

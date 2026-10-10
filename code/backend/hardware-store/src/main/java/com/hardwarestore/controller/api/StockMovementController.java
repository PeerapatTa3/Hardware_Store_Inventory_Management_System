package com.hardwarestore.controller.api;

import com.hardwarestore.dto.request.StockMovementRequest;
import com.hardwarestore.dto.response.StockMovementResponse;
import com.hardwarestore.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse create(@Valid @RequestBody StockMovementRequest request) {
        return stockMovementService.create(request);
    }

    @GetMapping
    public List<StockMovementResponse> findAll() {
        return stockMovementService.findAll();
    }

    @GetMapping("/products/{productId}")
    public List<StockMovementResponse> findByProductId(@PathVariable Long productId) {
        return stockMovementService.findByProductId(productId);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<StockMovementResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(stockMovementService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<StockMovementResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(stockMovementService.reject(id));
    }
}

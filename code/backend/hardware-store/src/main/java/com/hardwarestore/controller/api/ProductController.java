package com.hardwarestore.controller.api;

import com.hardwarestore.dto.request.ProductRequest;
import com.hardwarestore.dto.response.ProductResponse;
import com.hardwarestore.dto.response.ProductAdminResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import com.hardwarestore.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('OWNER') or hasRole('STOCK_MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductAdminResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public Page<ProductResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return productService.findAll(page, size, sortBy, direction, keyword, categoryId);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('STOCK_MANAGER')")
    @GetMapping("/admin")
    public Page<ProductAdminResponse> findAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return productService.findAllAdmin(page, size, sortBy, direction, keyword, categoryId);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('STOCK_MANAGER')")
    @GetMapping("/admin/{id}")
    public ProductAdminResponse findByIdAdmin(@PathVariable Long id) {
        return productService.findByIdAdmin(id);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('STOCK_MANAGER')")
    @PutMapping("/{id}")
    public ProductAdminResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}

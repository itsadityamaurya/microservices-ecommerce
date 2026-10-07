package com.aditya.product.controller;

import com.aditya.product.dto.ProductRequest;
import com.aditya.product.dto.ProductResponse;
import com.aditya.product.dto.ReserveRequest;
import com.aditya.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return service.create(request);
    }

    @GetMapping
    @Operation(summary = "List products")
    public List<ProductResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product")
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve stock (used by order-service)")
    public ProductResponse reserve(@PathVariable Long id, @Valid @RequestBody ReserveRequest request) {
        return service.reserve(id, request.quantity());
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release previously reserved stock (compensation)")
    public ProductResponse release(@PathVariable Long id, @Valid @RequestBody ReserveRequest request) {
        return service.release(id, request.quantity());
    }
}

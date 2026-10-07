package com.aditya.order.controller;

import com.aditya.order.dto.OrderRequest;
import com.aditya.order.dto.OrderResponse;
import com.aditya.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order (reserves stock in product-service)")
    public OrderResponse place(@Valid @RequestBody OrderRequest request) {
        return service.placeOrder(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order")
    public OrderResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List orders")
    public List<OrderResponse> list() {
        return service.findAll();
    }
}

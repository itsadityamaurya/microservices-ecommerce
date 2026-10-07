package com.aditya.order.service;

import com.aditya.order.client.ProductClient;
import com.aditya.order.dto.OrderRequest;
import com.aditya.order.dto.OrderResponse;
import com.aditya.order.dto.ProductInfo;
import com.aditya.order.exception.OrderNotFoundException;
import com.aditya.order.model.Order;
import com.aditya.order.model.OrderStatus;
import com.aditya.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductClient productClient;

    /**
     * Reserve stock first, then persist the order. If saving fails, stock is released
     * (simple compensation, a lightweight version of the saga pattern).
     */
    public OrderResponse placeOrder(OrderRequest request) {
        ProductInfo product = productClient.reserve(request.productId(), request.quantity());
        try {
            Order order = Order.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .quantity(request.quantity())
                    .totalPrice(product.price().multiply(BigDecimal.valueOf(request.quantity())))
                    .status(OrderStatus.CONFIRMED)
                    .build();
            return OrderResponse.from(repository.save(order));
        } catch (RuntimeException e) {
            productClient.release(request.productId(), request.quantity());
            throw e;
        }
    }

    public OrderResponse get(Long id) {
        return OrderResponse.from(repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id)));
    }

    public List<OrderResponse> findAll() {
        return repository.findAll().stream().map(OrderResponse::from).toList();
    }
}

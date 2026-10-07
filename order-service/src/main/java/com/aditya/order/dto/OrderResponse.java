package com.aditya.order.dto;

import com.aditya.order.model.Order;
import com.aditya.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(Long id, Long productId, String productName, int quantity,
                            BigDecimal totalPrice, OrderStatus status, Instant createdAt) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getProductId(), o.getProductName(), o.getQuantity(),
                o.getTotalPrice(), o.getStatus(), o.getCreatedAt());
    }
}

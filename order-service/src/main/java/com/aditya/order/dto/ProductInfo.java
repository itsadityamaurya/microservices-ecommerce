package com.aditya.order.dto;

import java.math.BigDecimal;

/** Subset of product-service's response that order-service cares about. */
public record ProductInfo(Long id, String name, BigDecimal price, int stock) {
}

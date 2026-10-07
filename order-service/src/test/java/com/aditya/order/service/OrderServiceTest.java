package com.aditya.order.service;

import com.aditya.order.client.ProductClient;
import com.aditya.order.dto.OrderRequest;
import com.aditya.order.dto.OrderResponse;
import com.aditya.order.dto.ProductInfo;
import com.aditya.order.exception.InsufficientStockException;
import com.aditya.order.exception.OrderNotFoundException;
import com.aditya.order.model.Order;
import com.aditya.order.model.OrderStatus;
import com.aditya.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService service;

    @Test
    void placeOrderCalculatesTotalAndConfirms() {
        when(productClient.reserve(1L, 3)).thenReturn(new ProductInfo(1L, "Mouse", new BigDecimal("999.00"), 7));
        when(repository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });
        OrderResponse r = service.placeOrder(new OrderRequest(1L, 3));
        assertEquals(new BigDecimal("2997.00"), r.totalPrice());
        assertEquals(OrderStatus.CONFIRMED, r.status());
        assertEquals("Mouse", r.productName());
    }

    @Test
    void stockFailureDoesNotSaveOrder() {
        when(productClient.reserve(1L, 99)).thenThrow(new InsufficientStockException("Not enough stock"));
        assertThrows(InsufficientStockException.class, () -> service.placeOrder(new OrderRequest(1L, 99)));
        verify(repository, never()).save(any());
    }

    @Test
    void stockIsReleasedIfSavingFails() {
        when(productClient.reserve(1L, 2)).thenReturn(new ProductInfo(1L, "Mouse", new BigDecimal("999.00"), 8));
        when(repository.save(any(Order.class))).thenThrow(new RuntimeException("db down"));
        assertThrows(RuntimeException.class, () -> service.placeOrder(new OrderRequest(1L, 2)));
        verify(productClient).release(1L, 2);
    }

    @Test
    void getThrowsWhenOrderMissing() {
        when(repository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> service.get(5L));
    }
}

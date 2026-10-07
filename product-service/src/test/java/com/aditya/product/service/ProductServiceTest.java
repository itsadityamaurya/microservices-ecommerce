package com.aditya.product.service;

import com.aditya.product.dto.ProductResponse;
import com.aditya.product.exception.InsufficientStockException;
import com.aditya.product.exception.ProductNotFoundException;
import com.aditya.product.model.Product;
import com.aditya.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product product(int stock) {
        return Product.builder().id(1L).name("Mouse").price(new BigDecimal("999.00")).stock(stock).build();
    }

    @Test
    void reserveDecrementsStock() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(product(10)));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        ProductResponse r = service.reserve(1L, 3);
        assertEquals(7, r.stock());
    }

    @Test
    void reserveFailsWhenStockIsLow() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(product(2)));
        assertThrows(InsufficientStockException.class, () -> service.reserve(1L, 5));
    }

    @Test
    void reserveFailsWhenProductMissing() {
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> service.reserve(5L, 1));
    }

    @Test
    void releaseRestoresStock() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(product(4)));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(6, service.release(1L, 2).stock());
    }
}

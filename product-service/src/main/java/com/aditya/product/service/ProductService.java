package com.aditya.product.service;

import com.aditya.product.dto.ProductRequest;
import com.aditya.product.dto.ProductResponse;
import com.aditya.product.exception.InsufficientStockException;
import com.aditya.product.exception.ProductNotFoundException;
import com.aditya.product.model.Product;
import com.aditya.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional
    public ProductResponse create(ProductRequest r) {
        Product p = Product.builder().name(r.name().trim()).price(r.price()).stock(r.stock()).build();
        return ProductResponse.from(repository.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findAll().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id)));
    }

    /** Atomically checks stock and decrements it. Returns the product so the caller gets name and price in one call. */
    @Transactional
    public ProductResponse reserve(Long id, int quantity) {
        Product p = repository.findByIdForUpdate(id).orElseThrow(() -> new ProductNotFoundException(id));
        if (p.getStock() < quantity) {
            throw new InsufficientStockException(id, quantity, p.getStock());
        }
        p.setStock(p.getStock() - quantity);
        return ProductResponse.from(repository.save(p));
    }

    /** Compensation: returns stock if the order could not be completed. */
    @Transactional
    public ProductResponse release(Long id, int quantity) {
        Product p = repository.findByIdForUpdate(id).orElseThrow(() -> new ProductNotFoundException(id));
        p.setStock(p.getStock() + quantity);
        return ProductResponse.from(repository.save(p));
    }
}

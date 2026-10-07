package com.aditya.product.config;

import com.aditya.product.model.Product;
import com.aditya.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    Product.builder().name("Mechanical Keyboard").price(new BigDecimal("3499.00")).stock(25).build(),
                    Product.builder().name("Wireless Mouse").price(new BigDecimal("999.00")).stock(50).build(),
                    Product.builder().name("27-inch Monitor").price(new BigDecimal("15999.00")).stock(10).build()));
        }
    }
}

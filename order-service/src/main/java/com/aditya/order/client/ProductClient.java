package com.aditya.order.client;

import com.aditya.order.dto.ProductInfo;
import com.aditya.order.exception.InsufficientStockException;
import com.aditya.order.exception.ProductNotFoundException;
import com.aditya.order.exception.ProductServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

/** Thin HTTP client for product-service with timeouts and error translation. */
@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product-service.url}") String baseUrl,
                         @Value("${product-service.connect-timeout-ms}") int connectTimeoutMs,
                         @Value("${product-service.read-timeout-ms}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public ProductInfo reserve(Long productId, int quantity) {
        try {
            return restClient.post()
                    .uri("/api/products/{id}/reserve", productId)
                    .body(Map.of("quantity", quantity))
                    .retrieve()
                    .body(ProductInfo.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ProductNotFoundException(productId);
            }
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new InsufficientStockException("Not enough stock for product " + productId);
            }
            throw new ProductServiceUnavailableException("Product service rejected the request");
        } catch (RestClientException e) {
            throw new ProductServiceUnavailableException("Product service is currently unavailable, please retry");
        }
    }

    public void release(Long productId, int quantity) {
        try {
            restClient.post()
                    .uri("/api/products/{id}/release", productId)
                    .body(Map.of("quantity", quantity))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Best effort compensation; in a real system push this to a retry queue.
        }
    }
}

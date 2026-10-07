# Microservices E-commerce (Product + Order)

Two independent **Spring Boot microservices** that talk over REST, each with its own database.
Shows service-to-service communication, failure handling, concurrency-safe stock updates and Docker Compose deployment.

```
 client ──► order-service (8082) ──HTTP──► product-service (8081)
                 │                               │
              orderdb                        productdb      (MySQL, one DB per service)
```

## What it demonstrates
- **Database per service**: no shared tables, services communicate only through APIs
- **Safe inventory**: `reserve` uses a pessimistic row lock (`SELECT ... FOR UPDATE`) so concurrent orders cannot oversell
- **Resilient HTTP client**: `RestClient` with connect/read timeouts, errors translated into 404 / 409 / 503
- **Compensation**: if saving the order fails after stock was reserved, stock is released (lightweight saga)
- Validation, global exception handlers, Swagger UI per service, unit tests, Dockerfiles, Compose

## Run locally (H2, no database needed)
```bash
cd product-service && mvn spring-boot:run     # terminal 1  -> http://localhost:8081/swagger-ui.html
cd order-service   && mvn spring-boot:run     # terminal 2  -> http://localhost:8082/swagger-ui.html
```

## Run with Docker Compose (MySQL)
```bash
docker compose up --build
```

## Try it
```bash
curl localhost:8081/api/products                      # 3 seeded products
curl -X POST localhost:8082/api/orders -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
curl localhost:8081/api/products/1                    # stock reduced by 2
curl -X POST localhost:8082/api/orders -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":999}'                 # 409 insufficient stock
```
Stop product-service and place an order: order-service returns a clean **503**, not a stack trace.

## Tests
```bash
cd product-service && mvn test
cd order-service   && mvn test
```

## Next steps (good talking points with clients)
API Gateway (Spring Cloud Gateway), service discovery, Resilience4j circuit breaker and retries,
Kafka events instead of synchronous calls, JWT between services (see project 1).

# Payment Processing API

A Java 21 and Spring Boot REST API demonstrating core payment-processing concepts.

> **Disclaimer:** This is a personal demonstration project inspired by real-world payment systems. It is not a production banking system and must not be used to process real payments.

## Features

* Customer registration and customer management
* JWT-based authentication and authorization
* Payment creation and status tracking
* Idempotency keys for payment requests
* Payment attempts and retry scheduling
* Mock payment provider
* PostgreSQL persistence
* Redis caching
* Audit logging and structured application logs
* Correlation IDs for request tracing
* Spring Boot Actuator health endpoints
* Centralized API exception handling

## Technology Stack

* Java 21
* Spring Boot
* Spring Security and JWT
* Spring Data JPA / Hibernate
* PostgreSQL
* Redis
* Maven
* Docker Compose
* JUnit 5 and Mockito

## Prerequisites

* Java 21
* Docker and Docker Compose
* Maven, or the included Maven Wrapper

## Configuration

Configure the required environment variables before running the application:

* `DATABASE_URL`
* `DATABASE_USERNAME`
* `DATABASE_PASSWORD`
* `REDIS_HOST`
* `REDIS_PORT`
* `REDIS_PASSWORD`
* `JWT_SECRET`

`JWT_SECRET` must be a Base64-encoded secret containing at least 32 random bytes.

Never commit real credentials, JWT secrets, or production configuration values.

## Run Locally

1. Start PostgreSQL and Redis:

   ```bash
   docker compose up -d
   ```

2. Configure the environment variables in your IDE or shell.

3. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

## Run Tests

```bash
./mvnw test
```

## Health Endpoint

```text
GET /actuator/health
```

## API Overview

```text
POST /api/v1/auth/register
POST /api/v1/auth/login

POST /api/v1/customers
GET  /api/v1/customers/{id}

POST /api/v1/payments
GET  /api/v1/payments/{id}
GET  /api/v1/payments?customerId={customerId}

POST /api/v1/payments/{paymentId}/attempts
POST /api/v1/payments/{paymentId}/retry
GET  /api/v1/payments/{paymentId}/attempts

POST /api/v1/payment-attempts/{attemptId}/success
POST /api/v1/payment-attempts/{attemptId}/fail
```

Protected endpoints require a bearer token:

```text
Authorization: Bearer <access-token>
```

Payment attempt endpoints are intended for demonstrating payment state transitions and retry behavior. The provider is mocked; no real funds are moved.

## Observability

* Structured JSON logs
* `X-Correlation-ID` response header
* Audit events for selected operations
* Actuator health and info endpoints

## Limitations

This project is a portfolio demonstration. A real payment platform would require additional controls, including provider-specific idempotency, concurrency-safe payment processing, database migrations, secrets management, operational monitoring, and security review.

## License

Add a license if you intend to distribute or reuse this project under specific terms.

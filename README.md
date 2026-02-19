# Product API - Zest India IT Pvt Ltd

A RESTful API for Product CRUD operations built with Java 17 and Spring Boot.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Data JPA** (Hibernate) with **PostgreSQL**
- **Spring Security** with JWT & Refresh Token rotation
- **Jakarta Validation** for input validation
- **Swagger/OpenAPI** documentation
- **JUnit 5 & Mockito** for testing (H2 for integration tests)
- **Docker & Docker Compose**

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for containerized setup)

### Run with Docker Compose (Recommended)

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

### Run Locally

1. Start a PostgreSQL instance (port 5432, db: `product_db`, user/pass: `postgres/postgres`)
2. Build and run:

```bash
mvn clean install
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

---

## API Documentation

Once running, access Swagger UI at:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Default Users

| Username | Password  | Roles              |
|----------|-----------|--------------------|
| admin    | admin123  | ROLE_ADMIN, ROLE_USER |
| user     | user123   | ROLE_USER          |

---

## API Endpoints

### Authentication

| Method | Endpoint               | Description               | Auth Required |
|--------|------------------------|---------------------------|---------------|
| POST   | `/api/v1/auth/register`| Register new user         | No            |
| POST   | `/api/v1/auth/login`   | Login & get JWT tokens    | No            |
| POST   | `/api/v1/auth/refresh` | Refresh access token      | No            |
| POST   | `/api/v1/auth/logout`  | Revoke refresh tokens     | No            |

### Products

| Method | Endpoint                      | Description              | Auth Required | Roles           |
|--------|-------------------------------|--------------------------|---------------|-----------------|
| GET    | `/api/v1/products`            | List products (paginated)| Yes           | Any             |
| GET    | `/api/v1/products/{id}`       | Get product by ID        | Yes           | Any             |
| POST   | `/api/v1/products`            | Create product           | Yes           | USER, ADMIN     |
| PUT    | `/api/v1/products/{id}`       | Update product           | Yes           | USER, ADMIN     |
| DELETE | `/api/v1/products/{id}`       | Delete product           | Yes           | ADMIN only      |
| GET    | `/api/v1/products/{id}/items` | Get product items        | Yes           | Any             |
| POST   | `/api/v1/products/{id}/items` | Add item to product      | Yes           | USER, ADMIN     |

### Pagination Parameters

| Parameter | Default | Description          |
|-----------|---------|----------------------|
| page      | 0       | Page number (0-based)|
| size      | 10      | Page size            |
| sortBy    | id      | Sort field           |
| sortDir   | asc     | Sort direction       |

---

## Usage Examples

### 1. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "550e8400-e29b...",
  "tokenType": "Bearer",
  "username": "admin"
}
```

### 2. Create Product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"productName": "My Product"}'
```

### 3. Get Products (Paginated)

```bash
curl "http://localhost:8080/api/v1/products?page=0&size=10&sortBy=productName&sortDir=asc" \
  -H "Authorization: Bearer <access_token>"
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refresh_token>"}'
```

---

## Database Schema

```sql
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    modified_on TIMESTAMP
);

CREATE TABLE item (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id)
);
```

---

## Project Structure

```
src/main/java/com/zest/product/
├── ProductApiApplication.java
├── config/          # Security, OpenAPI, async, data seeder configs
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Request DTOs with validation
│   └── response/    # Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions & global handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, filter, entry point, user details
└── service/
    └── impl/        # Service implementations
```

---

## Security Features

- **JWT Access Tokens** (15 min expiry)
- **Refresh Token Rotation** (7 day expiry, revoked after use)
- **Role-based Authorization** (USER, ADMIN)
- **BCrypt Password Hashing**
- **CORS Configuration**
- **Stateless Session Management**

---

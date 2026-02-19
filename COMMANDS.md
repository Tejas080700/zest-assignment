# Product API - Project Documentation

## Quick Start Commands

### Prerequisites Installation (macOS)

```bash
# Install Java 17
brew install openjdk@17
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Install Maven
brew install maven

# Install & start PostgreSQL
brew install postgresql@16
brew services start postgresql@16

# Add PostgreSQL to PATH (add to ~/.zshrc for persistence)
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"

# Create database user and database
psql -U $(whoami) -d postgres -c "CREATE USER postgres WITH SUPERUSER PASSWORD 'postgres';"
psql -U postgres -d postgres -c "CREATE DATABASE product_db;"
```

### Run the Application

```bash
# Build the project (skip tests for faster startup)
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/product-api-1.0.0.jar
```

### Run with Docker

```bash
# Build and start everything (app + PostgreSQL)
docker-compose up --build

# Stop
docker-compose down

# Stop and remove data
docker-compose down -v
```

### Run Tests

```bash
# Run all tests (unit + integration)
mvn test

# Run only unit tests
mvn test -Dtest="*ServiceTest,*ControllerTest"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"
```

### Run the API Test Script

```bash
chmod +x api-tests.sh
./api-tests.sh
```

---

## Individual cURL Commands

All commands below assume the app is running at `http://localhost:8080`.

### 1. Health Check

```bash
curl -s http://localhost:8080/actuator/health
```

### 2. Register a New User

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "pass123"
  }'
```

### 3. Login (Get JWT Token)

```bash
# Login as admin
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Login as regular user
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123"
  }'
```

**Save the token for subsequent requests:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
```

### 4. Create a Product

```bash
curl -s -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productName": "Laptop"}'
```

### 5. Get All Products (Paginated)

```bash
# Default pagination
curl -s http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN"

# Custom pagination & sorting
curl -s "http://localhost:8080/api/v1/products?page=0&size=5&sortBy=productName&sortDir=desc" \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Get Product by ID

```bash
curl -s http://localhost:8080/api/v1/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Update a Product

```bash
curl -s -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productName": "Gaming Laptop"}'
```

### 8. Delete a Product (ADMIN only)

```bash
curl -s -X DELETE http://localhost:8080/api/v1/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 9. Add Item to Product

```bash
curl -s -X POST http://localhost:8080/api/v1/products/1/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 10}'
```

### 10. Get Items for a Product

```bash
curl -s http://localhost:8080/api/v1/products/1/items \
  -H "Authorization: Bearer $TOKEN"
```

### 11. Refresh Token

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<your-refresh-token>"}'
```

### 12. Logout (Revoke Tokens)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<your-refresh-token>"}'
```

---

## Expected Error Responses

### Validation Error (400)
```bash
curl -s -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productName": ""}'
```
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [{"field": "productName", "message": "Product name is required"}]
}
```

### Not Found (404)
```bash
curl -s http://localhost:8080/api/v1/products/9999 \
  -H "Authorization: Bearer $TOKEN"
```
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: '9999'"
}
```

### Unauthorized (401)
```bash
curl -s http://localhost:8080/api/v1/products
```
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please provide a valid JWT token."
}
```

### Forbidden (403) - User trying to delete
```bash
# Login as user (not admin)
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -s -X DELETE http://localhost:8080/api/v1/products/1 \
  -H "Authorization: Bearer $USER_TOKEN"
```

---

## Swagger UI

Open in browser: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Use the "Authorize" button and paste your JWT token to test endpoints interactively.

---

## PostgreSQL Management

```bash
# Start PostgreSQL
brew services start postgresql@16

# Stop PostgreSQL
brew services stop postgresql@16

# Connect to the database
psql -U postgres -d product_db

# View tables
psql -U postgres -d product_db -c "\dt"

# View all products
psql -U postgres -d product_db -c "SELECT * FROM product;"

# View all items
psql -U postgres -d product_db -c "SELECT * FROM item;"

# Drop and recreate database (reset)
psql -U postgres -d postgres -c "DROP DATABASE product_db;"
psql -U postgres -d postgres -c "CREATE DATABASE product_db;"
```

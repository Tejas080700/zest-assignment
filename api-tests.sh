#!/bin/bash
# ============================================================================
# Product API - cURL Test Script
# Zest India IT Pvt Ltd
# ============================================================================
# Usage:
#   chmod +x api-tests.sh
#   ./api-tests.sh
#
# Prerequisites:
#   - Application running on http://localhost:8080
#   - curl and python3 installed
# ============================================================================

BASE_URL="http://localhost:8080/api/v1"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

print_header() {
  echo ""
  echo -e "${BLUE}============================================================================${NC}"
  echo -e "${BLUE}  $1${NC}"
  echo -e "${BLUE}============================================================================${NC}"
}

print_step() {
  echo ""
  echo -e "${YELLOW}>>> $1${NC}"
}

print_success() {
  echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
  echo -e "${RED}✗ $1${NC}"
}

pretty_print() {
  python3 -m json.tool 2>/dev/null || cat
}

# ============================================================================
# 0. HEALTH CHECK
# ============================================================================
print_header "HEALTH CHECK"

print_step "GET /actuator/health"
curl -s http://localhost:8080/actuator/health | pretty_print
echo ""ear
# ============================================================================
# 1. AUTHENTICATION - Register
# ============================================================================
print_header "AUTHENTICATION"

print_step "POST /api/v1/auth/register - Register a new user"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/auth/register" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }' | pretty_print

# ============================================================================
# 2. AUTHENTICATION - Login as Admin
# ============================================================================
print_step "POST /api/v1/auth/login - Login as admin"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "$LOGIN_RESPONSE" | pretty_print

# Extract tokens
ADMIN_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null)
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['refreshToken'])" 2>/dev/null)

if [ -z "$ADMIN_TOKEN" ]; then
  print_error "Failed to get admin token. Is the server running?"
  exit 1
fi
print_success "Admin token obtained"

# ============================================================================
# 3. AUTHENTICATION - Login as User
# ============================================================================
print_step "POST /api/v1/auth/login - Login as regular user"
USER_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{
    "username": "user",
    "password": "user123"
  }')

echo "$USER_LOGIN_RESPONSE" | pretty_print

USER_TOKEN=$(echo "$USER_LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null)
print_success "User token obtained"

# ============================================================================
# 4. PRODUCTS - Create
# ============================================================================
print_header "PRODUCT CRUD OPERATIONS"

print_step "POST /api/v1/products - Create Product 1 (Laptop)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "productName": "Laptop"
  }' | pretty_print

print_step "POST /api/v1/products - Create Product 2 (Smartphone)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "productName": "Smartphone"
  }' | pretty_print

print_step "POST /api/v1/products - Create Product 3 (Tablet)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "productName": "Tablet"
  }' | pretty_print

# ============================================================================
# 5. PRODUCTS - Get All (Paginated)
# ============================================================================
print_step "GET /api/v1/products - Get all products (page 0, size 10, sorted by name ASC)"
curl -s "$BASE_URL/products?page=0&size=10&sortBy=productName&sortDir=asc" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | pretty_print

# ============================================================================
# 6. PRODUCTS - Get by ID
# ============================================================================
print_step "GET /api/v1/products/1 - Get product by ID"
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/products/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | pretty_print

# ============================================================================
# 7. PRODUCTS - Update
# ============================================================================
print_step "PUT /api/v1/products/1 - Update product name"
curl -s -w "\nHTTP Status: %{http_code}\n" -X PUT "$BASE_URL/products/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "productName": "Gaming Laptop"
  }' | pretty_print

# ============================================================================
# 8. ITEMS - Add to Product
# ============================================================================
print_header "ITEM OPERATIONS"

print_step "POST /api/v1/products/1/items - Add item (qty: 10) to product 1"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products/1/items" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "quantity": 10
  }' | pretty_print

print_step "POST /api/v1/products/1/items - Add item (qty: 25) to product 1"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products/1/items" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "quantity": 25
  }' | pretty_print

# ============================================================================
# 9. ITEMS - Get by Product
# ============================================================================
print_step "GET /api/v1/products/1/items - Get all items for product 1"
curl -s "$BASE_URL/products/1/items" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | pretty_print

# ============================================================================
# 10. VALIDATION ERRORS
# ============================================================================
print_header "ERROR HANDLING & VALIDATION"

print_step "POST /api/v1/products - Empty product name (expect 400)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "productName": ""
  }' | pretty_print

print_step "GET /api/v1/products/9999 - Non-existent product (expect 404)"
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/products/9999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | pretty_print

print_step "POST /api/v1/products/1/items - Invalid quantity (expect 400)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/products/1/items" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "$CONTENT_TYPE" \
  -d '{
    "quantity": 0
  }' | pretty_print

# ============================================================================
# 11. AUTHORIZATION - Role-based access
# ============================================================================
print_header "AUTHORIZATION (ROLE-BASED)"

print_step "DELETE /api/v1/products/3 as USER (expect 403 Forbidden)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X DELETE "$BASE_URL/products/3" \
  -H "Authorization: Bearer $USER_TOKEN" | pretty_print

print_step "DELETE /api/v1/products/3 as ADMIN (expect 200)"
curl -s -w "\nHTTP Status: %{http_code}\n" -X DELETE "$BASE_URL/products/3" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | pretty_print

print_step "GET /api/v1/products - Without token (expect 401)"
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/products" | pretty_print

# ============================================================================
# 12. TOKEN REFRESH
# ============================================================================
print_header "TOKEN REFRESH"

print_step "POST /api/v1/auth/refresh - Refresh access token"
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/auth/refresh" \
  -H "$CONTENT_TYPE" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }" | pretty_print

# ============================================================================
# 13. FINAL STATE
# ============================================================================
print_header "FINAL STATE"

print_step "GET /api/v1/products - All remaining products"
# Re-login since token may have rotated
FINAL_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "$CONTENT_TYPE" \
  -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null)

curl -s "$BASE_URL/products?page=0&size=10" \
  -H "Authorization: Bearer $FINAL_TOKEN" | pretty_print

print_header "ALL TESTS COMPLETED"
echo ""

package com.zest.product.controller;

import com.zest.product.dto.request.ItemRequest;
import com.zest.product.dto.request.ProductRequest;
import com.zest.product.dto.response.ItemResponse;
import com.zest.product.dto.response.MessageResponse;
import com.zest.product.dto.response.PagedResponse;
import com.zest.product.dto.response.ProductResponse;
import com.zest.product.service.ItemService;
import com.zest.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;
    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with pagination and sorting")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its ID")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        ProductResponse response = productService.createProduct(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product by its ID")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(productService.updateProduct(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product by its ID (ADMIN only)")
    public ResponseEntity<MessageResponse> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {

        productService.deleteProduct(id);
        return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get product items", description = "Retrieve all items belonging to a product")
    public ResponseEntity<List<ItemResponse>> getProductItems(
            @Parameter(description = "Product ID") @PathVariable Long id) {

        return ResponseEntity.ok(itemService.getItemsByProductId(id));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to product", description = "Add a new item to a product")
    public ResponseEntity<ItemResponse> addItemToProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {

        ItemResponse response = itemService.addItemToProduct(id, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

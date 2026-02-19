package com.zest.product.service;

import com.zest.product.dto.request.ProductRequest;
import com.zest.product.dto.response.PagedResponse;
import com.zest.product.dto.response.ProductResponse;

public interface ProductService {

    PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest request, String username);

    ProductResponse updateProduct(Long id, ProductRequest request, String username);

    void deleteProduct(Long id);
}

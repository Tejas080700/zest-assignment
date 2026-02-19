package com.zest.product.service;

import com.zest.product.dto.request.ProductRequest;
import com.zest.product.dto.response.PagedResponse;
import com.zest.product.dto.response.ProductResponse;
import com.zest.product.entity.Product;
import com.zest.product.exception.ResourceNotFoundException;
import com.zest.product.repository.ProductRepository;
import com.zest.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .build();

        productRequest = ProductRequest.builder()
                .productName("Test Product")
                .build();
    }

    @Test
    @DisplayName("Should return paged products")
    void getAllProducts_ShouldReturnPagedProducts() {
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> result = productService.getAllProducts(0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductName()).isEqualTo("Test Product");
        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return product by ID")
    void getProductById_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Test Product");
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void getProductById_WhenNotFound_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Should create a new product")
    void createProduct_ShouldReturnCreatedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = productService.createProduct(productRequest, "admin");

        assertThat(result.getProductName()).isEqualTo("Test Product");
        assertThat(result.getCreatedBy()).isEqualTo("admin");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update an existing product")
    void updateProduct_ShouldReturnUpdatedProduct() {
        Product updatedProduct = Product.builder()
                .id(1L)
                .productName("Updated Product")
                .createdBy("admin")
                .modifiedBy("admin")
                .createdOn(LocalDateTime.now())
                .modifiedOn(LocalDateTime.now())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductRequest updateRequest = ProductRequest.builder()
                .productName("Updated Product")
                .build();

        ProductResponse result = productService.updateProduct(1L, updateRequest, "admin");

        assertThat(result.getProductName()).isEqualTo("Updated Product");
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProduct_WhenNotFound_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, productRequest, "admin"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete product by ID")
    void deleteProduct_ShouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void deleteProduct_WhenNotFound_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

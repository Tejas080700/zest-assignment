package com.zest.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.product.dto.request.ProductRequest;
import com.zest.product.dto.response.PagedResponse;
import com.zest.product.dto.response.ProductResponse;
import com.zest.product.exception.ResourceNotFoundException;
import com.zest.product.config.SecurityConfig;
import com.zest.product.security.JwtAuthenticationEntryPoint;
import com.zest.product.security.JwtAuthenticationFilter;
import com.zest.product.security.JwtTokenProvider;
import com.zest.product.service.ItemService;
import com.zest.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    private final ProductResponse sampleProduct = ProductResponse.builder()
            .id(1L)
            .productName("Test Product")
            .createdBy("admin")
            .createdOn(LocalDateTime.now())
            .build();

    @Test
    @DisplayName("GET /api/v1/products - Should return paged products")
    @WithMockUser(roles = "USER")
    void getAllProducts_ShouldReturnPagedProducts() throws Exception {
        PagedResponse<ProductResponse> pagedResponse = PagedResponse.<ProductResponse>builder()
                .content(List.of(sampleProduct))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return product")
    @WithMockUser(roles = "USER")
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 404 when not found")
    @WithMockUser(roles = "USER")
    void getProductById_WhenNotFound_ShouldReturn404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/products - Should create product")
    @WithMockUser(username = "admin", roles = "USER")
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .productName("New Product")
                .build();

        ProductResponse createdProduct = ProductResponse.builder()
                .id(2L)
                .productName("New Product")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .build();

        when(productService.createProduct(any(ProductRequest.class), eq("admin")))
                .thenReturn(createdProduct);

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("New Product"));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 for invalid input")
    @WithMockUser(roles = "USER")
    void createProduct_WithInvalidInput_ShouldReturn400() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .productName("")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should update product")
    @WithMockUser(username = "admin", roles = "USER")
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .productName("Updated Product")
                .build();

        ProductResponse updatedProduct = ProductResponse.builder()
                .id(1L)
                .productName("Updated Product")
                .createdBy("admin")
                .modifiedBy("admin")
                .build();

        when(productService.updateProduct(eq(1L), any(ProductRequest.class), eq("admin")))
                .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/v1/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Product"));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should delete product")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldReturn200() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should return 403 for non-admin")
    @WithMockUser(roles = "USER")
    void deleteProduct_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/products - Should return 401 for unauthenticated")
    void getAllProducts_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }
}

package com.zest.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.product.dto.request.LoginRequest;
import com.zest.product.dto.request.ProductRequest;
import com.zest.product.dto.request.RegisterRequest;
import com.zest.product.dto.response.AuthResponse;
import com.zest.product.repository.ProductRepository;
import com.zest.product.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login as admin (created by DataSeeder)
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        adminToken = authResponse.getAccessToken();
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Register a new user")
    void registerUser_ShouldReturn201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .password("test123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Full product CRUD lifecycle")
    void productCrudLifecycle() throws Exception {
        // CREATE
        ProductRequest createRequest = ProductRequest.builder()
                .productName("Integration Test Product")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Integration Test Product"))
                .andExpect(jsonPath("$.createdBy").value("admin"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long productId = objectMapper.readTree(responseBody).get("id").asLong();

        // READ
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Integration Test Product"));

        // UPDATE
        ProductRequest updateRequest = ProductRequest.builder()
                .productName("Updated Integration Product")
                .build();

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Integration Product"));

        // LIST with pagination
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // DELETE
        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // VERIFY DELETED
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Product items endpoint")
    void productItemsEndpoint() throws Exception {
        // Create product
        ProductRequest createRequest = ProductRequest.builder()
                .productName("Product With Items")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long productId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        // Add item
        String itemJson = "{\"quantity\": 5}";
        mockMvc.perform(post("/api/v1/products/" + productId + "/items")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5));

        // Get items
        mockMvc.perform(get("/api/v1/products/" + productId + "/items")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].quantity").value(5));

        // Cleanup
        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Unauthenticated access should return 401")
    void unauthenticatedAccess_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Validation errors should return 400")
    void invalidProductRequest_ShouldReturn400() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .productName("")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}

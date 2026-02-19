package com.zest.product.service;

import com.zest.product.dto.request.ItemRequest;
import com.zest.product.dto.response.ItemResponse;
import com.zest.product.entity.Item;
import com.zest.product.entity.Product;
import com.zest.product.exception.ResourceNotFoundException;
import com.zest.product.repository.ItemRepository;
import com.zest.product.repository.ProductRepository;
import com.zest.product.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Product product;
    private Item item;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("admin")
                .build();

        item = Item.builder()
                .id(1L)
                .quantity(10)
                .product(product)
                .build();
    }

    @Test
    @DisplayName("Should return items by product ID")
    void getItemsByProductId_ShouldReturnItems() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByProductId(1L)).thenReturn(List.of(item));

        List<ItemResponse> result = itemService.getItemsByProductId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(10);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when product not found for items")
    void getItemsByProductId_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getItemsByProductId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should add item to product")
    void addItemToProduct_ShouldReturnCreatedItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemRequest request = ItemRequest.builder().quantity(10).build();
        ItemResponse result = itemService.addItemToProduct(1L, request);

        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getProductId()).isEqualTo(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Should throw exception when adding item to non-existent product")
    void addItemToProduct_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ItemRequest request = ItemRequest.builder().quantity(10).build();

        assertThatThrownBy(() -> itemService.addItemToProduct(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete item")
    void deleteItem_ShouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(item);

        itemService.deleteItem(1L);

        verify(itemRepository).findById(1L);
        verify(itemRepository).delete(item);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent item")
    void deleteItem_WhenNotFound_ShouldThrowException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

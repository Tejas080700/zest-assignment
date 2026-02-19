package com.zest.product.service.impl;

import com.zest.product.dto.request.ItemRequest;
import com.zest.product.dto.response.ItemResponse;
import com.zest.product.entity.Item;
import com.zest.product.entity.Product;
import com.zest.product.exception.ResourceNotFoundException;
import com.zest.product.repository.ItemRepository;
import com.zest.product.repository.ProductRepository;
import com.zest.product.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ItemResponse> getItemsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return itemRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItemResponse addItemToProduct(Long productId, ItemRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Item item = Item.builder()
                .quantity(request.getQuantity())
                .product(product)
                .build();

        Item savedItem = itemRepository.save(item);
        return mapToResponse(savedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));
        itemRepository.delete(item);
    }

    private ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .productId(item.getProduct().getId())
                .build();
    }
}

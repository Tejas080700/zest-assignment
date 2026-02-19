package com.zest.product.service;

import com.zest.product.dto.request.ItemRequest;
import com.zest.product.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {

    List<ItemResponse> getItemsByProductId(Long productId);

    ItemResponse addItemToProduct(Long productId, ItemRequest request);

    void deleteItem(Long itemId);
}

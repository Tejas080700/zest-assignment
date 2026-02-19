package com.zest.product.repository;

import com.zest.product.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByProductId(Long productId);

    Page<Item> findByProductId(Long productId, Pageable pageable);
}

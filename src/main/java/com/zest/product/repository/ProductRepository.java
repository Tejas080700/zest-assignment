package com.zest.product.repository;

import com.zest.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Product findByIdWithItems(@Param("id") Long id);

    boolean existsByProductName(String productName);
}

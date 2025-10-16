package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> , JpaSpecificationExecutor<Product> {
    @Override
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.variants v JOIN v.orderDetails od  WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%'))) AND (:minPrice IS NULL OR p.price>=:minPrice) AND (:maxPrice IS NULL OR p.price<=:maxPrice) AND (:categoryId IS NULL OR EXISTS (  SELECT 1  FROM p.categories c2  WHERE c2.id = :categoryId )) GROUP BY p ORDER BY SUM(od.quantity) DESC")
    Page<Product> findBestSellingProduct(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

}

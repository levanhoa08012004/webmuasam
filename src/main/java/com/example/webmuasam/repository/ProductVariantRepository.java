package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {
    Page<ProductVariant> findAll(Specification<ProductVariant> spec, Pageable pageable);
    List<ProductVariant> findAllByProduct_Id(Long productId);
    boolean existsByProduct_IdAndSizeAndColor(Long productId, String size, String color);
    ProductVariant findByProduct_IdAndSizeAndColor(Long productId, String size, String color);
    void deleteAllByProduct(Product product);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id IN :ids")
    List<ProductVariant> findAllByIdInForUpdate(@Param("ids") List<Long> ids);

}

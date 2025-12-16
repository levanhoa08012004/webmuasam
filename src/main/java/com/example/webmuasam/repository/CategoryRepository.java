package com.example.webmuasam.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.webmuasam.entity.Category;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Page<Category> findAll(Specification<Category> spec, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE lower(c.name) LIKE %:keyword% ")
    Page<Category> searchCategories(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByName(String name);

    List<Category> findByIdIn(List<Long> ids);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM product_category pc WHERE pc.category_id = :categoryId", nativeQuery = true)
    void removeCategoryFromProducts(@Param("categoryId") Long categoryId);
}

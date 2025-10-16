package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<Review, Long> , JpaSpecificationExecutor<Review> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
}

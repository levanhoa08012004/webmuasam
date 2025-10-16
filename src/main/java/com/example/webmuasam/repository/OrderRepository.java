package com.example.webmuasam.repository;

import com.example.webmuasam.dto.Response.DashboardResponse;
import com.example.webmuasam.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Page<Order> findAllByUserId(Long userId, Pageable pageable);


}

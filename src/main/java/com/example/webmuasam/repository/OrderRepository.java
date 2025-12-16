package com.example.webmuasam.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.example.webmuasam.dto.Response.DashboardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.webmuasam.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);



    @Query("""
        SELECT new com.example.webmuasam.dto.Response.DashboardResponse(
            CONCAT(
                FUNCTION('YEAR', o.createdAt), '-',
                LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0'), '-',
                LPAD(CAST(FUNCTION('DAY', o.createdAt) AS string), 2, '0')
            ),
            COUNT(DISTINCT o.user.id),
            COUNT(o.id),
            SUM(o.total_price)
        )
        FROM Order o
        WHERE o.createdAt BETWEEN :start AND :end
        GROUP BY CONCAT(
            FUNCTION('YEAR', o.createdAt), '-',
            LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0'), '-',
            LPAD(CAST(FUNCTION('DAY', o.createdAt) AS string), 2, '0')
        )
        ORDER BY CONCAT(
            FUNCTION('YEAR', o.createdAt), '-',
            LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0'), '-',
            LPAD(CAST(FUNCTION('DAY', o.createdAt) AS string), 2, '0')
        )
    """)
    List<DashboardResponse> statsByDay(
            @Param("start") Instant start,
            @Param("end") Instant end
    );


    @Query("""
        SELECT new com.example.webmuasam.dto.Response.DashboardResponse(
            CONCAT(
                FUNCTION('YEAR', o.createdAt), '-',
                LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0'), '-',
                LPAD(CAST(FUNCTION('DAY', o.createdAt) AS string), 2, '0')
            ),
            COUNT(DISTINCT o.user.id),
            COUNT(o.id),
            SUM(o.total_price)
        )
        FROM Order o
        WHERE FUNCTION('YEAR', o.createdAt) = :year
          AND FUNCTION('MONTH', o.createdAt) = :month
        GROUP BY CONCAT(
            FUNCTION('YEAR', o.createdAt), '-',
            LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0'), '-',
            LPAD(CAST(FUNCTION('DAY', o.createdAt) AS string), 2, '0')
        )
        ORDER BY CONCAT(
            FUNCTION('YEAR', o.createdAt), '-',
            LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0'), '-',
            LPAD(CAST(FUNCTION('DAY', o.createdAt) AS string), 2, '0')
        )
    """)
    List<DashboardResponse> statsByDayInMonth(
            @Param("month") int month,
            @Param("year") int year
    );


    @Query("""
        SELECT new com.example.webmuasam.dto.Response.DashboardResponse(
            CONCAT(
                FUNCTION('YEAR', o.createdAt), '-',
                LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0')
            ),
            COUNT(DISTINCT o.user.id),
            COUNT(o.id),
            SUM(o.total_price)
        )
        FROM Order o
        WHERE FUNCTION('YEAR', o.createdAt) = :year
        GROUP BY CONCAT(
            FUNCTION('YEAR', o.createdAt), '-',
            LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0')
        )
        ORDER BY CONCAT(
            FUNCTION('YEAR', o.createdAt), '-',
            LPAD(CAST(FUNCTION('MONTH', o.createdAt) AS string), 2, '0')
        )
    """)
    List<DashboardResponse> statsByMonth(@Param("year") int year);


    @Query("""
        SELECT new com.example.webmuasam.dto.Response.DashboardResponse(
            CAST(FUNCTION('YEAR', o.createdAt) AS string),
            COUNT(DISTINCT o.user.id),
            COUNT(o.id),
            SUM(o.total_price)
        )
        FROM Order o
        GROUP BY FUNCTION('YEAR', o.createdAt)
        ORDER BY FUNCTION('YEAR', o.createdAt)
    """)
    List<DashboardResponse> statsByYear();


    @Query("""
        SELECT new com.example.webmuasam.dto.Response.DashboardResponse(
            'TOTAL',
            COUNT(DISTINCT o.user.id),
            COUNT(o.id),
            SUM(o.total_price)
        )
        FROM Order o
        WHERE o.createdAt BETWEEN :start AND :end
    """)
    DashboardResponse totalStats(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}

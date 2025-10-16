package com.example.webmuasam.Specification;

import com.example.webmuasam.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OrderSpecfication {
    public static Specification<Order> hasStatus(String status){
        return(root,query,cb)->{
            if(status == null || status.trim().isEmpty()) return null;
            return cb.like(root.get("status"), "%"+status+"%");
        };
    }
    public static Specification<Order> createdAtBetween(Instant start, Instant end) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localStart = LocalDateTime.ofInstant(start, zone);
        LocalDateTime localEnd = LocalDateTime.ofInstant(end, zone);

        return (root, query, cb) ->
                cb.between(root.get("createdAt"), localStart, localEnd);
    }

    /**
     * Lọc orders theo năm
     */
    public static Specification<Order> createdAtYear(int year) {
        return (root, query, cb) -> cb.equal(cb.function("year", Integer.class, root.get("createdAt")), year);
    }

    /**
     * Lọc orders theo tháng + năm
     */
    public static Specification<Order> createdAtMonthYear(int month, int year) {
        return (root, query, cb) -> cb.and(
                cb.equal(cb.function("month", Integer.class, root.get("createdAt")), month),
                cb.equal(cb.function("year", Integer.class, root.get("createdAt")), year)
        );
    }
}

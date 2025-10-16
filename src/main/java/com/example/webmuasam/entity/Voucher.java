package com.example.webmuasam.entity;

import com.example.webmuasam.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String code;

    String description;
    int usedCount=0;

    @Column(name = "min_order", nullable = false)
    Double minOrder;

    @Column(name = "discount_percent")
    Double discountPercent=0.0;

    @Column(name = "discount_amount")
    Double discountAmount=0.0;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "status")
    Boolean status = true;

    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    @OneToMany(mappedBy = "voucher",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Order> orders;
    @PrePersist
    public void handleBeforeCreate(){
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;
    }

    @PreUpdate
    public void handleBeforeUpdate(){
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;
    }
}

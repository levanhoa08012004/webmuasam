package com.example.webmuasam.entity;

import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.constant.PaymentMethod;
import com.example.webmuasam.util.constant.StatusOrder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    double total_price;
    @Enumerated(EnumType.STRING)
    StatusOrder status;

    String fullName;
    String phoneNumber;
    String email;
    String address;
    @Column(name = "request_id")
    String requestId;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name="user_id")
    User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<OrderDetail> orderDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    Voucher voucher;



    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
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

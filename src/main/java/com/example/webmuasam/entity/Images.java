package com.example.webmuasam.entity;

import com.example.webmuasam.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="product_images")
public class Images {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Lob
    @Column(name = "base_image", columnDefinition = "LONGBLOB")
    byte[] baseImage;

    @ManyToOne
    @JoinColumn(name="product_id")
    Product product;

    @ManyToOne
    @JoinColumn(name="review_id")
    Review review;

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

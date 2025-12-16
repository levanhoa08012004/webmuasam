package com.example.webmuasam.entity;

import java.time.Instant;

import jakarta.persistence.*;

import com.example.webmuasam.util.SecurityUtil;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "cart_item")
public class CartItem extends BaseEntity{


    int quantity;
    double price;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    Cart cart;

    @ManyToOne
    @JoinColumn(name = "productVariant_id")
    ProductVariant productVariant;


}

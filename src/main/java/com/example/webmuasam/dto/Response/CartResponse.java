package com.example.webmuasam.dto.Response;

import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.CartItem;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    Long id;
    Double totalPrice;
    Integer quantityTotal;
    List<Item> cartItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Item {
        Long id;
        String image;
        String name;
        int quantity;
        double price;
        ProductVariantCart productVariant;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductVariantCart {
        Long id;
        String color;
        String size;
        int stockQuantity;
    }
}

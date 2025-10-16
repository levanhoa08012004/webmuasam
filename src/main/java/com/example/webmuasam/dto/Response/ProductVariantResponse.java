package com.example.webmuasam.dto.Response;


import com.example.webmuasam.entity.ProductVariant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    Long id;
    String color;
    String size;
    int stockQuantity;
    ProductResponse productResponse;



    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductResponse {
        Long productId;
        String productName;
    }
}

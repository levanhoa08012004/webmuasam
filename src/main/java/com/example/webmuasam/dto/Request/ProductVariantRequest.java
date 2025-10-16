package com.example.webmuasam.dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {
    Long id;
    String color;
    String size;
    int stockQuantity;
}

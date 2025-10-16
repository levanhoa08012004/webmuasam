package com.example.webmuasam.dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCartItemRequest {
    Long cartId;
    Long variantId;
    int quantity;
}

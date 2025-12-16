package com.example.webmuasam.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {
    @NotNull(message = "Variant ID must not be null")
    Long id;

    @NotBlank(message = "Color must not be blank")
    @Size(max = 50, message = "Color must not exceed 50 characters")
    String color;

    @NotBlank(message = "Size must not be blank")
    @Size(max = 20, message = "Size must not exceed 20 characters")
    String size;

    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    int stockQuantity;
}

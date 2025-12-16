package com.example.webmuasam.dto.Request;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUpdateDTO {
    @NotNull(message = "Voucher ID must not be null")
    Long id;

    @NotBlank(message = "Voucher code must not be blank")
    @Size(max = 50, message = "Voucher code must not exceed 50 characters")
    String code;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description;

    @NotNull(message = "Minimum order amount must not be null")
    @PositiveOrZero(message = "Minimum order amount must be greater than or equal to 0")
    Double minOrder;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount percent must be greater than 0")
    @DecimalMax(value = "100.0", message = "Discount percent must not exceed 100")
    Double discountPercent;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount amount must be greater than 0")
    Double discountAmount;

    @NotNull(message = "Start date must not be null")
    LocalDate startDate;

    @NotNull(message = "End date must not be null")
    LocalDate endDate;

    @NotNull(message = "Status must not be null")
    Boolean status;
}

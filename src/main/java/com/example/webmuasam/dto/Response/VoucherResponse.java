package com.example.webmuasam.dto.Response;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class VoucherResponse {
    Long id;

    String code;

    String description;

    int usedCount;

    Double minOrder;

    Double discountPercent;

    Double discountAmount;

    LocalDate startDate;

    LocalDate endDate;

    Boolean status = true;
}

package com.example.webmuasam.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUpdateDTO {
    private Long id;
    private String code;
    private String description;
    private Double minOrder;
    private Double discountPercent;
    private Double discountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean status;
}

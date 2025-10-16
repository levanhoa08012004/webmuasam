package com.example.webmuasam.dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DashboardResponse {
    private String label;       // Ngày / Tháng / Năm
    private Long userCount;     // Số người dùng
    private Long orderCount;    // Số đơn hàng
    private Double revenue;

}

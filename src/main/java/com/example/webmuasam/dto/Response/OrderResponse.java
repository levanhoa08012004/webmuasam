package com.example.webmuasam.dto.Response;

import com.example.webmuasam.util.constant.StatusOrder;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    double total_price;
    StatusOrder status;
    String fullName;
    String phoneNumber;
    String email;
    String address;

}

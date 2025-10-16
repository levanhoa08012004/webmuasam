package com.example.webmuasam.dto.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class OrderRequestByCash {
    String fullName;
    String phoneNumber;
    String email;
    String address;
    String voucherCode;
}

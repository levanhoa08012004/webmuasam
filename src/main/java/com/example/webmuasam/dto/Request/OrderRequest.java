package com.example.webmuasam.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    @NotBlank(message = "message cannot be blank")
    String requestId;

    String fullName;
    String phoneNumber;
    String email;
    String address;
    String voucherCode;
    String ipAddress;

}

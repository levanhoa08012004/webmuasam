package com.example.webmuasam.dto.Request;

import com.example.webmuasam.util.constant.EmailPattern;
import com.example.webmuasam.util.constant.PhoneNumber;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    @NotBlank(message = "Request ID must not be blank")
    String requestId;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    String fullName;

    @NotBlank(message = "Phone number must not be blank")
    @PhoneNumber(message = "Phone number is invalid")
    String phoneNumber;

    @NotBlank(message = "Email must not be blank")
    @EmailPattern(message = "Email is invalid")
    String email;

    @NotBlank(message = "Address must not be blank")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @Size(max = 50, message = "Voucher code must not exceed 50 characters")
    String voucherCode;


    String ipAddress;
}

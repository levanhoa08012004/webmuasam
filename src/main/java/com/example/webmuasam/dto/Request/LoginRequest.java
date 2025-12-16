package com.example.webmuasam.dto.Request;

import com.example.webmuasam.util.constant.EmailPattern;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Email must not be blank")
    @EmailPattern(message = "Email invalid format")
    String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 64, message = "Password must be between 8 and 64 characters")
    String password;


}

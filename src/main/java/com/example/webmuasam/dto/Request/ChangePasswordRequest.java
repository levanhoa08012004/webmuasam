package com.example.webmuasam.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    @Size(min = 8, max = 64, message = "Old password must be between 8 and 64 characters")
    String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "New password must be between 8 and 64 characters")
    String newPassword;

}

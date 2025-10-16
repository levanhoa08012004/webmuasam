package com.example.webmuasam.dto.Response;

import com.example.webmuasam.entity.Role;
import com.example.webmuasam.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    @JsonProperty("access_token")
    String accessToken;
    UserLogin user;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserLogin{
        long id;
        String email;
        String name;
        Role role;
        String image;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserGetAccount extends UserLogin{
        String address;
        GenderEnum gender;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInsideToken{
        long id;
        String email;
        String name;
    }

}

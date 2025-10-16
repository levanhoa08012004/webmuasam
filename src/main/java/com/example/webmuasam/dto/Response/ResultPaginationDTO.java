package com.example.webmuasam.dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ResultPaginationDTO {

    Meta meta;
    Object result;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Meta {
        int page;
        int pageSize;
        int pages;
        long total;
        String sort;
    }
}

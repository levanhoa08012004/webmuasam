package com.example.webmuasam.dto.Response;

import com.example.webmuasam.entity.Category;
import com.example.webmuasam.entity.Images;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String name;
    double price;
    Double totalStar;
    Long quantityReview;
    String description;
    long quantity;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    List<CategoryPro> categories;
    List<String> images;
    List<Variants> variants;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Variants{
        Long id;
        String color;
        String size;
        int stockQuantity;
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CategoryPro{
        Long id;
        String name;
    }





}

package com.example.webmuasam.mapper;

import com.example.webmuasam.dto.Request.CategoryRequest;
import com.example.webmuasam.dto.Response.CategoryResponse;
import com.example.webmuasam.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")

public interface CategoryMapper {
    CategoryResponse mapCategoryToCategoryResponse(Category category);

    Category mapCategoryRequestToCategory(CategoryRequest category);

    @Mapping(target = "id", ignore = true) // không update id
    @Mapping(target = "products", ignore = true) // giữ quan hệ products
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}

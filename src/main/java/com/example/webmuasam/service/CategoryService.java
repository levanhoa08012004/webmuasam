package com.example.webmuasam.service;

import com.example.webmuasam.dto.Request.CategoryRequest;
import com.example.webmuasam.dto.Response.CategoryResponse;
import com.example.webmuasam.exception.ResourceNotFoundException;
import com.example.webmuasam.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Category;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    public CategoryResponse createCategory(CategoryRequest request) throws AppException {
        Category category = categoryMapper.mapCategoryRequestToCategory(request);
        if (this.categoryRepository.existsByName(category.getName())) {
            throw new AppException("Category name already exists");
        }
        category = categoryRepository.save(category);
        return categoryMapper.mapCategoryToCategoryResponse(category);
    }

    public CategoryResponse updateCategory(CategoryRequest request) {
        Category category = categoryRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new AppException("Category name already exists");
        }
        categoryMapper.updateEntityFromRequest(request,category);
        return categoryMapper.mapCategoryToCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategoryById(Long id) throws AppException {
        return this.categoryMapper.mapCategoryToCategoryResponse(this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id category not found")));
    }

    public void deleteCategory(Long id) throws AppException {
        Category category =
                this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id category not found"));
        this.categoryRepository.removeCategoryFromProducts(id);

        this.categoryRepository.delete(category);
    }

    public ResultPaginationDTO getAllCategories(int pageNo, int pageSize,String keyword,List<String> sorts) {
        int pageIndex = pageNo - 1;

        List<Sort.Order> orders = new ArrayList<>();
        if (sorts != null && !sorts.isEmpty()) {
            for (String sort : sorts) {
                Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
                Matcher matcher = pattern.matcher(sort);
                if (matcher.find()) {
                    orders.add(new Sort.Order(
                            matcher.group(3).equalsIgnoreCase("asc")
                                    ? Sort.Direction.ASC
                                    : Sort.Direction.DESC,
                            matcher.group(1)
                    ));
                }
            }
        }
        if (orders.isEmpty()) {
            orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        }
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(orders));

        Page<Category> pageCategories = this.categoryRepository.searchCategories(keyword, pageable);
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageCategories.getTotalElements());
        meta.setPages(pageCategories.getTotalPages());

        return ResultPaginationDTO.builder()
                .meta(meta)
                .result(pageCategories.getContent())
                .build();
    }
}

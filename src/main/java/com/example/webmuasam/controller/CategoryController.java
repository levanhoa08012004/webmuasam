package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.CategoryRequest;
import com.example.webmuasam.dto.Response.ApiResponse;
import com.example.webmuasam.dto.Response.CategoryResponse;
import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Category;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.CategoryService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ApiMessage("add category success")
    public ResponseEntity<CategoryResponse> CreateCategory(@Valid @RequestBody CategoryRequest request) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get category success")
    public ResponseEntity<CategoryResponse> GetCategory(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.categoryService.getCategoryById(id));
    }

    @GetMapping
    @ApiMessage("Get All category success")
    public ResponseEntity<ResultPaginationDTO> GetAllCategory(@RequestParam(defaultValue = "1")
                                                                   @Min(value = 1, message = "Page must be greater than or equal to 1")
                                                                   int pageNumber,
                                                               @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than or equal to 1")
                                                                   int pageSize,
                                                               @RequestParam(required = false) List<String> sorts,
                                                               @RequestParam(defaultValue = "") String keyword) {

        return ResponseEntity.ok(this.categoryService.getAllCategories(pageNumber,pageSize,keyword,sorts));
    }

    @PutMapping
    @ApiMessage("Update category success")
    public ResponseEntity<CategoryResponse> UpdateCategory(@Valid @RequestBody CategoryRequest request) throws AppException {
        return ResponseEntity.ok(this.categoryService.updateCategory(request));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete Category success")
    public ResponseEntity<ApiResponse<Void>> DeleteCategory(@PathVariable Long id) throws AppException {
        this.categoryService.deleteCategory(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Delete Category success")
                .data(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}

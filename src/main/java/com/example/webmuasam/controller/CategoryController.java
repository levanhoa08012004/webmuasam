package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Category;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.CategoryService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ApiMessage("add category success")
    public ResponseEntity<Category> CreateCategory(@Valid @RequestBody Category category)throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(category));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get category success")
    public ResponseEntity<Category> GetCategory(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.categoryService.getCategoryById(id));
    }

    @GetMapping
    @ApiMessage("Get All category success")
    public ResponseEntity<ResultPaginationDTO> GetAllCategory(@Filter Specification<Category> spec , Pageable pageable) {
        return ResponseEntity.ok(this.categoryService.getAllCategories(spec,pageable));
    }

    @PutMapping
    @ApiMessage("Update category success")
    public ResponseEntity<Category> UpdateCategory(@Valid @RequestBody Category category) throws AppException {
        return ResponseEntity.ok(this.categoryService.updateCategory(category));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete Category success")
    public ResponseEntity<String> DeleteCategory(@PathVariable Long id) throws AppException {
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok("success");
    }
}

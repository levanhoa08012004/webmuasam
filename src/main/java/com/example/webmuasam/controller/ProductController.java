package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.ProductResponse;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Category;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.ProductService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping
    @ApiMessage("add Product success")
    public ResponseEntity<Product> CreateProduct(@RequestParam String name,
                                                 @RequestParam Double price,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam(value = "images", required = false) List<MultipartFile> files,
                                                 @RequestParam(value = "categories", required = false) List<String> categoryIds

    ) {
        log.info("check>>>>>: " +name +" "+price+" "+description+" "+files+" "+categoryIds);
        List<Long> categories = categoryIds == null ? Collections.emptyList()
                : categoryIds.stream().map(Long::parseLong).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(name,price,description,files,categories));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get product success")
    public ResponseEntity<ProductResponse> GetProduct(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.productService.getProduct(id));
    }

    @GetMapping
    @ApiMessage("Get All Product success")
    public ResponseEntity<ResultPaginationDTO> GetAllProduct(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) Double minPrice,
                                                             @RequestParam(required = false) Double maxPrice,
                                                             @RequestParam(required = false) Long categoryId,
                                                             Pageable pageable) {
        return ResponseEntity.ok(this.productService.getAllProduct(name,minPrice,maxPrice,categoryId,pageable));
    }

    @GetMapping("/best-selling")
    @ApiMessage("Get product best selling success")
    public ResultPaginationDTO getBestSellingProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        return productService.getBestSellingProducts(name, minPrice, maxPrice, categoryId, page, size, sort);
    }
    @PutMapping
    @ApiMessage("Update product success")
    public ResponseEntity<Product> UpdateProduct(@RequestParam Long id,
                                                 @RequestParam String name,
                                                 @RequestParam Double price,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam(value = "images", required = false) List<MultipartFile> files,
                                                 @RequestParam(value = "categories", required = false) List<Long> categories
                                                 ) throws AppException {

        return ResponseEntity.ok(this.productService.updateProduct(id,name,price,description,files,categories));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete user success")
    public ResponseEntity<String> DeleteProduct(@PathVariable Long id) throws AppException {
        this.productService.deleteProduct(id);
        return ResponseEntity.ok("success");
    }
}

package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.ProductVariantRequest;
import com.example.webmuasam.dto.Response.ProductVariantResponse;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.ProductVariantService;
import com.example.webmuasam.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/product_variants")
public class ProductVariantController {
    private final ProductVariantService productVariantService;
    public ProductVariantController(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }
    @PostMapping("/{id}")
    @ApiMessage("create Product Variant success")
    public ResponseEntity<ProductVariant> CreateProductVariant(@Valid @RequestBody ProductVariant productVariant, @PathVariable Long id)throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productVariantService.createProductVariant(id,productVariant));
    }

    @GetMapping
    @ApiMessage("Get Product Variant success")
    public ResponseEntity<ProductVariantResponse> GetProductVariantByProductIdAndColorAndSize(@RequestParam Long productId,
                                                                               @RequestParam String color,
                                                                               @RequestParam String sizep) throws AppException
    {
        return ResponseEntity.ok(this.productVariantService.getVariant(productId, sizep, color));
    }
    @GetMapping("/{id}")
    @ApiMessage("Get Product Variant success")
    public ResponseEntity<ProductVariantResponse> GetProductVariant(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.productVariantService.getProductVariant(id));
    }

    @GetMapping("all/{id}")
    @ApiMessage("Get All Product Variant success")
    public ResponseEntity<List<ProductVariantResponse>> GetAllProductVariant(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.productVariantService.getAllByProductID(id));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update Product Variant success")
    public ResponseEntity<ProductVariantResponse> UpdateProductVariant(@Valid @PathVariable Long id, @RequestBody ProductVariantRequest productVariant) throws AppException {
        return ResponseEntity.ok(this.productVariantService.updateProductVariant(id,productVariant));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete Product Variant success")
    public ResponseEntity<String> DeleteProductVariant(@PathVariable Long id) throws AppException {
        this.productVariantService.deleteproductVariant(id);
        return ResponseEntity.ok("success");
    }
}

package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.ReviewResponse;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@Valid
                                                    @RequestParam String comment,
                                                    @RequestParam Integer rating,
                                                    @RequestParam Instant createdAt,
                                                    @RequestParam(required = false) List<MultipartFile> imageBases,
                                                    @RequestParam Long productId,
                                                    @RequestParam Long userId)throws AppException, IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.reviewService.addReview(comment,rating,createdAt,imageBases,productId,userId));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllReviewByProductId(@RequestParam Long productId,Pageable pageable) {
        return ResponseEntity.ok(this.reviewService.getAllReviewByProduct(pageable,productId));
    }
}

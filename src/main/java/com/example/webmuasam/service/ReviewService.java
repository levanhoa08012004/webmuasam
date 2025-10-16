package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.ReviewResponse;
import com.example.webmuasam.entity.Images;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.Review;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.ProductRepository;
import com.example.webmuasam.repository.ReviewRepository;
import com.example.webmuasam.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public ReviewResponse addReview(String comment, Integer rating, Instant createdAt, List<MultipartFile> imageBases, Long productId, Long userId) throws AppException {
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new AppException("Không tìm thấy sản phẩm này"));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new AppException("Không tìm thấy user"));
        Review review = new Review();
        review.setComment(comment);
        review.setRating(rating);
        review.setCreatedAt(createdAt);
        review.setProduct(product);
        review.setUser(user);
        if (imageBases != null) {
            List<Images> imagesNew = new ArrayList<>();
            for (MultipartFile image : imageBases) {
                try {
                    Images img = new Images();
                    img.setBaseImage(image.getBytes());
                    img.setReview(review);
                    imagesNew.add(img);
                } catch (IOException e) {
                    throw new RuntimeException("loi up anh", e);
                }
            }

            review.setImages(imagesNew);
        }
            this.reviewRepository.save(review);
            return convertReview(review);
    }
    public ReviewResponse convertReview (Review review){
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.setId(review.getId());
        reviewResponse.setComment(review.getComment());
        reviewResponse.setRating(review.getRating());
        reviewResponse.setCreatedAt(review.getCreatedAt());
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            List<String> images = review.getImages().stream().map((image) -> {
                return Base64.getEncoder().encodeToString(image.getBaseImage());
            }).collect(Collectors.toList());
            reviewResponse.setImageBase(images);
        }
        ReviewResponse.UserReview userReview = new ReviewResponse.UserReview();
        userReview.setId(review.getUser().getId());
        userReview.setUsername(review.getUser().getUsername());
        if (review.getUser().getImage() != null) {
            userReview.setImage(Base64.getEncoder().encodeToString(review.getUser().getImage()));
        }
        reviewResponse.setUser(userReview);
        return reviewResponse;
    }

    public ResultPaginationDTO getAllReviewByProduct(Pageable pageable,Long productId){
        Page<Review> reviews = this.reviewRepository.findByProductId(productId, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(reviews.getTotalElements());
        meta.setPages(reviews.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        List<ReviewResponse> reviewList = reviews.getContent().stream().map((this::convertReview)).collect(Collectors.toList());
        resultPaginationDTO.setResult(reviewList);
        return resultPaginationDTO;
    }


}


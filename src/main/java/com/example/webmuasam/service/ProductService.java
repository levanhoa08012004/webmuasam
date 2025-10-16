package com.example.webmuasam.service;

import com.example.webmuasam.Specification.ProductSpecification;
import com.example.webmuasam.dto.Response.ProductResponse;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.*;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CategoryRepository;
import com.example.webmuasam.repository.ImageRepository;
import com.example.webmuasam.repository.ProductRepository;
import com.example.webmuasam.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ImageRepository imageRepository;
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,ProductVariantRepository productVariantRepository, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.imageRepository = imageRepository;
    }
    public ProductResponse convertProductToProductResponse(Product product) {
        ProductResponse productResponse = new ProductResponse();

        productResponse.setId(product.getId());
        long quantity = product.getVariants().stream().mapToLong(ProductVariant::getStockQuantity).sum();
        productResponse.setQuantity(quantity);

        productResponse.setName(product.getName());
        productResponse.setDescription(product.getDescription());
        productResponse.setPrice(product.getPrice());
        productResponse.setCreatedBy(product.getCreatedBy());
        productResponse.setUpdatedBy(product.getUpdatedBy());
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        if(product.getCategories() != null) {
            List<ProductResponse.CategoryPro> productResponseCategoryPro = product.getCategories().stream().map(c->{
                ProductResponse.CategoryPro categoryPro = new ProductResponse.CategoryPro();
                categoryPro.setId(c.getId());
                categoryPro.setName(c.getName());
                return categoryPro;
            }).collect(Collectors.toList());

            productResponse.setCategories(productResponseCategoryPro);
        }
        if(product.getImages() != null) {
            List<String> base64Images = product.getImages().stream().map(img -> Base64.getEncoder().encodeToString(img.getBaseImage())).collect(Collectors.toList());
            productResponse.setImages(base64Images);
        }
        Double totalStar = 0.0;
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            totalStar = product.getReviews()
                    .stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            long quantityReview = product.getReviews().size();
            productResponse.setQuantityReview(quantityReview);
        }
        productResponse.setTotalStar(totalStar);
        if(product.getVariants() != null) {
            List<ProductResponse.Variants> variants= product.getVariants().stream().map(c->{
                ProductResponse.Variants variant = new ProductResponse.Variants();
                variant.setId(c.getId());
                variant.setColor(c.getColor());
                variant.setSize(c.getSize());
                variant.setStockQuantity(c.getStockQuantity());
                return variant;
            }).collect(Collectors.toList());
            productResponse.setVariants(variants);
        }
        return productResponse;
    }
    @Transactional
    public Product createProduct(String name, Double price, String description, List<MultipartFile> images,List<Long> categories) {
        // Gán danh mục
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        if(categories != null) {
            List<Category> categorienews = categoryRepository.findAllById(categories);
            product.setCategories(categorienews);
        }


        // Gán lại product cho images & variants
        if (images != null) {
            List<Images> imagesNew = new ArrayList<>();
            for (MultipartFile image : images) {
                try{
                    Images img =new Images();
                    img.setBaseImage(image.getBytes());
                    img.setProduct(product);
                    imagesNew.add(img);
                }catch (IOException e){
                    throw new RuntimeException("loi up anh",e);
                }
            }
            product.setImages(imagesNew);
        }

//        if (productVariants != null) {
//            List<ProductVariant> productNews = new ArrayList<>();
//            for (ProductVariant variant : productVariants) {
//                variant.setProduct(product);
//                productNews.add(variant);
//            }
//            product.setVariants(productNews);
//        }

        // Tổng quantity từ variants
        long totalQuantity = product.getVariants() != null
                ? product.getVariants().stream().mapToLong(ProductVariant::getStockQuantity).sum()
                : 0;
        product.setQuantity(totalQuantity);

        return productRepository.save(product);
    }
    @Transactional
    public Product updateProduct(Long id,String name, Double price, String description, List<MultipartFile> images,List<Long> categories) throws AppException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product not found"));

        // Cập nhật thông tin cơ bản
        existingProduct.setName(name);
        existingProduct.setPrice(price);
        existingProduct.setDescription(description);

        // Cập nhật danh mục
        if(categories != null) {
            List<Category> categoryNews = categoryRepository.findAllById(categories);
            existingProduct.setCategories(categoryNews);
        }


        // Xoá ảnh và thêm mới
        if(images != null) {
            imageRepository.deleteAllByProduct(existingProduct);
            List<Images> imagesNew = new ArrayList<>();
            for (MultipartFile image : images) {
                try{
                    Images img =new Images();
                    img.setBaseImage(image.getBytes());
                    img.setProduct(existingProduct);
                    imagesNew.add(img);
                }catch (IOException e){
                    throw new RuntimeException("loi up anh",e);
                }
            }
            existingProduct.setImages(imagesNew);
        }




        // Tính lại tổng quantity
        long totalQuantity = existingProduct.getVariants() != null
                ? existingProduct.getVariants().stream().mapToLong(ProductVariant::getStockQuantity).sum()
                : 0;
        existingProduct.setQuantity(totalQuantity);

        return productRepository.save(existingProduct);
    }

    public ResultPaginationDTO getAllProduct(String name, Double minPrice, Double maxPrice, Long categoryId, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.hasName(name))
                .and(ProductSpecification.hasPriceBetween(minPrice, maxPrice))
                .and(ProductSpecification.hasCategoryId(categoryId));
        Page<Product> products = this.productRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(products.getTotalElements());
        meta.setPages(products.getTotalPages());

        if(pageable.getSort().isSorted()){
            meta.setSort(pageable.getSort().toString());
        }
        resultPaginationDTO.setMeta(meta);

        List<ProductResponse> productResponses = products.getContent().stream().map(this::convertProductToProductResponse).collect(Collectors.toList());
        resultPaginationDTO.setResult(productResponses);
        return resultPaginationDTO;
    }
    public ResultPaginationDTO getBestSellingProducts(String name, Double minPrice, Double maxPrice, Long categoryId,int page,int size,String sort) {
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String sortField = parts[0];
            Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
        } else {
            pageable = PageRequest.of(page - 1, size);
        }

        Page<Product> products = productRepository.findBestSellingProduct(
                categoryId, name, minPrice, maxPrice, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(products.getTotalElements());
        meta.setPages(products.getTotalPages());

        if(pageable.getSort().isSorted()){
            meta.setSort(pageable.getSort().toString());
        }
        resultPaginationDTO.setMeta(meta);
        List<ProductResponse> productResponses = products.getContent().stream().map(this::convertProductToProductResponse).collect(Collectors.toList());
        resultPaginationDTO.setResult(productResponses);
        return resultPaginationDTO;
    }

    public ProductResponse getProduct(Long id) throws AppException {
        Product product =  this.productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product không tồn tại với id = " + id));
        return convertProductToProductResponse(product);
    }
    public void deleteProduct(Long id) throws AppException {
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product không tồn tại với id = " + id));

        // Xử lý xóa liên kết với category nếu cần
        product.setCategories(null);

        // Xóa các biến thể sản phẩm nếu có
        product.getVariants().forEach(variant -> variant.setProduct(null));
        product.getVariants().clear();

        // Xử lý nếu có ảnh hoặc liên kết khác (ví dụ productImages)
        product.getImages().forEach(img -> img.setProduct(null));
        product.getImages().clear();

        // Sau khi làm sạch quan hệ -> xóa
        this.productRepository.delete(product);
    }


}

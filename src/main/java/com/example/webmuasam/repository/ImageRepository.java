package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Images;
import com.example.webmuasam.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Images, Long>, JpaSpecificationExecutor<Images> {
    Page<Images> findAll(Specification<Images> spec, Pageable pageable);
    boolean existsByBaseImage(byte[] baseImage);
    void deleteAllByProduct(Product product);

}

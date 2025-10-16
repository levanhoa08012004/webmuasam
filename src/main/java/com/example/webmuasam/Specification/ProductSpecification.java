package com.example.webmuasam.Specification;

import com.example.webmuasam.entity.Category;
import com.example.webmuasam.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    public static Specification<Product> hasName(String name){
        return (root,query,cb)->{
            if(name == null || name.trim().isEmpty()) return null;
            return cb.like(root.get("name"), "%"+name+"%");
        };
    }

    public static Specification<Product> hasPriceBetween(Double minPrice, Double maxPrice){
        return (root,query,cb)->{
            if(minPrice == null && maxPrice == null)return null;
            if(minPrice == null) return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            if(maxPrice == null) return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            Join<Product, Category> join = root.join("categories", JoinType.INNER);
            return cb.equal(join.get("id"), categoryId);
        };
    }


}



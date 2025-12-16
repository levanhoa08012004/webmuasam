package com.example.webmuasam.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import com.example.webmuasam.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "categories")
public class Category extends BaseEntity{

    String name;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
    @JsonIgnore
    List<Product> products;




}

package com.example.webmuasam.entity;

import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.constant.GenderEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username",unique = true,columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    @NotBlank(message = "password khong duoc de trong")
    String password;

    @NotBlank(message = "email khong duoc de trong")
    String email;

    String address;

    @Enumerated(EnumType.STRING)
    GenderEnum gender;


    @Lob
    @Column(columnDefinition = "LONGBLOB")
    byte[] image;

    @Column(columnDefinition = "MEDIUMTEXT")
    String refreshToken;

    Instant createdAt;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a",timezone = "GMT+7")
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    @ManyToOne
    @JoinColumn(name="role_id")
    Role role;

    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY)
    @JsonIgnore
    Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<Order> order;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<Review> reviews;

    @PrePersist
    public void handleBeforeCreate(){
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;
    }

    @PreUpdate
    public void handleBeforeUpdate(){
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : null;
    }


}

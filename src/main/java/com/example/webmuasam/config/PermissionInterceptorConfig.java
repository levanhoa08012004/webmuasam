package com.example.webmuasam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfig implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {"/","/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/register",
                "/api/v1/auth/account"
                ,"/api/v1/products","/api/v1/products/**",
                "/api/v1/categories","/api/v1/categories/**",
                "/order/*/status",
                "/api/v1/auth/email",
                "/api/v1/auth/email/**",
                "/api/v1/product/best-selling",
                "/api/v1/cartitems/cart",
                "/api/v1/cartitems/update",
                "/api/v1/product_variants",
                "/api/v1/voucher",
                "/api/v1/voucher/**",
                "/api/v1/auth/logout",
                "/api/v1/orders/status/**",
                "/api/v1/orders/status",
                "/api/v1/auth/change-password",
                "/api/v1/reviews",
                "/api/v1/reviews/**",
                "/api/v1/users",


        };
        registry.addInterceptor(permissionInterceptor()).excludePathPatterns(whiteList);
    }
}

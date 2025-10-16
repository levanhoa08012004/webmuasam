package com.example.webmuasam.config;

import com.example.webmuasam.entity.Permission;
import com.example.webmuasam.entity.Role;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.repository.PermissionRepository;
import com.example.webmuasam.repository.RoleRepository;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.util.constant.GenderEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseInititalizer implements CommandLineRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInititalizer(final PermissionRepository permissionRepository,RoleRepository roleRepository,UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        long countPermission = this.permissionRepository.count();
        long countRole = this.roleRepository.count();
        long countUser = this.userRepository.count();
        if(countPermission == 0) {
            ArrayList<Permission> arr = new ArrayList<>();
            arr.add(new Permission("Get all cart item by cartId", "/api/v1/cartitems/{id}", "GET", "CARTITEMS"));
            arr.add(new Permission("Get cart", "/api/v1/cartitems/cart", "GET", "CARTITEMS"));
            arr.add(new Permission("Create a cart item", "/api/v1/cartitems", "POST", "CARTITEMS"));
            arr.add(new Permission("increase a cart item", "/api/v1/cartitems/incre/{id}", "PUT", "CARTITEMS"));
            arr.add(new Permission("reduce a cart item", "/api/v1/cartitems/des/{id}", "PUT", "CARTITEMS"));
            arr.add(new Permission("Delete a cart item", "/api/v1/cartitems/{id}", "DELETE", "CARTITEMS"));
            arr.add(new Permission("Update cart item", "/api/v1/cartitems/update", "PUT", "CARTITEMS"));

            arr.add(new Permission("Create a category", "/api/v1/categories", "POST", "CATEGORIES"));
            arr.add(new Permission("Update a category", "/api/v1/categories", "PUT", "CATEGORIES"));
            arr.add(new Permission("delete a category", "/api/v1/categories/{id}", "DELETE", "CATEGORIES"));
            arr.add(new Permission("Get a category", "/api/v1/categories/{id}", "GET", "CATEGORIES"));
            arr.add(new Permission("Get all category", "/api/v1/categories", "GET", "CATEGORIES"));

            arr.add(new Permission("Create a Permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Update a Permission", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            arr.add(new Permission("delete a Permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            arr.add(new Permission("Get a Permission", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Get all Permission", "/api/v1/permissions", "GET", "PERMISSIONS"));

            arr.add(new Permission("Create a product variants", "/api/v1/product_variants/{id}", "POST", "PRODUCTVARIANTS"));
            arr.add(new Permission("Update a product variants", "/api/v1/product_variants/{id}", "PUT", "PRODUCTVARIANTS"));
            arr.add(new Permission("delete a product variants", "/api/v1/product_variants/{id}", "DELETE", "PRODUCTVARIANTS"));
            arr.add(new Permission("Get a product variants", "/api/v1/product_variants/{id}", "GET", "PRODUCTVARIANTS"));
            arr.add(new Permission("Get all product variants by product id", "/api/v1/product_variants/all/{id}", "GET", "PRODUCTVARIANTS"));
            arr.add(new Permission("Get all product variants by product id and color and size", "/api/v1/product_variants", "GET", "PRODUCTVARIANTS"));


            arr.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Update a user", "/api/v1/users", "PUT", "USERS"));
            arr.add(new Permission("delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
            arr.add(new Permission("Get a user", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Get all user", "/api/v1/users", "GET", "USERS"));

            arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Update a role", "/api/v1/roles", "PUT", "ROLES"));
            arr.add(new Permission("delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            arr.add(new Permission("Get a role", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Get all role", "/api/v1/roles", "GET", "ROLES"));

            arr.add(new Permission("Create a product", "/api/v1/products", "POST", "PRODUCTS"));
            arr.add(new Permission("Update a product", "/api/v1/products", "PUT", "PRODUCTS"));
            arr.add(new Permission("delete a product", "/api/v1/products/{id}", "DELETE", "PRODUCTS"));
            arr.add(new Permission("Get a product", "/api/v1/products/{id}", "GET", "PRODUCTS"));
            arr.add(new Permission("Get all product", "/api/v1/products", "GET", "PRODUCTS"));
            arr.add(new Permission("Get all product best seller","/api/v1/products/best-selling","GET", "PRODUCTS"));


            arr.add(new Permission("order by momo create qr", "/api/v1/momo/create", "POST", "ORDERS"));
            arr.add(new Permission("order by momo", "/api/v1/momo/ipn-handler", "GET", "ORDERS"));
            arr.add(new Permission("order by cash", "/api/v1/orders/cash", "POST", "ORDERS"));
            arr.add(new Permission("delete a order", "/api/v1/orders/{id}", "DELETE", "ORDERS"));
            arr.add(new Permission("Get all order by user", "/api/v1/orders/user", "GET", "ORDERS"));
            arr.add(new Permission("Get order status by id", "/api/v1/orders/{id}/status", "GET", "ORDERS"));
            arr.add(new Permission("Get order by id", "/api/v1/orders/{id}", "GET", "ORDERS"));
            arr.add(new Permission("Get all order by status", "/api/v1/orders", "GET", "ORDERS"));
            arr.add(new Permission("Put change order status for admin", "/api/v1/orders/status-admin", "PUT", "ORDERS"));
            arr.add(new Permission("Put change order status for shipper", "/api/v1/orders/status", "PUT", "ORDERS"));
            arr.add(new Permission("Get all order by day", "/api/v1/orders/day", "GET", "ORDERS"));
            arr.add(new Permission("Get all order by month", "/api/v1/orders/month", "GET", "ORDERS"));
            arr.add(new Permission("Get all order by year", "/api/v1/orders/year", "GET", "ORDERS"));

            arr.add(new Permission("Get a review by product id", "/api/v1/reviews", "GET", "REVIEWS"));
            arr.add(new Permission("Post a review", "/api/v1/reivews", "POST", "REVIEWS"));

            arr.add(new Permission("Create a voucher", "/api/v1/voucher", "POST", "VOUCHERES"));
            arr.add(new Permission("Update a voucher", "/api/v1/voucher", "PUT", "VOUCHERES"));
            arr.add(new Permission("delete a voucher", "/api/v1/voucher/{id}", "DELETE", "VOUCHERES"));
            arr.add(new Permission("Get a voucher", "/api/v1/voucher/{id}", "GET", "VOUCHERES"));
            arr.add(new Permission("Get all voucher", "/api/v1/voucher", "GET", "VOUCHERES"));
            arr.add(new Permission("Check a voucher", "/api/v1/voucher/check", "GET", "VOUCHERES"));



            this.permissionRepository.saveAll(arr);
        }
        if(countRole == 0){
            List<Permission> allPermissions = this.permissionRepository.findAll();
            Role adminRole =new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);
            this.roleRepository.save(adminRole);
        }
        if(countUser == 0){
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("dh");
            adminUser.setGender(GenderEnum.MALE);
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");

            if(adminRole != null){
                adminUser.setRole(adminRole);
            }
            this.userRepository.save(adminUser);
        }
        if(countPermission > 0 && countUser > 0 && countRole > 0){
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA ...");
        }else
            System.out.println(">>> END INIT DATABASE");
    }

}

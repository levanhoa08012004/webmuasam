package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.CreateUserResponse;
import com.example.webmuasam.dto.Response.GetAllUserResponse;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.UpdateUserResponse;
import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.Order;
import com.example.webmuasam.entity.Role;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.*;
import com.example.webmuasam.util.constant.GenderEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, CartService cartService,CartItemRepository cartItemRepository,CartRepository cartRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public CreateUserResponse CreateUser(User user) throws AppException {

        User savedUser;
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new AppException("Email đã tồn tại");
        } else {
            if (user.getRole() != null && user.getRole().getId() != null) {
                Role role = this.roleRepository.findById(user.getRole().getId()).get();
                user.setRole(role != null ? role : null);
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            savedUser = userRepository.save(user);
            try {
                this.cartService.createCart(savedUser.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return convertToCreateUserResponse(savedUser);

    }
    public CreateUserResponse convertToCreateUserResponse(User user){
        CreateUserResponse createUserResponse = new CreateUserResponse();
        CreateUserResponse.RoleUser roleUser = new CreateUserResponse.RoleUser();
        createUserResponse.setId(user.getId());
        createUserResponse.setEmail(user.getEmail());
        createUserResponse.setUsername(user.getUsername());
        createUserResponse.setAddress(user.getAddress());
        createUserResponse.setGender(user.getGender());
        createUserResponse.setCreatedAt(user.getCreatedAt());
        createUserResponse.setCreatedBy(user.getCreatedBy());
        if(user.getImage() != null) {
            String imagebase = Base64.getEncoder().encodeToString(user.getImage());
            createUserResponse.setImage(imagebase);
        }
        if(user.getRole() != null){
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            createUserResponse.setRole(roleUser);
        }
        return createUserResponse;
    }

    public UpdateUserResponse convertToUpdateUserResponse(User user){
        UpdateUserResponse updateUserResponse = new UpdateUserResponse();
        UpdateUserResponse.RoleUser roleUser = new UpdateUserResponse.RoleUser();
        updateUserResponse.setEmail(user.getEmail());
        updateUserResponse.setUsername(user.getUsername());
        updateUserResponse.setAddress(user.getAddress());
        updateUserResponse.setGender(user.getGender());
        updateUserResponse.setUpdatedAt(user.getUpdatedAt());
        updateUserResponse.setUpdatedBy(user.getUpdatedBy());
        if(user.getImage() != null) {
            String imagebase = Base64.getEncoder().encodeToString(user.getImage());
            updateUserResponse.setImage(imagebase);
        }
        if(user.getRole() != null){
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            updateUserResponse.setRole(roleUser);
        }
        return updateUserResponse;
    }

    public UpdateUserResponse updateUser(Long id, String username, String address, GenderEnum gender, Long roleId,MultipartFile images) throws AppException, IOException {
        User oldUser = this.userRepository.findById(id).orElseThrow(()-> new AppException("user not found"));
            oldUser.setUsername(username);
            oldUser.setAddress(address);
            oldUser.setGender(gender);
            if(images != null){
                oldUser.setImage(images.getBytes());
            }
        Role role = this.roleRepository.findById(roleId).orElseThrow(()-> new AppException("role not found"));
            if(role != null){
                oldUser.setRole(role);
            }
            this.userRepository.save(oldUser);


        return convertToUpdateUserResponse(oldUser);
    }

    public User getUserById(Long id) throws AppException {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber()+1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());
        resultPaginationDTO.setMeta(meta);

        List<GetAllUserResponse> listUser = pageUser.getContent().stream().map(item -> new GetAllUserResponse(
                item.getId(),
                item.getUsername(),
                item.getEmail(),
                item.getAddress(),
                item.getGender(),
                item.getImage() != null ? Base64.getEncoder().encodeToString(item.getImage()):null,
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getCreatedBy(),
                item.getUpdatedBy(),
                new GetAllUserResponse.RoleUser(
                        item.getRole() != null ? item.getRole().getId() : 0,
                        item.getRole() != null ? item.getRole().getName() : "null"
                )
        )).collect(Collectors.toList());
        resultPaginationDTO.setResult(listUser);
        return resultPaginationDTO;
    }

    @Transactional
    public void deleteUser(Long userId) throws AppException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        // Xoá cart item & cart
        if (user.getCart() != null) {
            Cart cart = user.getCart();
            cartItemRepository.deleteByCartId(cart.getId()); // custom method
            cartRepository.delete(cart);
        }

        // Xoá order detail & order
        if (user.getOrder() != null && !user.getOrder().isEmpty()) {
            for (Order order : user.getOrder()) {
                orderDetailRepository.deleteByOrderId(order.getId()); // custom method
            }
            orderRepository.deleteAll(user.getOrder());
        }

        // Cuối cùng xóa user
        userRepository.delete(user);
    }

    public User handleGetUserByUserName(String email) throws AppException{

        return userRepository.findByEmail(email).orElseThrow(()->new AppException("user không tồn tại"));
    }


    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUserToken(String token,String email){
        User currentUser = null;
        try {
            currentUser = this.handleGetUserByUserName(email);
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
        if(currentUser !=null){
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }
    public User getUserByFreshTokenAndEmail(String token, String email){
        return this.userRepository.findByRefreshTokenAndEmail(token,email);
    }

}

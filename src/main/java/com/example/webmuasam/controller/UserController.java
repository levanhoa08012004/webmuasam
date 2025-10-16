package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.CreateUserResponse;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.dto.Response.UpdateUserResponse;
import com.example.webmuasam.entity.Role;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.service.RoleService;
import com.example.webmuasam.service.UserService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.example.webmuasam.util.constant.GenderEnum;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    public UserController(UserService userService,RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostMapping
    @ApiMessage("add user success")
    public ResponseEntity<CreateUserResponse> CreateUser(@Valid @RequestParam String email,
                                                         @RequestParam String username,
                                                         @RequestParam(required = false) String address,
                                                         @RequestParam(required = false) GenderEnum gender,
                                                         @RequestParam String password,
                                                         @RequestParam(required = false) Long roleId,
                                                         @RequestParam(required = false) MultipartFile images) throws AppException, IOException {
        if(roleId==null){
            roleId=2L;
        }
        Role role= this.roleService.fetchRoleById(roleId);
        User user= new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setAddress(address);
        user.setPassword(password);
        user.setGender(gender);
        user.setRole(role);

        if(images!=null){
            user.setImage(images.getBytes());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.CreateUser(user));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get user success")
    public ResponseEntity<CreateUserResponse> GetUser(@PathVariable Long id) throws AppException {
        User user = this.userService.getUserById(id);
        CreateUserResponse userResponse = this.userService.convertToCreateUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    @ApiMessage("Get All user success")
    public ResponseEntity<ResultPaginationDTO> GetAllUser(@Filter Specification<User> spec , Pageable pageable) {
        return ResponseEntity.ok(this.userService.getAllUsers(spec,pageable));
    }

    @PutMapping
    @ApiMessage("Update user success")
    public ResponseEntity<UpdateUserResponse> UpdateUser(@Valid @RequestParam Long id,
                                                         @RequestParam String username,
                                                         @RequestParam(required = false) String address,
                                                         @RequestParam(required = false) GenderEnum gender,
                                                         @RequestParam Long roleId,
                                                         @RequestParam(required = false) MultipartFile images) throws AppException,IOException {
        return ResponseEntity.ok(this.userService.updateUser(id,username,address,gender,roleId,images));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete user success")
    public ResponseEntity<String> DeleteUser(@PathVariable Long id) throws AppException {
        this.userService.deleteUser(id);
        return ResponseEntity.ok("success");
    }
}

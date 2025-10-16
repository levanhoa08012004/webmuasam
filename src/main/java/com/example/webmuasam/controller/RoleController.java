package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Role;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.RoleService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ApiMessage("create role success")
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.createRole(role));
    }

    @PutMapping
    @ApiMessage(("update role success"))
    public ResponseEntity<Role> updateRole(@Valid @RequestBody Role role) throws AppException {
        return ResponseEntity.ok().body(this.roleService.updateRole(role));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("delete role success")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) throws AppException {
        this.roleService.deleteRole(id);
        return ResponseEntity.ok().body("sucess");
    }

    @GetMapping
    @ApiMessage("get all role success")
    public ResponseEntity<ResultPaginationDTO> getAllRoles(@Filter Specification<Role> spec, Pageable pageAble) {
        return ResponseEntity.ok().body(this.roleService.getAllRoles(spec, pageAble));
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch role by id")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id)  {
        return ResponseEntity.ok().body(this.roleService.fetchRoleById(id));
    }
}
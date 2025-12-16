package com.example.webmuasam.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Permission;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.PermissionService;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    @ApiMessage("create permission success")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.createPermission(permission));
    }

    @PutMapping
    @ApiMessage("update permission success")
    public ResponseEntity<Permission> updatePermission(@Valid @RequestBody Permission permission) throws AppException {
        return ResponseEntity.ok().body(this.permissionService.updatePermission(permission));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("delete permission success")
    public ResponseEntity<String> deletePermission(@PathVariable Long id) throws AppException {
        this.permissionService.deletePermission(id);
        return ResponseEntity.ok().body("success");
    }

    @GetMapping
    @ApiMessage("GET ALL PERMISSION SUCCESS")
    public ResponseEntity<ResultPaginationDTO> getAllPermission(
            @Filter Specification<Permission> specification, Pageable pageable) {
        return ResponseEntity.ok().body(this.permissionService.getAllPermission(specification, pageable));
    }
}

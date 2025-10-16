package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Permission;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.PermissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.nio.channels.AcceptPendingException;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission createPermission(Permission permission) throws AppException {
        if(this.permissionRepository.existsByApiPathAndMethodAndModule(permission.getApiPath(), permission.getMethod(), permission.getModule())) {
            throw new AppException("Permission đã tồn tại");
        }
        return permissionRepository.save(permission);
    }

    public Permission updatePermission(Permission permission) throws AppException {
        Permission oldPermission = this.permissionRepository.findById(permission.getId()).orElseThrow(()-> new AppException("Permission không tồn tại"));
        if(this.permissionRepository.existsByApiPathAndMethodAndModuleAndIdNot(permission.getApiPath(), permission.getMethod(), permission.getModule(),permission.getId())) {
            throw new AppException("Permission đã tồn tại");
        }
        if(oldPermission !=null){
            oldPermission.setName(permission.getName());
            oldPermission.setApiPath(permission.getApiPath());
            oldPermission.setMethod(permission.getMethod());
            oldPermission.setModule(permission.getModule());
            return this.permissionRepository.save(oldPermission);

        }
        return null;

    }
    public ResultPaginationDTO getAllPermission(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> permissions = this.permissionRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(permissions.getTotalElements());
        meta.setPages(permissions.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(permissions.getContent());
        return resultPaginationDTO;
    }

    public void deletePermission(long id) throws AppException {
        Permission permission = this.permissionRepository.findById(id).orElseThrow(()-> new AppException("Permission không tồn tại"));
        permission.getRoles().forEach(role -> role.getPermissions().remove(permission));
        this.permissionRepository.delete(permission);

    }



}

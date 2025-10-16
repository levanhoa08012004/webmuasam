package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Permission;
import com.example.webmuasam.entity.Role;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.PermissionRepository;
import com.example.webmuasam.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role fetchRoleById(Long id){
        Optional<Role> role = roleRepository.findById(id);
        if(role.isPresent()){
            return role.get();
        }
        return null;
    }

    public Role createRole(Role role) throws AppException {
        if(this.roleRepository.existsByName(role.getName())){
            throw new AppException("Role đã tồn tại");
        }
        if(role.getPermissions() != null && !role.getPermissions().isEmpty()){
            List<Long> permissionIds = role.getPermissions()
                    .stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList());
            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(permissionIds);
            role.setPermissions(dbPermissions);
        } else {
            role.setPermissions(new ArrayList<>()); // đảm bảo không null
        }
        return this.roleRepository.save(role);
    }

    public Role updateRole(Role role) throws AppException {
        Role currentRole = this.roleRepository.findById(role.getId()).orElseThrow(()->new AppException("role không tồn tại"));
        if(!role.getPermissions().isEmpty()){
            List<Long> permissionIds = role.getPermissions().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(permissionIds);
            currentRole.setPermissions(dbPermissions);
        }
        currentRole.setName(role.getName());
        currentRole.setDescription(role.getDescription());
        currentRole.setActive(role.isActive());

        return this.roleRepository.save(role);
    }
    public ResultPaginationDTO getAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageRole = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(pageRole.getTotalElements());
        meta.setPages(pageRole.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pageRole.getContent());
        return resultPaginationDTO;
    }

    public void deleteRole(Long id) throws AppException {
        Role role = this.roleRepository.findById(id).orElseThrow(()-> new AppException("id role không tồn tại"));
        this.roleRepository.delete(role);
        }
    }






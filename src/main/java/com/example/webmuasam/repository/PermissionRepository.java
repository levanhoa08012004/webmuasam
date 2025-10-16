package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Page<Permission> findAll(Specification<Permission> spec, Pageable pageable);
    boolean existsByApiPathAndMethodAndModuleAndIdNot(String apiPath, String method, String module,Long Id);
    boolean existsByApiPathAndMethodAndModule(String apiPath, String method, String module);

    List<Permission> findByIdIn(List<Long> id);
}

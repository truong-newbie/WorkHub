package org.example.workhub.service;

import org.example.workhub.domain.entity.Permission;

public interface PermissionService {
    Permission createPermission(Permission permission);
    Permission updatePermission(Permission permission);
    void deletePermission(Long id);
    //ResultPaginationDTO fetchAllPermission(Specification<Permission> spec, Pageable pageable);

    Permission fetchAPermission(Long id);
}

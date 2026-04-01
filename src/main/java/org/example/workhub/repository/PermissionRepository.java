package org.example.workhub.repository;

import org.example.workhub.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByApiPathIgnoreCaseAndMethodIgnoreCaseAndModuleIgnoreCase(String apiPath, String method, String module);

    //// Tìm tất cả người dùng có ID nằm trong danh sách providedIds
    List<Permission> findByIdIn(List<Long> ids);
}

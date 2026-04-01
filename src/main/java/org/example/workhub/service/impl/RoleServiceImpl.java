package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.entity.Permission;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.exception.InvalidException;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.PermissionRepository;
import org.example.workhub.repository.RoleRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public void checkValidRoleName(String name){
        if(roleRepository.existsByName(name)){
            throw new NotFoundException("Role voi name = " + name + " da ton tai!");
        }
    }

    public List<Permission> convertPermissionExist(List<Permission> permissions){
        if(permissions == null || permissions.isEmpty()){
            return new ArrayList<>();
        }
        List<Long> ids = permissions.stream()
                .map(Permission::getId)
                .toList();
        return permissionRepository.findByIdIn(ids);
    }

    @Override
    public Role createRole(Role role) {
        // check valid exist name
        this.checkValidRoleName(role.getName());

//        if(role.getPermissions() != null){
//            List<Permission> existPermissions = permissionRepository.findByIdIn(role.getPermissions().stream()
//                 //   .map(permission -> permission.getId()) // giong cai duoi
//                    .map(Permission::getId)
//                    .toList());
//            role.setPermissions(existPermissions);
//        }
        List<Permission> permissionExistList = this.convertPermissionExist(role.getPermissions());
        role.setPermissions(permissionExistList);

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole( Role roleDetails) {
        // 1. Tìm vai trò cần cập nhật
        Role existingRole = roleRepository.findById(roleDetails.getId())
                .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleDetails.getId()));

        // 2. Kiểm tra nếu tên mới có bị trùng với một vai trò khác không
        roleRepository.findByNameIgnoreCase(roleDetails.getName()).ifPresent(otherRole -> {
            if (!otherRole.getId().equals(existingRole.getId())) {
                throw new InvalidException("Role with " + roleDetails.getName() + " already exist");
            }
        });

        // 3. Cập nhật các thuộc tính và lưu
        existingRole.setName(roleDetails.getName());
        existingRole.setDescription(roleDetails.getDescription());
        return roleRepository.save(existingRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        // 1. Kiểm tra vai trò có tồn tại không
        if (!roleRepository.existsById(roleId)) {
            new NotFoundException("Role not found with id: " + roleId);
        }

        // 3. Nếu không có ai sử dụng, tiến hành xóa
        roleRepository.deleteById(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() ->  new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND_ROLE, new String[]{roleId.toString()}));
    }
}

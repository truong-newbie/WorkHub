package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.workhub.domain.entity.Permission;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.PermissionRepository;
import org.example.workhub.service.PermissionService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;


    private void checkValidExistPermission( String apiPath, String method,String module){
        if(permissionRepository.existsByApiPathIgnoreCaseAndMethodIgnoreCaseAndModuleIgnoreCase(apiPath, method, module )){
            throw new NotFoundException("Permission da ton tai!");
        }
    }

    @Override
    public Permission createPermission(Permission permission)  {
        this.checkValidExistPermission(permission.getApiPath(), permission.getMethod(), permission.getModule());

        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Permission permission) {
        //check exist id
        Permission permissionDB = this.fetchAPermission(permission.getId());
        // check exist by apiPath, method, module
        this.checkValidExistPermission(permission.getApiPath(), permission.getMethod(), permission.getModule());

        permissionDB.setName(permission.getName());
        permissionDB.setApiPath(permission.getApiPath());
        permissionDB.setMethod(permission.getMethod());
        permissionDB.setModule(permission.getModule());
        //update
        permissionDB = permissionRepository.save(permissionDB);

        return permissionDB;
    }

    @Override
    public void deletePermission(Long id) {
        // de delete permission -> can vo bang permission and role xoa di cai rang buoc
        // co thang role nao chua cai skill xoa cai skill trong bang roi xoa skill o bang skill
        Permission permissionDB = fetchAPermission(id);
        if(permissionDB.getRoles() != null){
            permissionDB.getRoles()
                    .forEach(role -> role.getPermissions().remove(permissionDB));
        }

        // delete permission
        permissionRepository.delete(permissionDB);
    }

//    @Override
//    public ResultPaginationDTO fetchAllPermission(Specification<Permission> spec, Pageable pageable) {
//        Page<Permission> pagePermission = permissionRepository.findAll(spec, pageable);
//        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
//        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
//        meta.setCurrent(pageable.getPageNumber() + 1);
//        meta.setPageSize(pageable.getPageSize());
//        meta.setPages(pagePermission.getTotalPages());
//        meta.setTotal(pagePermission.getTotalElements());
//
//        resultPaginationDTO.setMeta(meta);
//        resultPaginationDTO.setResult(pagePermission.getContent());
//
//        return resultPaginationDTO;
//    }

    @Override
    public Permission fetchAPermission(Long id) {
        return permissionRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Khong ton tai Permission voi id = " + id));
    }

}

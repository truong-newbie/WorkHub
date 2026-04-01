package org.example.workhub.service;

import org.example.workhub.domain.entity.Role;

public interface RoleService {

    Role createRole(Role role);


    Role updateRole( Role roleDetails);


    void deleteRole(Long roleId);


    Role getRoleById(Long roleId);
}

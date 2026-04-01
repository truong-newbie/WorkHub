package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.RestData;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.example.workhub.base.VsResponseUtil.error;
import static org.example.workhub.base.VsResponseUtil.success;

@RestApiV1
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;


    @PostMapping("/role")
    public ResponseEntity<RestData<?>> createRole(@RequestBody Role role){
        try{
            Role createdRole= roleService.createRole(role);

            return success(HttpStatus.OK, createdRole);

        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


//    @GetMapping("/roles")
//    public ResponseEntity<RestData<?>> getAllRoles(PaginationFullRequestDto request) {
//        PaginationResponseDto<Role> response = roleService.getAllRoles(request);
//        return success(HttpStatus.OK, response);
//    }


    @GetMapping("/role/{id}")
    public ResponseEntity<RestData<?>> getRoleById(@PathVariable Long id) {
        try{
            Role role = roleService.getRoleById(id);

            return success(HttpStatus.OK, role);

        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }


    @PutMapping("/role/{id}")
    public ResponseEntity<RestData<?>> updateRole(@Valid @RequestBody Role roleDetails) {
        try{
            Role updatedRole = roleService.updateRole(roleDetails);

            return success(HttpStatus.OK, updatedRole);

        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @DeleteMapping("/role/{id}")
    public ResponseEntity<RestData<?>> deleteRole(@PathVariable Long id) {

        try{
            roleService.deleteRole(id);

            return success(HttpStatus.OK, null);

        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

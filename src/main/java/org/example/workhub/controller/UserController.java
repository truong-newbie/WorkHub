package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.RestData;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;
import org.example.workhub.domain.dto.request.UserCreateDto;
import org.example.workhub.domain.dto.request.UserUpdateDto;
import org.example.workhub.security.UserPrincipal;
import org.example.workhub.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.workhub.base.VsResponseUtil.error;
import static org.example.workhub.base.VsResponseUtil.success;

@RestApiV1
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping(UrlConstant.User.GET_USER)
    public ResponseEntity<RestData<?>> getUser(@PathVariable String id){
        try{
            return success(HttpStatus.OK,userService.getUserById(id) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(UrlConstant.User.GET_CURRENT_USER)
    public ResponseEntity<RestData<?>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal){
        try{
            return success(HttpStatus.OK,userService.getCurrentUser(principal) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping(UrlConstant.User.CREATE_USER)
    public ResponseEntity<RestData<?>> createUser(@RequestBody @Valid UserCreateDto user){
        try{
            return success(HttpStatus.CREATED,userService.createUser(user) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(UrlConstant.User.UPDATE_USER)
    public ResponseEntity<RestData<?>> updateUser(@RequestBody @Valid UserUpdateDto user){
        try{
            return success(HttpStatus.OK,userService.updateUser(user) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(UrlConstant.User.DELETE_USER)
    public ResponseEntity<RestData<?>> deleteUser(@PathVariable String id){
        try{
            userService.deleteUser(id);
            return success(HttpStatus.OK, null);
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @GetMapping(UrlConstant.User.GET_USERS)
    public ResponseEntity<RestData<?>> getListUser(
            @RequestParam(value = "filter", required = false) List<String> filter,
            PaginationSortRequestDto pageable
    ){
        try{
            return success(HttpStatus.OK,userService.getListUser(filter,pageable) );
        } catch (Exception e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

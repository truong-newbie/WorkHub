package org.example.workhub.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.CompanyRequestDto;
import org.example.workhub.domain.dto.response.CompanyResponseDto;
import org.example.workhub.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class CompanyController {

    CompanyService companyService;

    @GetMapping(UrlConstant.Company.COMPANY_BASE)
    public ResponseEntity<?> getAll(PaginationFullRequestDto request) {
         return VsResponseUtil.success(companyService.getAll(request));
    }

    @GetMapping(UrlConstant.Company.ID)
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.getById(id));
    }

    @PostMapping(UrlConstant.Company.COMPANY_BASE)
    public ResponseEntity<?> create(@RequestBody @Valid CompanyRequestDto request) {
        return VsResponseUtil.success(companyService.create(request));
    }

    @PutMapping(UrlConstant.Company.ID)
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @RequestBody @Valid CompanyRequestDto request) {
        return VsResponseUtil.success(companyService.update(id, request));
    }

    @DeleteMapping(UrlConstant.Company.ID)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        companyService.delete(id);
        return VsResponseUtil.success("Company deleted successfully");
    }

}

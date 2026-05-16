package org.example.workhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.request.CompanyRequestDto;
import org.example.workhub.domain.dto.request.CompanySearchRequest;
import org.example.workhub.service.CompanyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@RestApiV1
public class CompanyController {

    CompanyService companyService;

    @Operation(summary = "Search companies", description = "Public users only see active and verified companies. Admin/recruiter can search all non-deleted companies.")
    @GetMapping(UrlConstant.Company.COMPANIES_BASE)
    public ResponseEntity<?> getAll(@ModelAttribute CompanySearchRequest request) {
         return VsResponseUtil.success(companyService.getAll(request));
    }

    @Operation(summary = "Get current recruiter company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping(UrlConstant.Company.ME)
    public ResponseEntity<?> getCurrentRecruiterCompany() {
        return VsResponseUtil.success(companyService.getCurrentRecruiterCompany());
    }

    @Operation(summary = "Get company detail")
    @GetMapping(UrlConstant.Company.ID)
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.getById(id));
    }

    @Operation(summary = "Create company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PostMapping(UrlConstant.Company.COMPANIES_BASE)
    public ResponseEntity<?> create(@RequestBody @Valid CompanyRequestDto request) {
        return VsResponseUtil.success(companyService.create(request));
    }

    @Operation(summary = "Update company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PutMapping(UrlConstant.Company.ID)
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @RequestBody @Valid CompanyRequestDto request) {
        return VsResponseUtil.success(companyService.update(id, request));
    }

    @Operation(summary = "Update current recruiter company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PutMapping(UrlConstant.Company.ME)
    public ResponseEntity<?> updateCurrentRecruiterCompany(@RequestBody @Valid CompanyRequestDto request) {
        return VsResponseUtil.success(companyService.updateCurrentRecruiterCompany(request));
    }

    @Operation(summary = "Upload company logo", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PostMapping(value = UrlConstant.Company.LOGO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLogo(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return VsResponseUtil.success(companyService.uploadLogo(id, file));
    }

    @Operation(summary = "Upload company cover image", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PostMapping(value = UrlConstant.Company.COVER, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCover(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return VsResponseUtil.success(companyService.uploadCover(id, file));
    }

    @Operation(summary = "Enable company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(UrlConstant.Company.ENABLE)
    public ResponseEntity<?> enable(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.enable(id));
    }

    @Operation(summary = "Disable company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(UrlConstant.Company.DISABLE)
    public ResponseEntity<?> disable(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.disable(id));
    }

    @Operation(summary = "Approve company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(UrlConstant.Company.APPROVE)
    public ResponseEntity<?> approve(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.approve(id));
    }

    @Operation(summary = "Reject company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(UrlConstant.Company.REJECT)
    public ResponseEntity<?> reject(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.reject(id));
    }

    @Operation(summary = "Get company jobs")
    @GetMapping(UrlConstant.Company.JOBS)
    public ResponseEntity<?> getCompanyJobs(@PathVariable Long id, @ModelAttribute CompanySearchRequest request) {
        return VsResponseUtil.success(companyService.getCompanyJobs(id, request));
    }

    @Operation(summary = "Get company statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping(UrlConstant.Company.STATISTICS)
    public ResponseEntity<?> getCompanyStatistics(@PathVariable Long id) {
        return VsResponseUtil.success(companyService.getCompanyStatistics(id));
    }

    @Operation(summary = "Delete company", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @DeleteMapping(UrlConstant.Company.ID)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        companyService.delete(id);
        return VsResponseUtil.success("company.delete.success");
    }
}

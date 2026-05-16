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
import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.request.SkillSearchRequest;
import org.example.workhub.service.SkillService;
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
import org.springframework.web.bind.annotation.RequestParam;

@RestApiV1
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Log4j2
public class SkillController {

    SkillService skillService;

    @Operation(summary = "Create skill", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(UrlConstant.Skill.SKILL_BASE)
    public ResponseEntity<?> create(@RequestBody @Valid SkillRequestDto req) {
        return VsResponseUtil.success(skillService.create(req));
    }

    @Operation(summary = "Update skill", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(UrlConstant.Skill.ID)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid SkillRequestDto req) {
        return VsResponseUtil.success(skillService.update(id, req));
    }

    @Operation(summary = "Get skill by id")
    @GetMapping(UrlConstant.Skill.ID)
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return VsResponseUtil.success(skillService.getById(id));
    }

    @Operation(summary = "Search skills")
    @GetMapping({UrlConstant.Skill.SKILL_BASE, UrlConstant.Skill.SEARCH})
    public ResponseEntity<?> getAll(@ModelAttribute SkillSearchRequest req) {
        return VsResponseUtil.success(skillService.getAll(req));
    }

    @Operation(summary = "Enable skill", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(UrlConstant.Skill.ENABLE)
    public ResponseEntity<?> enable(@PathVariable Long id) {
        return VsResponseUtil.success(skillService.enable(id));
    }

    @Operation(summary = "Disable skill", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(UrlConstant.Skill.DISABLE)
    public ResponseEntity<?> disable(@PathVariable Long id) {
        return VsResponseUtil.success(skillService.disable(id));
    }

    @Operation(summary = "Get skill suggestions")
    @GetMapping(UrlConstant.Skill.SUGGESTIONS)
    public ResponseEntity<?> getSuggestions(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return VsResponseUtil.success(skillService.getSuggestions(keyword, limit));
    }

    @Operation(summary = "Get popular skills")
    @GetMapping(UrlConstant.Skill.POPULAR)
    public ResponseEntity<?> getPopularSkills(@RequestParam(required = false, defaultValue = "10") int limit) {
        return VsResponseUtil.success(skillService.getPopularSkills(limit));
    }

    @Operation(summary = "Delete skill", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(UrlConstant.Skill.ID)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        skillService.delete(id);
        return VsResponseUtil.success("skill.delete.success");
    }
}

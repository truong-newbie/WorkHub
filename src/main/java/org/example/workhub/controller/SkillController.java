package org.example.workhub.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.example.workhub.base.RestApiV1;
import org.example.workhub.base.VsResponseUtil;
import org.example.workhub.constant.UrlConstant;
import org.example.workhub.domain.dto.pagination.PaginationFullRequestDto;
import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestApiV1
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@Validated
@Log4j2
public class SkillController {
    SkillService skillService;

    @PostMapping(UrlConstant.Skill.SKILL_BASE)
    public ResponseEntity<?>  create(SkillRequestDto req){
        return VsResponseUtil.success(skillService.create(req));
    }

    @PutMapping(UrlConstant.Skill.ID)
    public ResponseEntity<?> update (@PathVariable long id, SkillRequestDto req){
        return VsResponseUtil.success(skillService.update(id, req));
    }

    @PostMapping(UrlConstant.Skill.ID)
    public ResponseEntity<?> getById(@PathVariable long id){
        return VsResponseUtil.success(skillService.getById(id));
    }

    @GetMapping(UrlConstant.Skill.SKILL_BASE)
    public ResponseEntity<?> getAll(PaginationFullRequestDto req){
        return VsResponseUtil.success(skillService.getAll(req));
    }

    @DeleteMapping(UrlConstant.Skill.ID)
    public ResponseEntity<?> delete(long id){
        skillService.delete(id);
        return VsResponseUtil.success("Skill Deleted Successfully");
    }

}

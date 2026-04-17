package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.response.SkillResponseDto;
import org.example.workhub.domain.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(dto.getName().trim())")
    @Mapping(target ="level", source = "level")
    Skill toEntity(SkillRequestDto dto);

    SkillResponseDto toDto(Skill skill);
}
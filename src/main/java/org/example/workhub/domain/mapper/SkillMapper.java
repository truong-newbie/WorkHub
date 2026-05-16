package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.SkillRequestDto;
import org.example.workhub.domain.dto.response.PopularSkillResponse;
import org.example.workhub.domain.dto.response.SkillResponseDto;
import org.example.workhub.domain.dto.response.SkillSuggestionResponse;
import org.example.workhub.domain.entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    default Skill toEntity(SkillRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setName(dto.getName().trim());
        skill.setLevel(trimToNull(dto.getLevel()));
        skill.setDescription(trimToNull(dto.getDescription()));
        skill.setActive(dto.getActive() == null || dto.getActive());
        return skill;
    }

    SkillResponseDto toDto(Skill skill);

    SkillSuggestionResponse toSuggestion(Skill skill);

    default PopularSkillResponse toPopular(Skill skill, Long usageCount) {
        if (skill == null) {
            return null;
        }
        return PopularSkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .slug(skill.getSlug())
                .level(skill.getLevel())
                .usageCount(usageCount == null ? 0L : usageCount)
                .build();
    }

    default String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

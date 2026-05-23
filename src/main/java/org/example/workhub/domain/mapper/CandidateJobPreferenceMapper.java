package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.CandidateJobPreferenceCreateRequest;
import org.example.workhub.domain.dto.request.CandidateJobPreferenceUpdateRequest;
import org.example.workhub.domain.dto.response.CandidateJobPreferenceResponse;
import org.example.workhub.domain.entity.CandidateJobPreference;
import org.example.workhub.domain.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CandidateJobPreferenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    CandidateJobPreference toEntity(CandidateJobPreferenceCreateRequest request);

    @Mapping(target = "candidateId", expression = "java(entity.getCandidate() == null ? null : entity.getCandidate().getId())")
    @Mapping(target = "skills", expression = "java(mapSkills(entity))")
    CandidateJobPreferenceResponse toResponse(CandidateJobPreference entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntityFromRequest(CandidateJobPreferenceUpdateRequest request, @MappingTarget CandidateJobPreference entity);

    default List<CandidateJobPreferenceResponse.SkillInfo> mapSkills(CandidateJobPreference entity) {
        if (entity == null || entity.getSkills() == null) {
            return List.of();
        }
        return entity.getSkills().stream()
                .sorted(Comparator.comparing(Skill::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(skill -> CandidateJobPreferenceResponse.SkillInfo.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .slug(skill.getSlug())
                        .build())
                .toList();
    }
}

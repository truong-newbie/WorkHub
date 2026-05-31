package org.example.workhub.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.workhub.domain.dto.response.ScreeningResultResponse;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.ScreeningResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScreeningResultMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    default ScreeningResultResponse toResponse(ScreeningResult result) {
        if (result == null) {
            return null;
        }
        JobApplication application = result.getApplication();
        return ScreeningResultResponse.builder()
                .id(result.getId())
                .applicationId(application != null ? application.getId() : null)
                .candidateId(application != null && application.getUser() != null ? application.getUser().getId() : null)
                .candidateName(application != null && application.getUser() != null ? application.getUser().getUsername() : null)
                .jobId(application != null && application.getJob() != null ? application.getJob().getId() : null)
                .jobTitle(application != null && application.getJob() != null ? application.getJob().getTitle() : null)
                .totalScore(result.getTotalScore())
                .skillScore(result.getSkillScore())
                .semanticScore(result.getSemanticScore())
                .experienceScore(result.getExperienceScore())
                .educationScore(result.getEducationScore())
                .matchedSkills(readList(result.getMatchedSkills()))
                .missingSkills(readList(result.getMissingSkills()))
                .extraSkills(readList(result.getExtraSkills()))
                .aiSummary(result.getAiSummary())
                .screenedAt(result.getCreatedDate())
                .build();
    }

    default List<ScreeningResultResponse> toResponses(List<ScreeningResult> results) {
        return results == null ? List.of() : results.stream().map(this::toResponse).toList();
    }

    default List<String> readList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}

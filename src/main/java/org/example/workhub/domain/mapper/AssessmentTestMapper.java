package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.AssessmentTestCreateRequest;
import org.example.workhub.domain.dto.request.AssessmentTestUpdateRequest;
import org.example.workhub.domain.dto.response.AssessmentTestResponse;
import org.example.workhub.domain.entity.AssessmentTest;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = AssessmentQuestionMapper.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssessmentTestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "recruiter", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    AssessmentTest toEntity(AssessmentTestCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "recruiter", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(AssessmentTestUpdateRequest request, @MappingTarget AssessmentTest test);

    default AssessmentTestResponse toResponse(AssessmentTest test, AssessmentQuestionMapper questionMapper, boolean includeCorrect) {
        if (test == null) return null;
        return AssessmentTestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .durationMinutes(test.getDurationMinutes())
                .startAt(test.getStartAt())
                .endAt(test.getEndAt())
                .status(test.getStatus())
                .jobId(test.getJob() != null ? test.getJob().getId() : null)
                .jobTitle(test.getJob() != null ? test.getJob().getTitle() : null)
                .recruiterId(test.getRecruiter() != null ? test.getRecruiter().getId() : null)
                .recruiterName(test.getRecruiter() != null ? test.getRecruiter().getUsername() : null)
                .createdDate(test.getCreatedDate())
                .lastModifiedDate(test.getLastModifiedDate())
                .questions(questionMapper.toResponses(test.getQuestions(), includeCorrect))
                .build();
    }
}

package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.AssessmentOptionRequest;
import org.example.workhub.domain.dto.request.AssessmentQuestionCreateRequest;
import org.example.workhub.domain.dto.request.AssessmentQuestionUpdateRequest;
import org.example.workhub.domain.dto.response.AssessmentOptionResponse;
import org.example.workhub.domain.dto.response.AssessmentQuestionResponse;
import org.example.workhub.domain.entity.AssessmentOption;
import org.example.workhub.domain.entity.AssessmentQuestion;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssessmentQuestionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "options", ignore = true)
    AssessmentQuestion toEntity(AssessmentQuestionCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "options", ignore = true)
    void updateEntity(AssessmentQuestionUpdateRequest request, @MappingTarget AssessmentQuestion question);

    @Mapping(target = "options", expression = "java(toOptionResponses(question.getOptions(), includeCorrect))")
    AssessmentQuestionResponse toResponse(AssessmentQuestion question, @Context boolean includeCorrect);

    default List<AssessmentQuestionResponse> toResponses(List<AssessmentQuestion> questions, boolean includeCorrect) {
        if (questions == null) return Collections.emptyList();
        return questions.stream()
                .filter(question -> !Boolean.TRUE.equals(question.getDeleted()))
                .map(question -> toResponse(question, includeCorrect))
                .toList();
    }

    default AssessmentOption toOptionEntity(AssessmentOptionRequest request, AssessmentQuestion question) {
        AssessmentOption option = new AssessmentOption();
        option.setContent(request.getContent());
        option.setCorrect(Boolean.TRUE.equals(request.getCorrect()));
        option.setQuestion(question);
        return option;
    }

    default List<AssessmentOptionResponse> toOptionResponses(List<AssessmentOption> options, boolean includeCorrect) {
        if (options == null) return Collections.emptyList();
        return options.stream()
                .map(option -> AssessmentOptionResponse.builder()
                        .id(option.getId())
                        .content(option.getContent())
                        .correct(includeCorrect ? option.getCorrect() : null)
                        .build())
                .toList();
    }
}

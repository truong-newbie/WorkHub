package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.CandidateAnswerResponse;
import org.example.workhub.domain.entity.CandidateAnswer;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CandidateAnswerMapper {

    default CandidateAnswerResponse toResponse(CandidateAnswer answer) {
        if (answer == null) return null;
        return CandidateAnswerResponse.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion() != null ? answer.getQuestion().getId() : null)
                .questionContent(answer.getQuestion() != null ? answer.getQuestion().getContent() : null)
                .selectedOptionId(answer.getSelectedOption() != null ? answer.getSelectedOption().getId() : null)
                .selectedOptionContent(answer.getSelectedOption() != null ? answer.getSelectedOption().getContent() : null)
                .essayAnswer(answer.getEssayAnswer())
                .score(answer.getScore())
                .maxScore(answer.getQuestion() != null ? answer.getQuestion().getScore() : null)
                .feedback(answer.getFeedback())
                .build();
    }

    default List<CandidateAnswerResponse> toResponses(List<CandidateAnswer> answers) {
        if (answers == null) return Collections.emptyList();
        return answers.stream().map(this::toResponse).toList();
    }
}

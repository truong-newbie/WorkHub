package org.example.workhub.service.impl;

import org.example.workhub.domain.dto.response.AiResumeAnalysisResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorServiceImplTest {

    private final ScoreCalculatorServiceImpl scoreCalculator = new ScoreCalculatorServiceImpl();

    @Test
    void shouldUseFinalScoreReturnedByAiWorker() {
        AiResumeAnalysisResponse response = new AiResumeAnalysisResponse();
        response.setSkillScore(100D);
        response.setSemanticScore(80D);
        response.setFinalScore(92D);

        assertEquals(92D, scoreCalculator.calculateTotalScore(response));
    }

    @Test
    void shouldKeepBackwardCompatibleCalculationWhenFinalScoreIsMissing() {
        AiResumeAnalysisResponse response = new AiResumeAnalysisResponse();
        response.setSkillScore(80D);
        response.setSemanticScore(50D);

        assertEquals(68D, scoreCalculator.calculateTotalScore(response));
    }
}

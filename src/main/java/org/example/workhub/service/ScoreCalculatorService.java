package org.example.workhub.service;

import org.example.workhub.domain.dto.response.AiResumeAnalysisResponse;

public interface ScoreCalculatorService {
    Double calculateTotalScore(AiResumeAnalysisResponse response);
}

package org.example.workhub.service.impl;

import org.example.workhub.domain.dto.response.AiResumeAnalysisResponse;
import org.example.workhub.service.ScoreCalculatorService;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorServiceImpl implements ScoreCalculatorService {

    @Override
    public Double calculateTotalScore(AiResumeAnalysisResponse response) {
        if (response == null || response.getSkillScore() == null) {
            return 0D;
        }
        Double semanticScore = response.getSemanticScore();
        if (semanticScore == null || semanticScore == 0D) {
            return round(response.getSkillScore());
        }
        return round(response.getSkillScore() * 0.6D + semanticScore * 0.4D);
    }

    private Double round(Double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }
}

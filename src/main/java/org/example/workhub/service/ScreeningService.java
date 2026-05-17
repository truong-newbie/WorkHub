package org.example.workhub.service;

import org.example.workhub.domain.dto.response.ScreeningResultResponse;

import java.util.List;

public interface ScreeningService {
    ScreeningResultResponse screenApplication(Long applicationId);

    ScreeningResultResponse getScreeningResult(Long applicationId);

    List<ScreeningResultResponse> getJobScreeningResults(Long jobId);
}

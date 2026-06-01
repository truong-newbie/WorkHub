package org.example.workhub.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.workhub.domain.dto.response.ScreeningResultResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.ScreeningResult;
import org.example.workhub.domain.mapper.ScreeningResultMapper;
import org.example.workhub.repository.JobApplicationRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.ScreeningResultRepository;
import org.example.workhub.repository.UserRepository;
import org.example.workhub.service.AiWorkerClient;
import org.example.workhub.service.ScoreCalculatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceImplTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private ScreeningResultRepository screeningResultRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiWorkerClient aiWorkerClient;

    @Mock
    private ScoreCalculatorService scoreCalculatorService;

    @Mock
    private ScreeningResultMapper screeningResultMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ScreeningServiceImpl screeningService;

    @Test
    void processQueuedScreeningReusesPersistedResultForSameResumeAndJob() {
        Resume resume = new Resume();
        resume.setId(11L);
        Job job = new Job();
        job.setId(22L);
        JobApplication application = new JobApplication();
        application.setId(33L);
        application.setResume(resume);
        application.setJob(job);
        ScreeningResult cachedResult = new ScreeningResult();
        cachedResult.setId(44L);
        cachedResult.setApplication(application);
        cachedResult.setRecommendation("PASS");
        cachedResult.setExplanationStatus("CALCULATED");
        ScreeningResultResponse expected = ScreeningResultResponse.builder()
                .id(cachedResult.getId())
                .recommendation("PASS")
                .build();

        when(applicationRepository.findByIdAndDeletedFalse(application.getId()))
                .thenReturn(Optional.of(application));
        when(screeningResultRepository
                .findFirstByApplicationResumeIdAndApplicationJobIdAndExplanationStatusOrderByIdDesc(
                        resume.getId(), job.getId(), "CALCULATED"))
                .thenReturn(Optional.of(cachedResult));
        when(screeningResultRepository.findByApplicationId(application.getId()))
                .thenReturn(Optional.of(cachedResult));
        when(screeningResultRepository.save(cachedResult)).thenReturn(cachedResult);
        when(screeningResultMapper.toResponse(cachedResult)).thenReturn(expected);

        ScreeningResultResponse actual = screeningService.processQueuedScreening(
                application.getId());

        assertThat(actual).isSameAs(expected);
        verifyNoInteractions(aiWorkerClient);
        verify(screeningResultRepository).save(cachedResult);
    }
}

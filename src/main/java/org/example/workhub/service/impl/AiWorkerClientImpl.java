package org.example.workhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.domain.dto.response.AiResumeAnalysisResponse;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.exception.InternalServerException;
import org.example.workhub.service.AiWorkerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiWorkerClientImpl implements AiWorkerClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(90);
    private static final List<String> COMMON_SKILLS = List.of(
            "Java", "Spring Boot", "Docker", "Redis", "MySQL", "PostgreSQL",
            "React", "Angular", "AWS", "Kafka", "Kubernetes", "Python"
    );

    private final RestTemplate restTemplate = createRestTemplate();

    @Value("${ai.worker.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${ai.worker.mock-enabled:true}")
    private Boolean mockEnabled;

    @Override
    public AiResumeAnalysisResponse analyzeResume(Resume resume, Long jobId, String jobTitle, String jobDescription, List<String> jobSkills) {
        if (Boolean.TRUE.equals(mockEnabled)) {
            return buildMockResponse(resume, jobDescription, jobSkills);
        }

        try {
            byte[] fileBytes;
            try (InputStream stream = URI.create(resume.getFileUrl()).toURL().openStream()) {
                fileBytes = stream.readAllBytes();
            }
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("job_description", jobDescription);
            body.add("required_skills", String.join(",", jobSkills == null ? List.of() : jobSkills));
            body.add("job_title", jobTitle == null ? "" : jobTitle);
            body.add("resume_id", resume.getId());
            body.add("job_id", jobId);
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return resume.getFileName();
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ResponseEntity<AiResumeAnalysisResponse> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/ai/resume/analyze",
                    new HttpEntity<>(body, headers),
                    AiResumeAnalysisResponse.class
            );
            AiResumeAnalysisResponse result = response.getBody();
            if (result == null) {
                throw new InternalServerException(ErrorMessage.AiWorker.ERR_UNAVAILABLE);
            }
            return result;
        } catch (IOException | IllegalArgumentException | RestClientException ex) {
            log.warn("AI worker analysis failed resumeId={} jobId={} cause={}: {}",
                    resume.getId(), jobId, ex.getClass().getSimpleName(), ex.getMessage());
            throw new InternalServerException(ErrorMessage.AiWorker.ERR_UNAVAILABLE);
        }
    }

    private AiResumeAnalysisResponse buildMockResponse(Resume resume, String jobDescription, List<String> jobSkills) {
        List<String> resumeSkills = extractResumeSkills(resume);
        List<String> requiredSkills = jobSkills == null ? List.of() : jobSkills;
        Set<String> matched = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();

        for (String jobSkill : requiredSkills) {
            if (containsIgnoreCase(resumeSkills, jobSkill)) {
                matched.add(jobSkill);
            } else {
                missing.add(jobSkill);
            }
        }

        Set<String> extra = new LinkedHashSet<>();
        for (String resumeSkill : resumeSkills) {
            if (!containsIgnoreCase(requiredSkills, resumeSkill)) {
                extra.add(resumeSkill);
            }
        }

        double skillScore = requiredSkills.isEmpty() ? 0D : matched.size() * 100D / requiredSkills.size();
        AiResumeAnalysisResponse response = new AiResumeAnalysisResponse();
        response.setRawText(resume.getParsedContent());
        response.setResumeSkills(resumeSkills);
        response.setJobSkills(requiredSkills);
        response.setMatchedSkills(new ArrayList<>(matched));
        response.setMissingSkills(new ArrayList<>(missing));
        response.setExtraSkills(new ArrayList<>(extra));
        response.setSkillScore(Math.round(skillScore * 100D) / 100D);
        response.setSemanticScore(0D);
        response.setFinalScore(response.getSkillScore());
        response.setStrengths(matched.isEmpty()
                ? List.of("No required skill keyword match was detected")
                : List.of("Matched required skills: " + String.join(", ", matched)));
        response.setWeaknesses(missing.isEmpty()
                ? List.of("No required skill gaps were detected")
                : List.of("Missing required skills: " + String.join(", ", missing)));
        response.setRecommendation(recommendationForScore(response.getFinalScore()));
        response.setConfidence(response.getFinalScore());
        response.setSummary("Mock ATS analysis generated by Spring Boot because ai.worker.mock-enabled=true.");
        response.setExplanationStatus("SKIPPED_MOCK");
        response.setAiSummary(response.getSummary());
        return response;
    }

    private List<String> extractResumeSkills(Resume resume) {
        Set<String> skills = new LinkedHashSet<>();
        if (resume.getSkills() != null) {
            resume.getSkills().stream().map(Skill::getName).forEach(skills::add);
        }
        String text = (resume.getParsedContent() == null ? "" : resume.getParsedContent()) + " " + resume.getTitle();
        String lowerText = text.toLowerCase(Locale.ROOT);
        COMMON_SKILLS.stream()
                .filter(skill -> lowerText.contains(skill.toLowerCase(Locale.ROOT)))
                .forEach(skills::add);
        return new ArrayList<>(skills);
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return new RestTemplate(requestFactory);
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        if (values == null || target == null) {
            return false;
        }
        return values.stream().anyMatch(value -> target.equalsIgnoreCase(value));
    }

    private String recommendationForScore(Double score) {
        if (score >= 80D) {
            return "PASS";
        }
        if (score >= 60D) {
            return "CONSIDER";
        }
        return "REJECT";
    }
}

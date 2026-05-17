package org.example.workhub.service;

import org.example.workhub.domain.dto.response.AiResumeAnalysisResponse;
import org.example.workhub.domain.entity.Resume;

import java.util.List;

public interface AiWorkerClient {
    AiResumeAnalysisResponse analyzeResume(Resume resume, String jobDescription, List<String> jobSkills);
}

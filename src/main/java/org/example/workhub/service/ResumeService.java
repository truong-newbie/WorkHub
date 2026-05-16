package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.request.ResumeSearchRequest;
import org.example.workhub.domain.dto.request.ResumeUpdateRequest;
import org.example.workhub.domain.dto.request.ResumeUploadRequest;
import org.example.workhub.domain.dto.response.ResumeDownloadResponse;
import org.example.workhub.domain.dto.response.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeResponse uploadResume(ResumeUploadRequest request);

    ResumeResponse updateResume(Long id, ResumeUpdateRequest request);

    ResumeResponse updateResumeFile(Long id, MultipartFile file);

    ResumeResponse deleteResume(Long id);

    ResumeResponse getResumeDetail(Long id);

    PaginationResponseDto<ResumeResponse> getMyResumes(ResumeSearchRequest request);

    PaginationResponseDto<ResumeResponse> getAllResumes(ResumeSearchRequest request);

    ResumeResponse setDefaultResume(Long id);

    ResumeResponse getCandidateResumeForJob(Long jobId, String candidateId);

    ResumeDownloadResponse downloadMyResume(Long id);

    ResumeDownloadResponse downloadCandidateResumeForJob(Long jobId, String candidateId);
}

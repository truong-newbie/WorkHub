package org.example.workhub.service;

import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;
import org.example.workhub.domain.dto.request.job.JobCreateDto;
import org.example.workhub.domain.dto.request.job.JobUpdateDto;
import org.example.workhub.domain.dto.response.JobDto;
import org.example.workhub.domain.entity.Job;

import java.util.List;

public interface JobService {

    JobDto createJob(JobCreateDto jobCreateDto);

    JobDto updateJob(JobUpdateDto jobUpdateDto);

    JobDto getJob(Long id);

    void deleteJob(Long id);

    PaginationResponseDto<JobDto> getAllJobs(List<String> filter, PaginationSortRequestDto paginationSortRequestDto);


}

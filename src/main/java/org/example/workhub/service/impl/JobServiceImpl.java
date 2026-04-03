package org.example.workhub.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.example.workhub.config.OpenApiConfig;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.SortByDataConstant;
import org.example.workhub.domain.dto.pagination.PaginationResponseDto;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;
import org.example.workhub.domain.dto.pagination.PagingMeta;
import org.example.workhub.domain.dto.request.job.JobCreateDto;
import org.example.workhub.domain.dto.request.job.JobUpdateDto;
import org.example.workhub.domain.dto.response.JobDto;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.mapper.JobMapper;
import org.example.workhub.domain.specification.FilterProcessor;
import org.example.workhub.domain.specification.SpecificationBuilder;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.repository.CompanyRepository;
import org.example.workhub.repository.JobRepository;
import org.example.workhub.repository.SkillRepository;
import org.example.workhub.service.JobService;
import org.example.workhub.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final JobMapper jobMapper;
    @Override
    public JobDto createJob(JobCreateDto jobCreateDto) {
        if(jobRepository.existsByNameIgnoreCase(jobCreateDto.getName())){
            throw new NotFoundException(ErrorMessage.Job.ERR_EXISTED_JOB, new String[]{jobCreateDto.getName()});
        }
        //chuyển DTO sang Entity trước
        Job createJob = jobMapper.toJob(jobCreateDto);
        createJob.setActive(true);

        // set Company
            if(jobCreateDto.getCompany() != null && jobCreateDto.getCompany().getId() != null) {
                Company company = companyRepository.findById(jobCreateDto.getCompany().getId()).orElseThrow(
                        () -> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND_COMPANY, new String[]{jobCreateDto.getCompany().getId().toString()})
                );
                createJob.setCompany(company);
            }
        // set Skill
            if(jobCreateDto.getSkillIds() != null && !jobCreateDto.getSkillIds().isEmpty()){
                List<Skill> skills = skillRepository.findByIdIn(jobCreateDto.getSkillIds());
                if(skills != null){
                    createJob.setSkills(skills);
                }
            }

            // lưu vào DB
            createJob = jobRepository.save(createJob);


            return jobMapper.toJobDto(createJob);

    }

    @Override
    public JobDto updateJob(JobUpdateDto jobUpdateDto) {
        Job updateJob = jobRepository.findById(jobUpdateDto.getId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_SKILL, new String[]{jobUpdateDto.getId().toString()})
        );
        if(jobRepository.existsByNameIgnoreCase(jobUpdateDto.getName().trim())){
            throw new NotFoundException(ErrorMessage.Job.ERR_EXISTED_JOB, new String[]{jobUpdateDto.getName()});
        }else {
            updateJob.setName(jobUpdateDto.getName());
        }


        updateJob.setLocation(jobUpdateDto.getLocation());
        updateJob.setSalary(jobUpdateDto.getSalary());
        updateJob.setQuantity(jobUpdateDto.getQuantity());
        updateJob.setLevel(jobUpdateDto.getLevel());
        updateJob.setDescription(jobUpdateDto.getDescription());
        updateJob.setStartDate(jobUpdateDto.getStartDate());
        updateJob.setEndDate(jobUpdateDto.getEndDate());
        updateJob.setActive(jobUpdateDto.getActive());

        // set Company
        if(jobUpdateDto.getCompany() != null && jobUpdateDto.getCompany().getId() != null) {
            Company company = companyRepository.findById(jobUpdateDto.getCompany().getId()).orElseThrow(
                    () -> new NotFoundException(ErrorMessage.Company.ERR_NOT_FOUND_COMPANY, new String[]{jobUpdateDto.getCompany().getId().toString()})
            );
            updateJob.setCompany(company);
        }
        // set Skill
        if(jobUpdateDto.getSkillIds() != null && !jobUpdateDto.getSkillIds().isEmpty()){
            List<Skill> skills = skillRepository.findByIdIn(jobUpdateDto.getSkillIds());
            if(skills != null){
                updateJob.setSkills(skills);
            }
        }

        updateJob =  jobRepository.save(updateJob);



        return jobMapper.toJobDto(updateJob);
    }

    @Override
    public JobDto getJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_SKILL, new String[]{id.toString()})
        );
        return jobMapper.toJobDto(job);
    }

    @Override
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Job.ERR_NOT_FOUND_SKILL, new String[]{id.toString()})
        );
        jobRepository.delete(job);
    }

    @Override
    public PaginationResponseDto<JobDto> getAllJobs(List<String> filter, PaginationSortRequestDto paginationSortRequestDto) {
        SpecificationBuilder<Job> specificationBuilder = new SpecificationBuilder<>();

        FilterProcessor processor = FilterProcessor.process(specificationBuilder, filter );

        Pageable pageable = PaginationUtil.buildPageable(paginationSortRequestDto, SortByDataConstant.JOB);

        Page<Job> page = jobRepository.findAll(specificationBuilder.build(), pageable);

        PagingMeta pagingMeta = PaginationUtil.buildPagingMeta(paginationSortRequestDto, SortByDataConstant.JOB, page );
        List<JobDto> jobDtos = jobMapper.toJobDtoList(page.getContent());



        return new PaginationResponseDto<>(pagingMeta, jobDtos);
    }
}

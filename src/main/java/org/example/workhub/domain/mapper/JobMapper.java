package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.JobCreateRequest;
import org.example.workhub.domain.dto.request.JobUpdateRequest;
import org.example.workhub.domain.dto.response.JobResponse;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "requirement", ignore = true)
    @Mapping(target = "benefit", ignore = true)
    @Mapping(target = "salaryMin", ignore = true)
    @Mapping(target = "salaryMax", ignore = true)
    @Mapping(target = "negotiableSalary", ignore = true)
    @Mapping(target = "employmentType", ignore = true)
    @Mapping(target = "experienceYears", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "expiredAt", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "recruiter", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateFromRequest(JobUpdateRequest request, @MappingTarget Job job);

    @Mapping(target = "company", expression = "java(mapCompanyBasicInfo(job.getCompany()))")
    @Mapping(target = "recruiter", expression = "java(mapRecruiterInfo(job.getRecruiter()))")
    @Mapping(target = "skills", expression = "java(mapSkills(job.getSkills()))")
    @Mapping(target = "applicationCount", expression = "java(job.getResumes() != null ? job.getResumes().size() : 0)")
    @Mapping(target = "expiredAt", expression = "java(mapExpiredAt(job.getExpiredAt()))")
    @Mapping(target = "startDate", expression = "java(mapStartDate(job.getStartDate()))")
    @Mapping(target = "expired", expression = "java(isExpired(job))")
    JobResponse toResponse(Job job);

    List<JobResponse> toResponses(List<Job> jobs);

    default JobResponse.CompanyBasicInfo mapCompanyBasicInfo(Company company) {
        if (company == null) return null;
        return JobResponse.CompanyBasicInfo.builder()
                .id(company.getId())
                .name(company.getName())
                .logo(company.getLogo())
                .address(company.getAddress())
                .build();
    }

    default JobResponse.RecruiterInfo mapRecruiterInfo(User recruiter) {
        if (recruiter == null) return null;
        return JobResponse.RecruiterInfo.builder()
                .id(recruiter.getId())
                .username(recruiter.getUsername())
                .email(recruiter.getEmail())
                .avatar(recruiter.getAvatar())
                .build();
    }

    default List<JobResponse.SkillInfo> mapSkills(List<Skill> skills) {
        if (skills == null) return null;
        return skills.stream()
                .map(skill -> JobResponse.SkillInfo.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .level(skill.getLevel())
                        .build())
                .collect(Collectors.toList());
    }

    default boolean isExpired(Job job) {
        if (job.getExpiredAt() == null) return false;
        return job.getExpiredAt().isBefore(java.time.LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
    }

    default java.time.LocalDate mapExpiredAt(java.time.Instant instant) {
        if (instant == null) return null;
        return instant.atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }

    default java.time.LocalDate mapStartDate(java.time.Instant instant) {
        if (instant == null) return null;
        return instant.atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }
}
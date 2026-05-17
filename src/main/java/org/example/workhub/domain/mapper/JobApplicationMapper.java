package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.request.JobApplicationRequest;
import org.example.workhub.domain.dto.response.JobApplicationResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.JobApplication;
import org.example.workhub.domain.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewNote", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateFromRequest(JobApplicationRequest request, @MappingTarget JobApplication application);

    @Mapping(target = "job", expression = "java(mapJobBasicInfo(application.getJob()))")
    @Mapping(target = "candidate", expression = "java(mapCandidateInfo(application.getUser()))")
    @Mapping(target = "resumeId", expression = "java(application.getResume() != null ? application.getResume().getId() : null)")
    JobApplicationResponse toResponse(JobApplication application);

    List<JobApplicationResponse> toResponses(List<JobApplication> applications);

    default JobApplicationResponse.JobBasicInfo mapJobBasicInfo(Job job) {
        if (job == null) return null;
        String companyName = job.getCompany() != null ? job.getCompany().getName() : null;
        return JobApplicationResponse.JobBasicInfo.builder()
                .id(job.getId())
                .title(job.getTitle())
                .location(job.getLocation())
                .companyName(companyName)
                .build();
    }

    default JobApplicationResponse.CandidateInfo mapCandidateInfo(User candidate) {
        if (candidate == null) return null;
        return JobApplicationResponse.CandidateInfo.builder()
                .id(candidate.getId())
                .username(candidate.getUsername())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .avatar(candidate.getAvatar())
                .headline(candidate.getHeadline())
                .build();
    }
}

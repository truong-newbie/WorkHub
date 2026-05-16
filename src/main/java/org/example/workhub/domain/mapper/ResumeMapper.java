package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.ResumeDownloadResponse;
import org.example.workhub.domain.dto.response.ResumeResponse;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResumeMapper {

    default ResumeResponse toResponse(Resume resume) {
        if (resume == null) {
            return null;
        }
        return ResumeResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .fileName(resume.getFileName())
                .fileUrl(resume.getFileUrl())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .isDefault(resume.getIsDefault())
                .isPublic(resume.getIsPublic())
                .deleted(resume.getDeleted())
                .summary(resume.getSummary())
                .atsScore(resume.getAtsScore())
                .parsedContent(resume.getParsedContent())
                .status(resume.getStatus())
                .uploadedAt(resume.getUploadedAt())
                .candidate(mapCandidate(resume.getUser()))
                .job(mapJob(resume.getJob()))
                .skills(mapSkills(resume.getSkills()))
                .createdDate(resume.getCreatedDate())
                .lastModifiedDate(resume.getLastModifiedDate())
                .build();
    }

    List<ResumeResponse> toResponses(List<Resume> resumes);

    default ResumeDownloadResponse toDownloadResponse(Resume resume) {
        if (resume == null) {
            return null;
        }
        return ResumeDownloadResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .fileName(resume.getFileName())
                .fileUrl(resume.getFileUrl())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .build();
    }

    default ResumeResponse.CandidateInfo mapCandidate(User user) {
        if (user == null) {
            return null;
        }
        return ResumeResponse.CandidateInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .headline(user.getHeadline())
                .avatar(user.getAvatar())
                .build();
    }

    default ResumeResponse.JobInfo mapJob(Job job) {
        if (job == null) {
            return null;
        }
        String companyName = job.getCompany() != null ? job.getCompany().getName() : null;
        return ResumeResponse.JobInfo.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(companyName)
                .build();
    }

    default List<ResumeResponse.SkillInfo> mapSkills(List<Skill> skills) {
        if (skills == null) {
            return null;
        }
        return skills.stream()
                .map(skill -> ResumeResponse.SkillInfo.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .level(skill.getLevel())
                        .build())
                .collect(Collectors.toList());
    }
}

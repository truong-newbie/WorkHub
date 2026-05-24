package org.example.workhub.search.mapper;

import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.search.document.JobSearchDocument;
import org.example.workhub.search.dto.response.JobSearchResponse;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class JobSearchMapper {

    public JobSearchDocument toDocument(Job job) {
        if (job == null) {
            return null;
        }
        Company company = job.getCompany();
        JobSearchDocument document = new JobSearchDocument();
        document.setId(job.getId());
        document.setTitle(job.getTitle());
        document.setSlug(job.getSlug());
        document.setDescription(job.getDescription());
        document.setRequirement(job.getRequirement());
        document.setBenefit(job.getBenefit());
        document.setLocation(job.getLocation());
        document.setCompanyId(company != null ? company.getId() : null);
        document.setCompanyName(company != null ? company.getName() : null);
        document.setCompanyLogo(company != null ? company.getLogo() : null);
        document.setCompanyActive(company == null || Boolean.TRUE.equals(company.getActive()));
        document.setCompanyVerified(company == null || Boolean.TRUE.equals(company.getVerified()));
        document.setRecruiterId(job.getRecruiter() != null ? job.getRecruiter().getId() : null);
        document.setSalaryMin(parseSalary(job.getSalaryMin()));
        document.setSalaryMax(parseSalary(job.getSalaryMax()));
        document.setNegotiableSalary(job.getNegotiableSalary());
        document.setExperienceYears(job.getExperienceYears());
        document.setLevel(job.getLevel() != null ? job.getLevel().name() : null);
        document.setWorkMode(null);
        document.setEmploymentType(job.getEmploymentType());
        document.setSkillIds(mapSkillIds(job.getSkills()));
        document.setSkillNames(mapSkillNames(job.getSkills()));
        document.setPublished(job.getPublished());
        document.setDeleted(job.getDeleted());
        document.setExpiredAt(job.getExpiredAt());
        document.setCreatedDate(job.getCreatedDate());
        document.setLastModifiedDate(job.getLastModifiedDate());
        return document;
    }

    public JobSearchResponse toResponse(JobSearchDocument document) {
        return toResponse(document, null, null);
    }

    public JobSearchResponse toResponse(SearchHit<JobSearchDocument> searchHit) {
        if (searchHit == null) {
            return null;
        }
        return toResponse(searchHit.getContent(), searchHit.getScore(), searchHit.getHighlightFields());
    }

    private JobSearchResponse toResponse(JobSearchDocument document, Float score, Map<String, List<String>> highlights) {
        if (document == null) {
            return null;
        }
        return JobSearchResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .slug(document.getSlug())
                .location(document.getLocation())
                .companyId(document.getCompanyId())
                .companyName(document.getCompanyName())
                .companyLogo(document.getCompanyLogo())
                .salaryMin(document.getSalaryMin())
                .salaryMax(document.getSalaryMax())
                .negotiableSalary(document.getNegotiableSalary())
                .experienceYears(document.getExperienceYears())
                .level(document.getLevel())
                .workMode(document.getWorkMode())
                .employmentType(document.getEmploymentType())
                .skillNames(document.getSkillNames())
                .expiredAt(document.getExpiredAt())
                .createdDate(document.getCreatedDate())
                .matchScore(score)
                .highlightTitle(firstHighlight(highlights, "title"))
                .highlightDescription(firstHighlight(highlights, "description"))
                .highlights(highlights)
                .build();
    }

    private List<Long> mapSkillIds(List<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .filter(skill -> skill.getId() != null)
                .map(Skill::getId)
                .toList();
    }

    private List<String> mapSkillNames(List<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .map(Skill::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private BigDecimal parseSalary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String firstHighlight(Map<String, List<String>> highlights, String field) {
        if (highlights == null || highlights.get(field) == null || highlights.get(field).isEmpty()) {
            return null;
        }
        return highlights.get(field).get(0);
    }
}

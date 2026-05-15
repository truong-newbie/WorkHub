package org.example.workhub.domain.specification;

import jakarta.persistence.criteria.Join;
import org.example.workhub.domain.dto.request.JobFilterRequest;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Job;
import org.example.workhub.domain.entity.Skill;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("location")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
            );
        };
    }

    public static Specification<Job> withFilters(JobFilterRequest filter) {
        return Specification.where(hasCompany(filter.getCompanyId()))
                .and(hasLevel(filter.getLevel()))
                .and(hasLocation(filter.getLocation()))
                .and(hasEmploymentType(filter.getEmploymentType()))
                .and(hasSalaryRange(filter.getSalaryMin(), filter.getSalaryMax()))
                .and(hasExperienceYears(filter.getExperienceYearsMin(), filter.getExperienceYearsMax()))
                .and(isPublished(filter.getPublished()))
                .and(notExpired(filter.getIncludeExpired()))
                .and(isNotDeleted());
    }

    public static Specification<Job> hasCompany(Long companyId) {
        return (root, query, cb) -> {
            if (companyId == null) {
                return cb.conjunction();
            }
            Join<Job, Company> companyJoin = root.join("company");
            return cb.equal(companyJoin.get("id"), companyId);
        };
    }

    public static Specification<Job> hasLevel(String level) {
        return (root, query, cb) -> {
            if (level == null || level.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("level"), level);
        };
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) -> {
            if (location == null || location.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }

    public static Specification<Job> hasEmploymentType(String employmentType) {
        return (root, query, cb) -> {
            if (employmentType == null || employmentType.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("employmentType"), employmentType);
        };
    }

    public static Specification<Job> hasSalaryRange(String salaryMin, String salaryMax) {
        return (root, query, cb) -> {
            if (salaryMin == null && salaryMax == null) {
                return cb.conjunction();
            }
            if (salaryMin != null && salaryMax != null) {
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("salaryMin"), salaryMin),
                        cb.lessThanOrEqualTo(root.get("salaryMax"), salaryMax)
                );
            }
            if (salaryMin != null) {
                return cb.greaterThanOrEqualTo(root.get("salaryMin"), salaryMin);
            }
            return cb.lessThanOrEqualTo(root.get("salaryMax"), salaryMax);
        };
    }

    public static Specification<Job> hasExperienceYears(Integer minYears, Integer maxYears) {
        return (root, query, cb) -> {
            if (minYears == null && maxYears == null) {
                return cb.conjunction();
            }
            if (minYears != null && maxYears != null) {
                return cb.between(root.get("experienceYears"), minYears, maxYears);
            }
            if (minYears != null) {
                return cb.greaterThanOrEqualTo(root.get("experienceYears"), minYears);
            }
            return cb.lessThanOrEqualTo(root.get("experienceYears"), maxYears);
        };
    }

    public static Specification<Job> isPublished(Boolean published) {
        return (root, query, cb) -> {
            if (published == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("published"), published);
        };
    }

    public static Specification<Job> notExpired(Boolean includeExpired) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(includeExpired)) {
                return cb.conjunction();
            }
            return cb.or(
                    cb.isNull(root.get("expiredAt")),
                    cb.greaterThan(root.get("expiredAt"), LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())
            );
        };
    }

    public static Specification<Job> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Job> hasSkill(List<Long> skillIds) {
        return (root, query, cb) -> {
            if (skillIds == null || skillIds.isEmpty()) {
                return cb.conjunction();
            }
            Join<Job, Skill> skillJoin = root.join("skills");
            return skillJoin.get("id").in(skillIds);
        };
    }
}
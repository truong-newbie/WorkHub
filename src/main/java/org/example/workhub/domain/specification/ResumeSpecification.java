package org.example.workhub.domain.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.example.workhub.Specification.FilterParser;
import org.example.workhub.domain.dto.request.ResumeSearchRequest;
import org.example.workhub.domain.entity.Resume;
import org.example.workhub.domain.entity.Skill;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class ResumeSpecification {

    private static final Set<String> GENERIC_FIELDS = Set.of("title", "atsScore", "isDefault", "isPublic");

    public static Specification<Resume> withFilters(ResumeSearchRequest request) {
        Specification<Resume> spec = Specification.where(isNotDeleted())
                .and(titleContains(request.getTitle()))
                .and(atsScoreBetween(request.getAtsScoreMin(), request.getAtsScoreMax()))
                .and(hasSkill(request.getSkillId()))
                .and(uploadedBetween(
                        request.getUploadedFrom() != null ? request.getUploadedFrom().atStartOfDay() : null,
                        request.getUploadedTo() != null ? request.getUploadedTo().atTime(LocalTime.MAX) : null))
                .and(isDefault(request.getIsDefault()))
                .and(isPublic(request.getIsPublic()));

        Specification<Resume> dynamicSpec = dynamicFilter(request.getFilter());
        return dynamicSpec == null ? spec : spec.and(dynamicSpec);
    }

    public static Specification<Resume> ownedBy(String userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Resume> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Resume> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Resume> atsScoreBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            if (min != null && max != null) {
                return cb.between(root.get("atsScore"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("atsScore"), min);
            }
            return cb.lessThanOrEqualTo(root.get("atsScore"), max);
        };
    }

    public static Specification<Resume> hasSkill(Long skillId) {
        return (root, query, cb) -> {
            if (skillId == null) {
                return cb.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<Resume, Skill> skillJoin = root.join("skills", JoinType.LEFT);
            return cb.equal(skillJoin.get("id"), skillId);
        };
    }

    public static Specification<Resume> uploadedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("uploadedAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("uploadedAt"), from);
            }
            return cb.lessThanOrEqualTo(root.get("uploadedAt"), to);
        };
    }

    public static Specification<Resume> isDefault(Boolean isDefault) {
        return (root, query, cb) -> isDefault == null ? cb.conjunction() : cb.equal(root.get("isDefault"), isDefault);
    }

    public static Specification<Resume> isPublic(Boolean isPublic) {
        return (root, query, cb) -> isPublic == null ? cb.conjunction() : cb.equal(root.get("isPublic"), isPublic);
    }

    private static Specification<Resume> dynamicFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return null;
        }
        String normalized = normalizeFlagFilters(filter);
        String[] parts = normalized.split(",");
        Specification<Resume> result = null;
        FilterParser<Resume> parser = new FilterParser<>();
        for (String part : parts) {
            String expression = part.trim();
            if (expression.isEmpty() || !isSupportedGenericExpression(expression)) {
                continue;
            }
            Specification<Resume> current = parser.parse(expression).toSpecification();
            result = result == null ? current : result.and(current);
        }
        return result;
    }

    private static String normalizeFlagFilters(String filter) {
        return filter.replaceAll("(^|,)\\s*isPublic\\s*(?=,|$)", "$1isPublic:true")
                .replaceAll("(^|,)\\s*isDefault\\s*(?=,|$)", "$1isDefault:true");
    }

    private static boolean isSupportedGenericExpression(String expression) {
        return GENERIC_FIELDS.stream().anyMatch(field -> expression.startsWith(field + ":")
                || expression.startsWith(field + "=")
                || expression.startsWith(field + ">")
                || expression.startsWith(field + "<")
                || expression.startsWith(field + "!="));
    }
}

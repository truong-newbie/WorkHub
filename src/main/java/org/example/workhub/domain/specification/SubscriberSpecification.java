package org.example.workhub.domain.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.example.workhub.Specification.FilterParser;
import org.example.workhub.domain.dto.request.SubscriberSearchRequest;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.Subscriber;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class SubscriberSpecification {

    private static final Set<String> GENERIC_FIELDS = Set.of("email", "enabled");

    public static Specification<Subscriber> withFilters(SubscriberSearchRequest request) {
        Specification<Subscriber> spec = Specification.where(isNotDeleted())
                .and(emailContains(request.getEmail()))
                .and(isEnabled(request.getEnabled()))
                .and(hasSkill(request.getSkillId()))
                .and(subscribedBetween(
                        request.getSubscribedFrom() != null ? request.getSubscribedFrom().atStartOfDay() : null,
                        request.getSubscribedTo() != null ? request.getSubscribedTo().atTime(LocalTime.MAX) : null
                ));

        Specification<Subscriber> dynamicSpec = dynamicFilter(request.getFilter());
        return dynamicSpec == null ? spec : spec.and(dynamicSpec);
    }

    public static Specification<Subscriber> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Subscriber> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<Subscriber> isEnabled(Boolean enabled) {
        return (root, query, cb) -> enabled == null ? cb.conjunction() : cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<Subscriber> hasSkill(Long skillId) {
        return (root, query, cb) -> {
            if (skillId == null) {
                return cb.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<Subscriber, Skill> skillJoin = root.join("skills", JoinType.LEFT);
            return cb.equal(skillJoin.get("id"), skillId);
        };
    }

    public static Specification<Subscriber> subscribedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }
            if (from != null && to != null) {
                return cb.between(root.get("subscribedAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("subscribedAt"), from);
            }
            return cb.lessThanOrEqualTo(root.get("subscribedAt"), to);
        };
    }

    private static Specification<Subscriber> dynamicFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return null;
        }
        String[] parts = filter.split(",");
        Specification<Subscriber> result = null;
        FilterParser<Subscriber> parser = new FilterParser<>();
        for (String part : parts) {
            String expression = part.trim();
            if (expression.isEmpty() || !isSupportedGenericExpression(expression)) {
                continue;
            }
            Specification<Subscriber> current = parser.parse(expression).toSpecification();
            result = result == null ? current : result.and(current);
        }
        return result;
    }

    private static boolean isSupportedGenericExpression(String expression) {
        return GENERIC_FIELDS.stream().anyMatch(field -> expression.startsWith(field + ":")
                || expression.startsWith(field + "=")
                || expression.startsWith(field + ">")
                || expression.startsWith(field + "<")
                || expression.startsWith(field + "!="));
    }
}

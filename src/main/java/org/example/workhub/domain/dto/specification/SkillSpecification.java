package org.example.workhub.domain.dto.specification;

import org.example.workhub.domain.dto.request.SkillSearchRequest;
import org.example.workhub.domain.entity.Skill;
import org.springframework.data.jpa.domain.Specification;

public class SkillSpecification {

    public static Specification<Skill> withFilters(SkillSearchRequest request, boolean admin) {
        Specification<Skill> spec = Specification.where(search(request.getKeyword()))
                .and(like("name", request.getName()))
                .and(like("slug", request.getSlug()))
                .and(like("level", request.getLevel()))
                .and(equal("active", request.getActive()))
                .and(createdFrom(request.getCreatedFrom()))
                .and(createdTo(request.getCreatedTo()));

        if (admin && request.getDeleted() != null) {
            return spec.and(equal("deleted", request.getDeleted()));
        }
        return spec.and(isNotDeleted());
    }

    public static Specification<Skill> publicVisible() {
        return isNotDeleted().and(isActive());
    }

    public static Specification<Skill> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("level")), pattern)
            );
        };
    }

    public static Specification<Skill> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Skill> isActive() {
        return (root, query, cb) -> cb.equal(root.get("active"), true);
    }

    private static Specification<Skill> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase() + "%");
        };
    }

    private static <T> Specification<Skill> equal(String field, T value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.equal(root.get(field), value);
    }

    private static Specification<Skill> createdFrom(java.time.LocalDateTime value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdDate"), value);
    }

    private static Specification<Skill> createdTo(java.time.LocalDateTime value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdDate"), value);
    }
}

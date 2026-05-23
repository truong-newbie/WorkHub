package org.example.workhub.domain.dto.specification;

import org.example.workhub.domain.entity.Skill;
import org.springframework.data.jpa.domain.Specification;

public class SkillSpecification {

    public static Specification<Skill> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        };
    }
    public static Specification<Skill> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }
}
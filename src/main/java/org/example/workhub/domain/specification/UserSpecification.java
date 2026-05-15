package org.example.workhub.domain.specification;

import jakarta.persistence.criteria.Join;
import org.example.workhub.domain.dto.request.UserFilterRequest;
import org.example.workhub.domain.entity.Company;
import org.example.workhub.domain.entity.Role;
import org.example.workhub.domain.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), searchPattern),
                    cb.like(cb.lower(root.get("email")), searchPattern),
                    cb.like(cb.lower(root.get("phone")), searchPattern)
            );
        };
    }

    public static Specification<User> withFilters(UserFilterRequest filter) {
        return Specification.where(hasRole(filter.getRole()))
                .and(hasGender(filter.getGender()))
                .and(hasAgeBetween(filter.getMinAge(), filter.getMaxAge()))
                .and(belongsToCompany(filter.getCompanyId()))
                .and(isEnabled(filter.getEnabled()))
                .and(includeDeleted(filter.getIncludeDeleted()));
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.trim().isEmpty()) {
                return cb.conjunction();
            }
            Join<User, Role> roleJoin = root.join("role");
            return cb.equal(roleJoin.get("name"), roleName);
        };
    }

    public static Specification<User> hasGender(String gender) {
        return (root, query, cb) -> {
            if (gender == null || gender.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("gender"), gender);
        };
    }

    public static Specification<User> hasAgeBetween(Integer minAge, Integer maxAge) {
        return (root, query, cb) -> {
            if (minAge == null && maxAge == null) {
                return cb.conjunction();
            }
            if (minAge != null && maxAge != null) {
                return cb.between(root.get("age"), minAge, maxAge);
            }
            if (minAge != null) {
                return cb.greaterThanOrEqualTo(root.get("age"), minAge);
            }
            return cb.lessThanOrEqualTo(root.get("age"), maxAge);
        };
    }

    public static Specification<User> belongsToCompany(String companyId) {
        return (root, query, cb) -> {
            if (companyId == null || companyId.trim().isEmpty()) {
                return cb.conjunction();
            }
            Join<User, Company> companyJoin = root.join("company");
            return cb.equal(companyJoin.get("id"), Long.parseLong(companyId));
        };
    }

    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, cb) -> {
            if (enabled == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("enabled"), enabled);
        };
    }

    public static Specification<User> includeDeleted(Boolean includeDeleted) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(includeDeleted)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("deleted"), false);
        };
    }

    public static Specification<User> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdDate"), date);
        };
    }

    public static Specification<User> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return cb.conjunction();
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("createdDate"), startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("createdDate"), startDate.atStartOfDay());
            }
            return cb.lessThan(root.get("createdDate"), endDate.plusDays(1).atStartOfDay());
        };
    }
}

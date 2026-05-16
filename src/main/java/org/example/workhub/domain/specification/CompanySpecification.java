package org.example.workhub.domain.specification;

import org.example.workhub.domain.dto.request.CompanySearchRequest;
import org.example.workhub.domain.entity.Company;
import org.springframework.data.jpa.domain.Specification;

public class CompanySpecification {

    public static Specification<Company> withFilters(CompanySearchRequest request) {
        return isNotDeleted()
                .and(keyword(request.getKeyword()))
                .and(like("name", request.getName()))
                .and(like("city", request.getCity()))
                .and(like("country", request.getCountry()))
                .and(like("industry", request.getIndustry()))
                .and(equal("companySize", request.getCompanySize()))
                .and(equal("active", request.getActive()))
                .and(equal("verified", request.getVerified()))
                .and(createdFrom(request.getCreatedFrom()))
                .and(createdTo(request.getCreatedTo()));
    }

    public static Specification<Company> visibleToPublic() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("active"), true),
                cb.equal(root.get("verified"), true),
                cb.equal(root.get("deleted"), false)
        );
    }

    private static Specification<Company> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    private static Specification<Company> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("city")), pattern),
                    cb.like(cb.lower(root.get("country")), pattern),
                    cb.like(cb.lower(root.get("industry")), pattern)
            );
        };
    }

    private static Specification<Company> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase() + "%");
        };
    }

    private static <T> Specification<Company> equal(String field, T value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.equal(root.get(field), value);
    }

    private static Specification<Company> createdFrom(java.time.LocalDateTime value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdDate"), value);
    }

    private static Specification<Company> createdTo(java.time.LocalDateTime value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdDate"), value);
    }
}

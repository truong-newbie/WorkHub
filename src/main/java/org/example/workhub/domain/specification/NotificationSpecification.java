package org.example.workhub.domain.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.workhub.domain.dto.request.NotificationSearchRequest;
import org.example.workhub.domain.entity.Notification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification {

    private NotificationSpecification() {
    }

    public static Specification<Notification> withFilters(NotificationSearchRequest request, String recipientId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("recipient").get("id"), recipientId));
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), keyword)
                ));
            }
            if (request.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }
            if (request.getRead() != null) {
                predicates.add(criteriaBuilder.equal(root.get("read"), request.getRead()));
            }
            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getToDate()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}

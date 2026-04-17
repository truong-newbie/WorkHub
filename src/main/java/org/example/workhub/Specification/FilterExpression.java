package org.example.workhub.Specification;

import org.springframework.data.jpa.domain.Specification;

public interface FilterExpression<T> {
    Specification<T> toSpecification();
}

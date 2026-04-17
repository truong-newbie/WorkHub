package org.example.workhub.Specification;

import org.example.workhub.domain.dto.common.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class ConditionExpression<T> implements FilterExpression<T> {
    private final SearchCriteria criteria;

    public ConditionExpression(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Specification<T> toSpecification() {
        return new GenericSpecification<>(criteria);
    }
}

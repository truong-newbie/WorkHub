package org.example.workhub.domain.dto.common;

import lombok.Getter;
import org.example.workhub.constant.SearchOperation;

@Getter
public class SearchCriteria {
    private final String key;
    private final SearchOperation operation;
    private final Object value;
    private final boolean orPredicate;

    public SearchCriteria(String key, SearchOperation operation, Object value) {
        this(key, operation, value, false);
    }

    public SearchCriteria(String key, SearchOperation operation, Object value, boolean orPredicate) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        this.orPredicate = orPredicate;
    }
}

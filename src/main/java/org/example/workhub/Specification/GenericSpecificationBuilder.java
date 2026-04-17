package org.example.workhub.Specification;


import org.example.workhub.constant.SearchOperation;
import org.example.workhub.domain.dto.common.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class GenericSpecificationBuilder<T> {

    private final List<SearchCriteria> params = new ArrayList<>();

    public GenericSpecificationBuilder<T> with(String key, String operation, Object value) {
        return with(key, operation, value, null, null);
    }

    public GenericSpecificationBuilder<T> with(String key, String operation, Object value,
                                               String prefix, String suffix) {
        return with(false, key, operation, value, prefix, suffix);
    }

    public GenericSpecificationBuilder<T> with(boolean orPredicate, String key, String operation,
                                               Object value, String prefix, String suffix) {
        SearchOperation op = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            if (op == SearchOperation.EQUALITY) {
                boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    op = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    op = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    op = SearchOperation.STARTS_WITH;
                }
            }
            params.add(new SearchCriteria(key, op, value, orPredicate));
        }
        return this;
    }

    public Specification<T> build() {                                                                                                                                           if (params.isEmpty()) {                                                                                                                                                     return null;                                                                                                                                                        }

        Specification<T> result = new GenericSpecification<>(params.get(0));
        for (int i = 1; i < params.size(); i++) {
            Specification<T> spec = new GenericSpecification<>(params.get(i));
            //  Nếu current hoặc trước đó là OR, nối OR. Còn lại mặc định là AND.
            if (params.get(i).isOrPredicate() || params.get(i - 1).isOrPredicate()) {
                result = Specification.where(result).or(spec);
            } else {
                result = Specification.where(result).and(spec);
            }
        }
        return result;
    }
}
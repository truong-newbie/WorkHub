package org.example.workhub.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.workhub.constant.SearchOperation;
import org.example.workhub.domain.dto.common.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nullable;


public class GenericSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    public GenericSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Nullable
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String key = criteria.getKey();
        Object value = criteria.getValue();
        SearchOperation operation = criteria.getOperation();

        if (operation == null) {
            return null;
        }

        return switch (operation) {
            case EQUALITY -> {
                if (isStringField(root, key)) {
                    yield builder.equal(root.get(key), value.toString());
                } else {
                    yield builder.equal(root.get(key), convertValue(root, key, value));
                }
            }
            case NEGATION -> builder.notEqual(root.get(key), convertValue(root, key, value));
            case GREATER_THAN -> builder.greaterThan(root.get(key), convertComparable(root, key, value));
            case LESS_THAN -> builder.lessThan(root.get(key), convertComparable(root, key, value));
            case GREATER_THAN_EQUAL -> builder.greaterThanOrEqualTo(root.get(key), convertComparable(root, key, value));
            case LESS_THAN_EQUAL -> builder.lessThanOrEqualTo(root.get(key), convertComparable(root, key, value));
            case CONTAINS -> builder.like(root.get(key), "%" + value + "%");
            case STARTS_WITH -> builder.like(root.get(key), value + "%");
            case ENDS_WITH -> builder.like(root.get(key), "%" + value);
        };
    }

    private boolean isStringField(Root<T> root, String key) {
        return root.get(key).getJavaType() == String.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertValue(Root<T> root, String key, Object value) {
        Class<?> fieldType = root.get(key).getJavaType();
        if (fieldType == String.class) {
            return value.toString();
        }
        if (value.getClass() == fieldType) {
            return value;
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(value.toString());
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(value.toString());
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(value.toString());
        }
        if (fieldType == Float.class || fieldType == float.class) {
            return Float.parseFloat(value.toString());
        }
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Comparable convertComparable(Root<T> root, String key, Object value) {
        Class<?> fieldType = root.get(key).getJavaType();
        if (value.getClass() == fieldType && value instanceof Comparable) {
            return (Comparable) value;
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(value.toString());
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(value.toString());
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(value.toString());
        }
        if (fieldType == Float.class || fieldType == float.class) {
            return Float.parseFloat(value.toString());
        }
        return (Comparable) value;
    }
}
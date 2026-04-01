package org.example.workhub.domain.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;


public class GenericSpecification<T> implements Specification<T> {
    private final SpecSearchCriteria criteria;

    public GenericSpecification(final  SpecSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Nullable
    @Override
    public Predicate toPredicate(Root<T> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<?> path = getPath(root, criteria.getKey());

        Object value = castToRequiredType(path.getJavaType(), criteria.getValue());

        return switch (criteria.getOperation()) {

            case EQUALITY -> cb.equal(path, value);

            case NEGATION -> cb.notEqual(path, value);

            case GREATER_THAN -> cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) value);

            case LESS_THAN -> cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) value);

            case CONTAINS -> cb.like(path.as(String.class), "%" + value + "%");

            case STARTS_WITH -> cb.like(path.as(String.class), value + "%");

            case ENDS_WITH -> cb.like(path.as(String.class), "%" + value);

            case LIKE -> cb.like(path.as(String.class), value.toString());

            default -> null;

        };

    }


    // 🔥 support nested: role.name
//    private Path<?> getPath(Root<T> root, String key) {
//        if (key.contains(".")) {
//            String[] parts = key.split("\\.");
//            Path<?> path = root.get(parts[0]);
//            for (int i = 1; i < parts.length; i++) {
//                path = path.get(parts[i]);
//            }
//            return path;
//        }
//        return root.get(key);
//    }

    // 🔥 Support nested relationships (Ví dụ: company.name) an toàn với Join
    private Path<?> getPath(Root<T> root, String key) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            Join<Object, Object> join = root.join(parts[0], JoinType.LEFT);
            // Giả định hỗ trợ join 1 cấp (Thường là đủ cho đa số trường hợp)
            return join.get(parts[1]);
        }
        return root.get(key);
    }





    // 🔥 auto convert type
//    private Object castToRequiredType(Class<?> type, Object value) {
//
//        if (type == Integer.class) {
//            return Integer.parseInt(value.toString());
//        } else if (type == Long.class) {
//            return Long.parseLong(value.toString());
//        } else if (type == Double.class) {
//            return Double.parseDouble(value.toString());
//        } else if (type == Boolean.class) {
//            return Boolean.parseBoolean(value.toString());
//        } else if (type == java.util.UUID.class) {
//            return java.util.UUID.fromString(value.toString());
//        } else if (type == java.time.LocalDate.class) {
//            return java.time.LocalDate.parse(value.toString());
//        }
//
//        return value.toString();
//    }

    // 🔥 Auto convert type (Bổ sung thêm xử lý Enum)
    private Object castToRequiredType(Class<?> type, Object value) {
        String stringValue = value.toString();

        if (type.isAssignableFrom(String.class)) {
            return stringValue;
        } else if (type == Integer.class || type == int.class) {
            return Integer.parseInt(stringValue);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(stringValue);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(stringValue);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (type == java.util.UUID.class) {
            return java.util.UUID.fromString(stringValue);
        } else if (type == java.time.LocalDate.class) {
            return java.time.LocalDate.parse(stringValue);
        } else if (type.isEnum()) {
            // Xử lý tự động ép kiểu chuỗi thành Enum tương ứng trong Entity
            return Enum.valueOf((Class<Enum>) type, stringValue);
        }

        return stringValue;
    }


}

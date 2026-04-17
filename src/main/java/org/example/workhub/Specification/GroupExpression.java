package org.example.workhub.Specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class GroupExpression<T> implements FilterExpression<T> {
    public enum Type { AND, OR }

    private final List<FilterExpression<T>> children;
    private final Type type;

    public GroupExpression(Type type, List<FilterExpression<T>> children) {
        this.type = type;
        this.children = children;
    }

    @Override
    public Specification<T> toSpecification() {
        if (children.isEmpty()) return null;
        Specification<T> spec = children.get(0).toSpecification();
        for (int i = 1; i < children.size(); i++) {
            Specification<T> childSpec = children.get(i).toSpecification();
            spec = (type == Type.AND)
                    ? Specification.where(spec).and(childSpec)
                    : Specification.where(spec).or(childSpec);
        }
        return spec;
    }
}

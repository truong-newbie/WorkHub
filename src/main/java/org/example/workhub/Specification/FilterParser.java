package org.example.workhub.Specification;

import org.example.workhub.constant.SearchOperation;
import org.example.workhub.domain.dto.common.SearchCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterParser<T> {
    // Parse string thành expression tree
    public FilterExpression<T> parse(String input) {
        input = input.trim();

        if (input.startsWith("(") && input.endsWith(")")) {
            input = input.substring(1, input.length() - 1);
        }

        // Parse ANDs (',')
        List<String> andParts = splitLevel(input, ',');
        if (andParts.size() > 1) {
            List<FilterExpression<T>> children = andParts.stream()
                    .map(this::parse)
                    .collect(Collectors.toList());
            return new GroupExpression<>(GroupExpression.Type.AND, children);
        }

        // Parse ORs ('|')
        List<String> orParts = splitLevel(input, '|');
        if (orParts.size() > 1) {
            List<FilterExpression<T>> children = orParts.stream()
                    .map(this::parse)
                    .collect(Collectors.toList());
            return new GroupExpression<>(GroupExpression.Type.OR, children);
        }

        // Parse single condition (key:op:value)
        SearchCriteria sc = parseCriteria(input);
        return new ConditionExpression<>(sc);
    }

    // Tách các điều kiện cùng cấp theo dấu phân cách
    private List<String> splitLevel(String input, char sep) {
        List<String> parts = new ArrayList<>();
        int level = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '(') level++;
            else if (c == ')') level--;
            if (c == sep && level == 0) {
                parts.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        if (!sb.isEmpty()) parts.add(sb.toString());
        return parts;
    }

    private SearchCriteria parseCriteria(String expr) {
        String pattern = "(\\w+?)(>=|<=|!=|:|=|>|<)(\"[^\"]+\"|[^,|]+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(expr.trim());
        if (m.matches()) {
            String key = m.group(1);
            String op = m.group(2);
            String value = m.group(3);

            boolean startWithAsterisk = value.startsWith("*");
            boolean endWithAsterisk = value.endsWith("*");
            if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
            if (startWithAsterisk) value = value.substring(1);
            if (endWithAsterisk) value = value.substring(0, value.length() - 1);

            SearchOperation searchOp;
            if (op.equals(":") || op.equals("=")) {
                if (startWithAsterisk && endWithAsterisk) {
                    searchOp = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    searchOp = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    searchOp = SearchOperation.STARTS_WITH;
                } else {
                    searchOp = SearchOperation.EQUALITY;
                }
            } else {
                searchOp = SearchOperation.getSimpleOperation(op.charAt(0));
            }
            return new SearchCriteria(key, searchOp, value);
        }
        // fallback: tìm chứa keyword (mặc định LIKE)
        return new SearchCriteria("name", SearchOperation.CONTAINS, expr.trim());
    }
}

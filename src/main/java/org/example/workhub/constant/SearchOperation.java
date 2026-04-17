package org.example.workhub.constant;

public enum SearchOperation {
    EQUALITY,
    NEGATION,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,

    // LIKE operations
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH;
    public static final String ZERO_OR_MORE_REGEX = "*";

    public static SearchOperation getSimpleOperation(char input) {
        switch (input) {
            case ':':
                return EQUALITY;
            case '=':
                return EQUALITY;
            case '>':
                return GREATER_THAN;
            case '<':
                return LESS_THAN;
            case '!':
                return NEGATION;
            default:
                return null;
        }
    }
}

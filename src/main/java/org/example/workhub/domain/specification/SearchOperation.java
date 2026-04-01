package org.example.workhub.domain.specification;

import java.util.Set;

public enum SearchOperation {

    // Bang voi, Phu dinh, Lon hon, Be hon, Giong, Bat dau voi, Ket thuc voi, Chua
    EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, LIKE, STARTS_WITH, ENDS_WITH, CONTAINS;

    public static final Set<Character> SIMPLE_OPERATION_SET = Set.of(':', '!', '>', '<', '~');

    public static final String OR_PREDICATE_FLAG = "'";

    public static final String ZERO_OR_MORE_REGEX  = "*";



    public static SearchOperation getSimpleOperation(char input){
        return switch (input){
            case ':' -> EQUALITY;
            case '!' -> NEGATION;
            case '>' -> GREATER_THAN;
            case '<' -> LESS_THAN;
            case '~' -> LIKE;
            default -> null;
        };
    }
}

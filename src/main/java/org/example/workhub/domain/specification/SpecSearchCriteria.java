package org.example.workhub.domain.specification;

import lombok.Getter;


import static org.example.workhub.domain.specification.SearchOperation.*;


@Getter
public class SpecSearchCriteria {
    private final String key;
    private final SearchOperation operation;
    private final Object value;
    private final boolean orPredicate;

    private SpecSearchCriteria(String key, SearchOperation searchOperation, Object value){
        this(null, key, searchOperation, value);
    }

    public SpecSearchCriteria(String orPredicate, String key, SearchOperation searchOperation, Object value){
        this.key = key;
        this.operation = searchOperation;
        this.value = value;
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
    }

    public SpecSearchCriteria(String orPredicate, String key, String operation, Object value, String prefix, String suffix) {
        if(operation != null && operation.isEmpty()){
            throw new IllegalArgumentException("Search operation cannot be empty or null");
        }
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if(searchOperation != null){
            if(searchOperation == EQUALITY){
                final boolean startWithAsterisks = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisks = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);
                if(startWithAsterisks && endWithAsterisks){
                    searchOperation = CONTAINS;
                } else if (startWithAsterisks) {
                    searchOperation = ENDS_WITH;
                } else if(endWithAsterisks){
                    searchOperation = STARTS_WITH;
                }
            }

        }
        this.key = key;
        this.operation = searchOperation;
        this.value = value;
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
    }

    public static void handleWildCardSearch(String valueStr, String orPredicate,String prefix, String suffix, boolean isOrPredicate){
        if(valueStr.startsWith("*")){
            prefix = "*";
            valueStr = valueStr.substring(1); // 0 - 1
        }
        if(valueStr.endsWith("*")){
            suffix = "*";
            valueStr = valueStr.substring(0, valueStr.length() - 1);
        }
        isOrPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
    }

}

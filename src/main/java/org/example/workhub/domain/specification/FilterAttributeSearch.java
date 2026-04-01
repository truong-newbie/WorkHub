package org.example.workhub.domain.specification;

public record FilterAttributeSearch(String valueStr, String prefix, String suffix, boolean isOrPredicate) {
    public static FilterAttributeSearch handleWildCardSearch(String valueStr, String orIndicator){
        String prefix = null;
        String suffix = null;
        if(valueStr.startsWith("*")){
            prefix = "*";
            valueStr = valueStr.substring(1);
        }
        if(valueStr.endsWith("*")){
            suffix = "*";
            valueStr = valueStr.substring(0, valueStr.length() - 1);
        }

        boolean isOrPredicate = orIndicator != null && orIndicator.equals(SearchOperation.OR_PREDICATE_FLAG);
        return new FilterAttributeSearch(valueStr, prefix, suffix, isOrPredicate);
    }
}

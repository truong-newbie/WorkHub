package org.example.workhub.domain.specification;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record FilterProcessor(SpecificationBuilder<?> specificationBuilder, List<String> filter) {
    public static FilterProcessor process(SpecificationBuilder<?> specificationBuilder, List<String> filter){
        SpecificationBuilder<?> builder = new SpecificationBuilder<>();
        if (filter == null || filter.isEmpty()) {
            return new FilterProcessor(builder, filter);
//            Nếu người dùng không truyền bộ lọc nào (null hoặc rỗng),
//            hệ thống sẽ trả về một kết quả "trống" nhưng vẫn hợp lệ.
        }else {
            Pattern pattern = Pattern.compile("^('?)\\s*([a-zA-Z0-9_.]+)([<:>~!])(.*)$");
            for(String condition : filter){
                condition = condition.trim();
                Matcher matcher = pattern.matcher(condition);
                if(matcher.find()){
                    String orIndicator = matcher.group(1);
                    String key = matcher.group(2).trim();
                    String operation = matcher.group(3);
                    String valueStr = matcher.group(4).trim();
                    String prefix = null;
                    String suffix = null;
                    FilterAttributeSearch att = FilterAttributeSearch.handleWildCardSearch(valueStr, orIndicator);
                    if(att.isOrPredicate()){
                        specificationBuilder.with(SearchOperation.OR_PREDICATE_FLAG,key, operation, valueStr, prefix, suffix);
                    } else{
                        specificationBuilder.with(key, operation, valueStr, prefix, suffix);
                    }
                }
            }
            return new FilterProcessor(specificationBuilder, filter);
        }

        }


}

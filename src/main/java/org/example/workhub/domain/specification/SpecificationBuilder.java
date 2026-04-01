package org.example.workhub.domain.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {
    private final List<SpecSearchCriteria> params;
    public  SpecificationBuilder() {
        this.params = new ArrayList<>();
    }

    public SpecificationBuilder<T> with(String key, String operation, Object value, String prefix, String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public SpecificationBuilder<T> with(String orPredicate ,String key, String operation, Object value, String prefix, String suffix){

        params.add(new SpecSearchCriteria(orPredicate, key, operation, value, prefix, suffix));

        return this;
    }
    public Specification<T> build(){
        if(params.isEmpty()){
            return null;
        }
        Specification<T> result = new GenericSpecification<>(params.get(0));
        for(int i = 1; i < params.size();i++){
            SpecSearchCriteria criteria = params.get(i);
            Specification<T> spec = new GenericSpecification<>(criteria);
            if(criteria.isOrPredicate()){
                result = result.or(spec);
            } else {
                result = result.and(spec);
            }
        }
        return  result;
    }
}

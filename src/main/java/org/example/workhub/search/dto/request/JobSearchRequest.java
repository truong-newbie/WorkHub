package org.example.workhub.search.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.dto.pagination.PaginationSortRequestDto;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class JobSearchRequest extends PaginationSortRequestDto {

    private String keyword;

    private String location;

    private List<Long> skillIds;

    private List<String> skillNames;

    private String level;

    private Long companyId;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String workMode;

    private String employmentType;
}

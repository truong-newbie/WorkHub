package org.example.workhub.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class JobSearchResponse {

    private Long id;

    private String title;

    private String slug;

    private String location;

    private Long companyId;

    private String companyName;

    private String companyLogo;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private Boolean negotiableSalary;

    private Integer experienceYears;

    private String level;

    private String workMode;

    private String employmentType;

    private List<String> skillNames;

    private Instant expiredAt;

    private LocalDateTime createdDate;

    private Float matchScore;

    private String highlightTitle;

    private String highlightDescription;

    private Map<String, List<String>> highlights;
}

package org.example.workhub.domain.dto.internal;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiIntentResponse {
    private String intent;
    private Boolean outOfScope;
    private String keyword;
    private String location;
    private Long jobId;
    private Long companyId;
    private String companyName;
    private List<String> skillNames;
    private String level;
    private String employmentType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
}


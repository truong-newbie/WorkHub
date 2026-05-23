package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.StatusEnum;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyJoinRequestResponse {

    private Long id;
    private StatusEnum status;
    private String message;
    private String reviewNote;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdDate;
    private RecruiterSummary recruiter;
    private CompanySummary company;
    private ReviewerSummary reviewedBy;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecruiterSummary {
        private String id;
        private String username;
        private String email;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompanySummary {
        private Long id;
        private String name;
        private Boolean active;
        private Boolean verified;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReviewerSummary {
        private String id;
        private String username;
        private String email;
    }
}

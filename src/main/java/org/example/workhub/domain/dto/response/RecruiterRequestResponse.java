package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.workhub.constant.RecruiterRequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruiterRequestResponse {

    private Long id;
    private RecruiterRequestStatus status;
    private String message;
    private String reviewNote;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdDate;
    private UserSummary user;
    private ReviewerSummary reviewedBy;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserSummary {
        private String id;
        private String username;
        private String email;
        private String roleName;
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

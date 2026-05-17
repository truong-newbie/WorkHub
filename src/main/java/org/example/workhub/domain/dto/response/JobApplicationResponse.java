package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.workhub.constant.StatusEnum;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Job application response DTO")
public class JobApplicationResponse {

    @Schema(description = "Application ID", example = "1")
    private Long id;

    @Schema(description = "Application status", example = "PENDING")
    private StatusEnum status;

    @Schema(description = "Cover letter")
    private String coverLetter;

    @Schema(description = "Applied at")
    private Instant appliedAt;

    @Schema(description = "Reviewed at")
    private Instant reviewedAt;

    @Schema(description = "Review note")
    private String reviewNote;

    @Schema(description = "Resume ID used for this application", example = "1")
    private Long resumeId;

    @Schema(description = "Job info")
    private JobBasicInfo job;

    @Schema(description = "Candidate info")
    private CandidateInfo candidate;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JobBasicInfo {
        private Long id;
        private String title;
        private String location;
        private String companyName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CandidateInfo {
        private String id;
        private String username;
        private String email;
        private String phone;
        private String avatar;
        private String headline;
    }
}

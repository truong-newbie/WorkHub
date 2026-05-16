package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.workhub.constant.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resume response DTO")
public class ResumeResponse {

    @Schema(description = "Resume ID", example = "1")
    private Long id;

    @Schema(description = "Resume title", example = "Senior Java Backend Resume")
    private String title;

    @Schema(description = "Original file name", example = "resume.pdf")
    private String fileName;

    @Schema(description = "Uploaded file URL")
    private String fileUrl;

    @Schema(description = "File type", example = "pdf")
    private String fileType;

    @Schema(description = "File size in bytes", example = "120000")
    private Long fileSize;

    @Schema(description = "Default resume flag", example = "true")
    private Boolean isDefault;

    @Schema(description = "Public resume flag", example = "false")
    private Boolean isPublic;

    @Schema(description = "Soft delete flag", example = "false")
    private Boolean deleted;

    @Schema(description = "Resume summary")
    private String summary;

    @Schema(description = "ATS score", example = "86")
    private Integer atsScore;

    @Schema(description = "Parsed content")
    private String parsedContent;

    @Schema(description = "Application moderation status")
    private StatusEnum status;

    @Schema(description = "Uploaded at")
    private LocalDateTime uploadedAt;

    @Schema(description = "Owner info")
    private CandidateInfo candidate;

    @Schema(description = "Linked job info")
    private JobInfo job;

    @Schema(description = "Resume skills")
    private List<SkillInfo> skills;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Last modified date")
    private LocalDateTime lastModifiedDate;

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
        private String headline;
        private String avatar;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JobInfo {
        private Long id;
        private String title;
        private String companyName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SkillInfo {
        private Long id;
        private String name;
        private String level;
    }
}

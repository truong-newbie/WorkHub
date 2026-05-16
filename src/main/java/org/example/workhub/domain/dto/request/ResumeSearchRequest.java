package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to search resumes")
public class ResumeSearchRequest {

    @Schema(description = "Dynamic filter expression", example = "title:*java*,atsScore>80,isPublic:true")
    private String filter;

    @Schema(description = "Title keyword", example = "java")
    private String title;

    @Schema(description = "Minimum ATS score", example = "80")
    private Integer atsScoreMin;

    @Schema(description = "Maximum ATS score", example = "100")
    private Integer atsScoreMax;

    @Schema(description = "Skill ID", example = "1")
    private Long skillId;

    @Schema(description = "Uploaded from date", example = "2026-05-01")
    private LocalDate uploadedFrom;

    @Schema(description = "Uploaded to date", example = "2026-05-16")
    private LocalDate uploadedTo;

    @Schema(description = "Default resume flag", example = "true")
    private Boolean isDefault;

    @Schema(description = "Public resume flag", example = "false")
    private Boolean isPublic;

    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "10")
    private Integer size = 10;

    @Schema(description = "Sort field", example = "uploadedAt")
    private String sortBy = "uploadedAt";

    @Schema(description = "Sort direction", example = "DESC")
    private String sortDir = "DESC";
}

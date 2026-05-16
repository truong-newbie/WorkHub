package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update resume metadata")
public class ResumeUpdateRequest {

    @Size(max = 150, message = "{invalid.general}")
    @Schema(description = "Resume title", example = "Senior Java Backend Resume")
    private String title;

    @Size(max = 2000, message = "{invalid.general}")
    @Schema(description = "Resume summary", example = "Java backend engineer with Spring Boot experience")
    private String summary;

    @Schema(description = "Make this resume public", example = "true")
    private Boolean isPublic;

    @Schema(description = "Make this resume default", example = "false")
    private Boolean isDefault;

    @Min(value = 0, message = "{resume.ats.score.invalid}")
    @Max(value = 100, message = "{resume.ats.score.invalid}")
    @Schema(description = "ATS score from parser or moderation", example = "86")
    private Integer atsScore;

    @Schema(description = "Parsed resume content for ATS and AI analysis")
    private String parsedContent;

    @Schema(description = "Skill IDs attached to this resume", example = "[1,2,3]")
    private List<Long> skillIds;
}

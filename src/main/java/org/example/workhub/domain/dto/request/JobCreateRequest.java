package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.workhub.constant.LevelEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a job")
public class JobCreateRequest {

    @NotBlank(message = "{invalid.general.required}")
    @Size(max = 255, message = "{invalid.general}")
    @Schema(description = "Job title", example = "Senior Java Developer")
    private String title;

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Job description", example = "We are looking for...")
    private String description;

    @Schema(description = "Job requirements", example = "5+ years experience...")
    private String requirement;

    @Schema(description = "Job benefits", example = "Competitive salary, insurance...")
    private String benefit;

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Job location", example = "Ho Chi Minh City")
    private String location;

    @Schema(description = "Minimum salary", example = "1000")
    private String salaryMin;

    @Schema(description = "Maximum salary", example = "3000")
    private String salaryMax;

    @Schema(description = "Salary is negotiable", example = "true")
    private Boolean negotiableSalary = false;

    @NotNull(message = "{invalid.general.required}")
    @Schema(description = "Job level", example = "MIDDLE")
    private LevelEnum level;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private String employmentType;

    @Min(value = 0, message = "{invalid.general}")
    @Schema(description = "Required experience years", example = "3")
    private Integer experienceYears;

    @Min(value = 1, message = "{invalid.general}")
    @Schema(description = "Number of positions", example = "2")
    private Integer quantity = 1;

    @Future(message = "{invalid.date-format}")
    @Schema(description = "Job expiry date", example = "2024-12-31")
    private LocalDate expiredAt;

    @Schema(description = "Start date", example = "2024-06-01")
    private LocalDate startDate;

    @Schema(description = "List of skill IDs", example = "[1, 2, 3]")
    private List<Long> skillIds;

    @Schema(description = "Publish immediately", example = "false")
    private Boolean published = false;
}
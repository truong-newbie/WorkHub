package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to upload a resume")
public class ResumeUploadRequest {

    @NotBlank(message = "{invalid.general.required}")
    @Size(max = 150, message = "{invalid.general}")
    @Schema(description = "Resume title", example = "Senior Java Backend Resume")
    private String title;

    @Size(max = 2000, message = "{invalid.general}")
    @Schema(description = "Resume summary", example = "Java backend engineer with Spring Boot experience")
    private String summary;

    @Schema(description = "Make this resume default", example = "true")
    private Boolean isDefault;

    @Schema(description = "Make this resume public", example = "false")
    private Boolean isPublic;

    @Schema(description = "Skill IDs attached to this resume", example = "[1,2,3]")
    private List<Long> skillIds;

    @Schema(description = "Resume file. Supported: PDF, DOC, DOCX", type = "string", format = "binary")
    private MultipartFile file;
}

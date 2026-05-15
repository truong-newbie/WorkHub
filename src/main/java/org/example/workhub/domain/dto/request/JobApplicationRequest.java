package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to apply for a job")
public class JobApplicationRequest {

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Cover letter", example = "I am interested in this position...")
    private String coverLetter;
}
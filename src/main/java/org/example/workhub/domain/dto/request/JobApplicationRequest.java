package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to apply for a job")
public class JobApplicationRequest {

    @NotNull(message = "{invalid.general.required}")
    @Schema(description = "Uploaded resume ID", example = "1")
    private Long resumeId;

    @Schema(description = "Cover letter", example = "I am interested in this position...")
    private String coverLetter;
}

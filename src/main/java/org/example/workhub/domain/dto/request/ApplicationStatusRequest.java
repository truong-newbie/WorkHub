package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update application status")
public class ApplicationStatusRequest {

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "New status (REVIEWING, APPROVED, REJECTED)", example = "APPROVED")
    private String status;

    @Schema(description = "Review note", example = "Good candidate, please schedule interview")
    private String reviewNote;
}
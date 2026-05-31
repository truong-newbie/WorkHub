package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for reviewing recruiter upgrade request")
public class RecruiterRequestReviewRequest {

    @Size(max = 1000, message = "{invalid.general}")
    @Schema(description = "Admin review note", example = "Candidate recruiter request approved.")
    private String reviewNote;
}

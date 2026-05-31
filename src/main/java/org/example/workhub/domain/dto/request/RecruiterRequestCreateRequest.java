package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for candidate recruiter upgrade request")
public class RecruiterRequestCreateRequest {

    @Size(max = 1000, message = "{invalid.general}")
    @Schema(description = "Candidate message to admin", example = "I want to use WorkHub as a recruiter.")
    private String message;
}

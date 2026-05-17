package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for recruiter company join request")
public class CompanyJoinRequestCreateRequest {

    @Size(max = 1000, message = "{invalid.general}")
    @Schema(description = "Recruiter message to company owner/admin", example = "I am HR at this company and want to manage jobs.")
    private String message;
}

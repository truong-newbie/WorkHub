package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for reviewing company join request")
public class CompanyJoinRequestReviewRequest {

    @Size(max = 1000, message = "{invalid.general}")
    @Schema(description = "Review note from admin or company owner", example = "Verified recruiter email and company ownership.")
    private String reviewNote;
}

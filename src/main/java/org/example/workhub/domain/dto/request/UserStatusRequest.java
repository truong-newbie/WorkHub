package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to change user status (lock/unlock/role)")
public class UserStatusRequest {

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Reason for the action", example = "Violation of terms")
    private String reason;

    @Schema(description = "New role (for role change only)", example = "CANDIDATE")
    private String newRole;
}
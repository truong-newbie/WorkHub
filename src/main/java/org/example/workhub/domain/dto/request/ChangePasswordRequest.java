package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to change password")
public class ChangePasswordRequest {

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Current password", example = "OldPassword123")
    private String currentPassword;

    @NotBlank(message = "{invalid.general.required}")
    @Size(min = 8, max = 100, message = "{invalid.password-format}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "{invalid.password-format}")
    @Schema(description = "New password (min 8 chars, 1 uppercase, 1 lowercase, 1 digit)", example = "NewPassword123")
    private String newPassword;

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Confirm new password", example = "NewPassword123")
    private String confirmPassword;
}
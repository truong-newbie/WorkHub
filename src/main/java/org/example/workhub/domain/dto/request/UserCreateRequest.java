package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.workhub.constant.GenderEnum;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new user")
public class UserCreateRequest {

    @NotBlank(message = "{invalid.general.not-blank}")
    @Size(min = 3, max = 50, message = "{invalid.general}")
    @Schema(description = "Username", example = "johndoe")
    private String username;

    @NotBlank(message = "{invalid.general.required}")
    @Email(message = "{invalid.email}")
    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @NotBlank(message = "{invalid.general.not-blank}")
    @Size(min = 8, max = 100, message = "{invalid.password-format}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "{invalid.password-format}")
    @Schema(description = "Password (min 8 chars, 1 uppercase, 1 lowercase, 1 digit)", example = "Password123")
    private String password;

    @Min(value = 18, message = "{invalid.general}")
    @Max(value = 100, message = "{invalid.general}")
    @Schema(description = "Age", example = "25")
    private Integer age;

    @Schema(description = "Gender", example = "MALE")
    private GenderEnum gender;

    @Past(message = "{invalid.date-format}")
    @Schema(description = "Date of birth", example = "1995-05-15")
    private LocalDate dob;

    @Schema(description = "Address", example = "123 Nguyen Hue, District 1, HCMC")
    private String address;

    @Pattern(regexp = "^$|^[+]?[0-9]{10,15}$", message = "{invalid.general}")
    @Schema(description = "Phone number", example = "0912345678")
    private String phone;

    @Schema(description = "Headline", example = "Senior Java Developer")
    private String headline;

    @Schema(description = "Bio/About me", example = "Experienced developer with 5+ years...")
    private String bio;

    @Min(value = 0, message = "{invalid.general}")
    @Max(value = 50, message = "{invalid.general}")
    @Schema(description = "Years of experience", example = "5")
    private Integer experienceYears;

    @Schema(description = "Location", example = "Ho Chi Minh City")
    private String location;

    @Pattern(regexp = "^$|^(https?://)[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "{invalid.general.format}")
    @Schema(description = "Personal website", example = "https://johndoe.dev")
    private String website;

    @Pattern(regexp = "^$|^(https?://)[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$", message = "{invalid.general.format}")
    @Schema(description = "LinkedIn profile URL", example = "https://linkedin.com/in/johndoe")
    private String linkedinUrl;

    @Pattern(regexp = "^$|^(https?://)[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$", message = "{invalid.general.format}")
    @Schema(description = "GitHub profile URL", example = "https://github.com/johndoe")
    private String githubUrl;

    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Role name (ADMIN, RECRUITER, CANDIDATE)", example = "CANDIDATE")
    private String roleName;

    @Schema(description = "Company ID (for RECRUITER)", example = "1")
    private String companyId;
}
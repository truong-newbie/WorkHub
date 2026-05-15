package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.workhub.constant.GenderEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User response DTO")
public class UserResponse {

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @Schema(description = "Age", example = "25")
    private Integer age;

    @Schema(description = "Gender", example = "MALE")
    private GenderEnum gender;

    @Schema(description = "Date of birth", example = "1995-05-15")
    private LocalDate dob;

    @Schema(description = "Address", example = "123 Nguyen Hue, District 1, HCMC")
    private String address;

    @Schema(description = "Phone", example = "0912345678")
    private String phone;

    @Schema(description = "Headline", example = "Senior Java Developer")
    private String headline;

    @Schema(description = "Bio", example = "Experienced developer with 5+ years...")
    private String bio;

    @Schema(description = "Years of experience", example = "5")
    private Integer experienceYears;

    @Schema(description = "Location", example = "Ho Chi Minh City")
    private String location;

    @Schema(description = "Website", example = "https://johndoe.dev")
    private String website;

    @Schema(description = "LinkedIn URL", example = "https://linkedin.com/in/johndoe")
    private String linkedinUrl;

    @Schema(description = "GitHub URL", example = "https://github.com/johndoe")
    private String githubUrl;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "Role name", example = "CANDIDATE")
    private String roleName;

    @Schema(description = "Role ID", example = "1")
    private Long roleId;

    @Schema(description = "Company ID", example = "1")
    private Long companyId;

    @Schema(description = "Company name", example = "TechCorp Inc")
    private String companyName;

    @Schema(description = "Is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Is deleted", example = "false")
    private Boolean deleted;

    @Schema(description = "Provider (GOOGLE/FACEBOOK/null)", example = "GOOGLE")
    private String provider;

    @Schema(description = "Created date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "Last modified date", example = "2024-01-20T15:45:00")
    private LocalDateTime lastModifiedDate;
}
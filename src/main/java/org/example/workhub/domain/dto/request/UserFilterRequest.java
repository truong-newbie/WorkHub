package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to filter and search users")
public class UserFilterRequest {

    @Schema(description = "Search keyword (username, email)", example = "john")
    private String keyword;

    @Schema(description = "Role name filter", example = "CANDIDATE")
    private String role;

    @Schema(description = "Gender filter", example = "MALE")
    private String gender;

    @Schema(description = "Minimum age", example = "20")
    private Integer minAge;

    @Schema(description = "Maximum age", example = "40")
    private Integer maxAge;

    @Schema(description = "Company ID", example = "company-uuid")
    private String companyId;

    @Schema(description = "Enabled status", example = "true")
    private Boolean enabled;

    @Schema(description = "Include deleted users", example = "false")
    private Boolean includeDeleted = false;

    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "10")
    private Integer size = 10;

    @Schema(description = "Sort field", example = "createdDate")
    private String sortBy = "createdDate";

    @Schema(description = "Sort direction (ASC/DESC)", example = "DESC")
    private String sortDir = "DESC";
}

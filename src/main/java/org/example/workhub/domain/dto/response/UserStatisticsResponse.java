package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User statistics response")
public class UserStatisticsResponse {

    @Schema(description = "Total users", example = "1500")
    private Long totalUsers;

    @Schema(description = "Active users (enabled)", example = "1400")
    private Long activeUsers;

    @Schema(description = "Locked users (disabled)", example = "100")
    private Long lockedUsers;

    @Schema(description = "Deleted users", example = "50")
    private Long deletedUsers;

    @Schema(description = "Users by role")
    private Map<String, Long> usersByRole;

    @Schema(description = "Users by gender")
    private Map<String, Long> usersByGender;

    @Schema(description = "New users this month", example = "45")
    private Long newUsersThisMonth;

    @Schema(description = "New users today", example = "5")
    private Long newUsersToday;
}
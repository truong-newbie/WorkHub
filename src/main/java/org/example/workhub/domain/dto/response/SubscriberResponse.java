package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscriber response DTO")
public class SubscriberResponse {

    @Schema(description = "Subscriber ID", example = "1")
    private Long id;

    @Schema(description = "Subscriber name", example = "Nguyen Van A")
    private String name;

    @Schema(description = "Subscriber email", example = "candidate@gmail.com")
    private String email;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;

    @Schema(description = "Deleted flag", example = "false")
    private Boolean deleted;

    @Schema(description = "Subscribed at")
    private LocalDateTime subscribedAt;

    @Schema(description = "Last email sent at")
    private LocalDateTime lastEmailSentAt;

    @Schema(description = "Unsubscribed at")
    private LocalDateTime unsubscribedAt;

    @Schema(description = "Owner info")
    private UserInfo user;

    @Schema(description = "Subscribed skills")
    private List<SkillInfo> skills;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Last modified date")
    private LocalDateTime lastModifiedDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SkillInfo {
        private Long id;
        private String name;
        private String level;
    }
}

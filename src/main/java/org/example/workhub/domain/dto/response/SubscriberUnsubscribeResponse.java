package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscriber unsubscribe response")
public class SubscriberUnsubscribeResponse {

    @Schema(description = "Subscriber email", example = "candidate@gmail.com")
    private String email;

    @Schema(description = "Enabled flag after unsubscribe", example = "false")
    private Boolean enabled;

    @Schema(description = "Unsubscribed at")
    private LocalDateTime unsubscribedAt;

    @Schema(description = "Message", example = "Unsubscribed successfully")
    private String message;
}

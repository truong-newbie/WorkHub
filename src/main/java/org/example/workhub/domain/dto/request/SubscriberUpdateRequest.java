package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update subscriber")
public class SubscriberUpdateRequest {

    @Email(message = "{subscriber.invalid.email}")
    @Schema(description = "Subscriber email", example = "candidate@gmail.com")
    private String email;

    @Schema(description = "Subscriber name", example = "Nguyen Van A")
    private String name;

    @Schema(description = "Skill IDs to subscribe", example = "[1,2]")
    private List<Long> skillIds;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;
}

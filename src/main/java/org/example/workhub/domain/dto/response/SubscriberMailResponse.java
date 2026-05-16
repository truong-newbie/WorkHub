package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscriber mail sending result")
public class SubscriberMailResponse {

    @Schema(description = "Number of subscribers checked", example = "10")
    private Integer checkedSubscribers;

    @Schema(description = "Number of emails sent", example = "3")
    private Integer sentEmails;

    @Schema(description = "Number of matched jobs", example = "8")
    private Integer matchedJobs;
}

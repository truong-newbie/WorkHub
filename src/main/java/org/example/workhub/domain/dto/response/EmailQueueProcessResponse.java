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
@Schema(description = "Email queue processing result")
public class EmailQueueProcessResponse {

    @Schema(description = "Number of email queue items checked", example = "20")
    private Integer checkedEmails;

    @Schema(description = "Number of emails sent successfully", example = "18")
    private Integer sentEmails;

    @Schema(description = "Number of emails requeued for retry", example = "1")
    private Integer retriedEmails;

    @Schema(description = "Number of emails marked failed", example = "1")
    private Integer failedEmails;
}

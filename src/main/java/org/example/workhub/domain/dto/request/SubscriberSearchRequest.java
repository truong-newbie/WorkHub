package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to search subscribers")
public class SubscriberSearchRequest {

    @Schema(description = "Dynamic filter expression", example = "email:*gmail*,enabled:true")
    private String filter;

    @Schema(description = "Email keyword", example = "gmail")
    private String email;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;

    @Schema(description = "Skill ID", example = "1")
    private Long skillId;

    @Schema(description = "Subscribed from date", example = "2026-05-01")
    private LocalDate subscribedFrom;

    @Schema(description = "Subscribed to date", example = "2026-05-16")
    private LocalDate subscribedTo;

    @Schema(description = "Page number, 0-based", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "10")
    private Integer size = 10;

    @Schema(description = "Sort field", example = "subscribedAt")
    private String sortBy = "subscribedAt";

    @Schema(description = "Sort direction", example = "DESC")
    private String sortDir = "DESC";
}

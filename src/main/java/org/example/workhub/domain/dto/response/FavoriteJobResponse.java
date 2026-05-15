package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Favorite job response DTO")
public class FavoriteJobResponse {

    @Schema(description = "Favorite ID", example = "1")
    private Long id;

    @Schema(description = "Job info")
    private JobResponse job;

    @Schema(description = "Saved at")
    private Instant savedAt;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;
}
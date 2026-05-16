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
@Schema(description = "Resume download metadata")
public class ResumeDownloadResponse {

    @Schema(description = "Resume ID", example = "1")
    private Long id;

    @Schema(description = "Resume title", example = "Senior Java Backend Resume")
    private String title;

    @Schema(description = "File name", example = "resume.pdf")
    private String fileName;

    @Schema(description = "File URL")
    private String fileUrl;

    @Schema(description = "File type", example = "pdf")
    private String fileType;

    @Schema(description = "File size in bytes", example = "120000")
    private Long fileSize;
}

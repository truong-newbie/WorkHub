package org.example.workhub.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.ErrorMessage;

@Getter
@Setter
@Schema(description = "Skill create/update request")
public class SkillRequestDto {

    @NotBlank(message = "{" + ErrorMessage.NOT_BLANK_FIELD + "}")
    @Size(max = 255, message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD + "}")
    @Schema(description = "Skill name", example = "Java")
    private String name;

    @Schema(description = "Skill level", example = "BACKEND")
    private String level;

    @Schema(description = "Skill description", example = "Java backend programming skill")
    private String description;

    @Schema(description = "Skill active status", example = "true")
    private Boolean active;
}

package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.ErrorMessage;

@Getter
@Setter
public class SkillRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @NotNull(message = "khong duoc de null")
    private String name;

    private String level;
}

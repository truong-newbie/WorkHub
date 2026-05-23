package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssessmentOptionRequest {

    @NotBlank(message = "{invalid.general.required}")
    private String content;

    private Boolean correct = false;
}

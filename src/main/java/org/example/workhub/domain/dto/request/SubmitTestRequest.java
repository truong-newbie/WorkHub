package org.example.workhub.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitTestRequest {

    @Valid
    @NotEmpty(message = "{invalid.general.required}")
    private List<SubmitAnswerRequest> answers;
}

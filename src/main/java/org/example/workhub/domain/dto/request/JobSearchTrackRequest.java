package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobSearchTrackRequest {

    @NotBlank(message = "{invalid.general.required}")
    private String keyword;

    private String filtersJson;
}

package org.example.workhub.domain.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.CandidateLevel;
import org.example.workhub.constant.EmploymentType;
import org.example.workhub.constant.ErrorMessage;
import org.example.workhub.constant.WorkMode;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
public class CandidateJobPreferenceCreateRequest {

    @NotBlank(message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED + "}")
    private String desiredJobTitle;

    @NotBlank(message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED + "}")
    private String preferredLocation;

    @NotNull(message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED + "}")
    private WorkMode workMode;

    @NotNull(message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED + "}")
    private EmploymentType employmentType;

    @NotNull(message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED + "}")
    private CandidateLevel candidateLevel;

    @Min(value = 0, message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD + "}")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD + "}")
    private BigDecimal expectedSalaryMin;

    @DecimalMin(value = "0.0", message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD + "}")
    private BigDecimal expectedSalaryMax;

    @NotEmpty(message = "{" + ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED + "}")
    private Set<Long> skillIds;
}

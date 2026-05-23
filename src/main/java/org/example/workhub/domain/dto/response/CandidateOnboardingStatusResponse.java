package org.example.workhub.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateOnboardingStatusResponse {

    private Boolean hasJobPreference;

    private Boolean requiredPreference;
}

package org.example.workhub.service;

import org.example.workhub.domain.dto.request.CandidateJobPreferenceCreateRequest;
import org.example.workhub.domain.dto.request.CandidateJobPreferenceUpdateRequest;
import org.example.workhub.domain.dto.response.CandidateJobPreferenceResponse;
import org.example.workhub.domain.dto.response.CandidateOnboardingStatusResponse;

public interface CandidateJobPreferenceService {

    CandidateOnboardingStatusResponse getOnboardingStatus();

    CandidateJobPreferenceResponse createPreference(CandidateJobPreferenceCreateRequest request);

    CandidateJobPreferenceResponse updatePreference(CandidateJobPreferenceUpdateRequest request);

    CandidateJobPreferenceResponse getMyPreference();
}

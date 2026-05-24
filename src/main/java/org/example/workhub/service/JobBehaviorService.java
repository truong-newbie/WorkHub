package org.example.workhub.service;

import org.example.workhub.domain.dto.request.JobBehaviorTrackRequest;
import org.example.workhub.domain.dto.request.JobSearchTrackRequest;
import org.example.workhub.domain.dto.response.JobBehaviorSummaryResponse;

public interface JobBehaviorService {

    void trackView(Long jobId, JobBehaviorTrackRequest request);

    void trackClick(Long jobId, JobBehaviorTrackRequest request);

    void trackSearch(JobSearchTrackRequest request);

    JobBehaviorSummaryResponse getBehaviorSummary();
}

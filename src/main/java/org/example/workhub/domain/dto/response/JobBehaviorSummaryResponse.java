package org.example.workhub.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobBehaviorSummaryResponse {

    private long totalViewed;

    private long totalClicked;

    private long totalSaved;

    private long totalApplied;

    private List<String> topSkillInterests;

    private List<String> topTitleKeywords;

    private List<String> topLocations;
}

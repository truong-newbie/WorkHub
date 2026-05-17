package org.example.workhub.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiResumeAnalysisResponse {

    @JsonProperty("raw_text")
    private String rawText;

    @JsonProperty("resume_skills")
    private List<String> resumeSkills;

    @JsonProperty("job_skills")
    private List<String> jobSkills;

    @JsonProperty("matched_skills")
    private List<String> matchedSkills;

    @JsonProperty("missing_skills")
    private List<String> missingSkills;

    @JsonProperty("extra_skills")
    private List<String> extraSkills;

    @JsonProperty("skill_score")
    private Double skillScore;

    @JsonProperty("semantic_score")
    private Double semanticScore;

    @JsonProperty("ai_summary")
    private String aiSummary;
}

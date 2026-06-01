package org.example.workhub.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.entity.common.DateAuditing;

@Entity
@Table(name = "tbl_screening_results")
@Getter
@Setter
public class ScreeningResult extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private JobApplication application;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "skill_score")
    private Double skillScore;

    @Column(name = "semantic_score")
    private Double semanticScore;

    @Column(name = "experience_score")
    private Double experienceScore;

    @Column(name = "education_score")
    private Double educationScore;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "extra_skills", columnDefinition = "TEXT")
    private String extraSkills;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "recommendation", length = 20)
    private String recommendation;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "explanation_status", length = 40)
    private String explanationStatus;

    @Column(name = "explanation_reason", columnDefinition = "TEXT")
    private String explanationReason;

    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;
}

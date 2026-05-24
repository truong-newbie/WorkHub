package org.example.workhub.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_job_recommendation_logs",
        indexes = {
                @Index(name = "idx_job_recommendation_user", columnList = "user_id"),
                @Index(name = "idx_job_recommendation_job", columnList = "job_id"),
                @Index(name = "idx_job_recommendation_generated_at", columnList = "generated_at")
        }
)
@Getter
@Setter
public class JobRecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    private Double contentScore;

    private Double behaviorScore;

    private Double collaborativeScore;

    private Double hybridScore;

    @Column(columnDefinition = "TEXT")
    private String reasonCodes;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    void prePersist() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}

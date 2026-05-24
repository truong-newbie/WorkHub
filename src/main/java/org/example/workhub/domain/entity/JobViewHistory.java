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
        name = "tbl_job_view_histories",
        indexes = {
                @Index(name = "idx_job_view_user", columnList = "user_id"),
                @Index(name = "idx_job_view_job", columnList = "job_id"),
                @Index(name = "idx_job_view_viewed_at", columnList = "viewed_at"),
                @Index(name = "idx_job_view_user_job", columnList = "user_id,job_id")
        }
)
@Getter
@Setter
public class JobViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    private String source;

    @Column(name = "session_id")
    private String sessionId;

    @PrePersist
    void prePersist() {
        if (viewedAt == null) {
            viewedAt = LocalDateTime.now();
        }
    }
}

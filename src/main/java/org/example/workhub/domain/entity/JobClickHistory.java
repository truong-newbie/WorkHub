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
        name = "tbl_job_click_histories",
        indexes = {
                @Index(name = "idx_job_click_user", columnList = "user_id"),
                @Index(name = "idx_job_click_job", columnList = "job_id"),
                @Index(name = "idx_job_click_clicked_at", columnList = "clicked_at")
        }
)
@Getter
@Setter
public class JobClickHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    private String source;

    @Column(name = "display_position")
    private Integer position;

    @PrePersist
    void prePersist() {
        if (clickedAt == null) {
            clickedAt = LocalDateTime.now();
        }
    }
}

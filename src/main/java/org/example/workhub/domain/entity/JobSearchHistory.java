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
        name = "tbl_job_search_histories",
        indexes = {
                @Index(name = "idx_job_search_user", columnList = "user_id"),
                @Index(name = "idx_job_search_searched_at", columnList = "searched_at"),
                @Index(name = "idx_job_search_keyword", columnList = "keyword")
        }
)
@Getter
@Setter
public class JobSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String keyword;

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    void prePersist() {
        if (searchedAt == null) {
            searchedAt = LocalDateTime.now();
        }
    }
}

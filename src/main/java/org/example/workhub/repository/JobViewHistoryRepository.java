package org.example.workhub.repository;

import org.example.workhub.domain.entity.JobViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobViewHistoryRepository extends JpaRepository<JobViewHistory, Long> {

    @Query("""
            SELECT h FROM JobViewHistory h
            LEFT JOIN FETCH h.job j
            LEFT JOIN FETCH j.skills
            LEFT JOIN FETCH j.company
            WHERE h.user.id = :userId AND h.viewedAt >= :since
            """)
    List<JobViewHistory> findByUserIdSince(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("""
            SELECT h FROM JobViewHistory h
            LEFT JOIN FETCH h.job j
            LEFT JOIN FETCH j.skills
            WHERE h.viewedAt >= :since
            """)
    List<JobViewHistory> findAllSince(@Param("since") LocalDateTime since);

    long countByUserIdAndViewedAtGreaterThanEqual(String userId, LocalDateTime since);
}

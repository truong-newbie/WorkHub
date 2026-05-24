package org.example.workhub.repository;

import org.example.workhub.domain.entity.JobClickHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobClickHistoryRepository extends JpaRepository<JobClickHistory, Long> {

    @Query("""
            SELECT h FROM JobClickHistory h
            LEFT JOIN FETCH h.job j
            LEFT JOIN FETCH j.skills
            LEFT JOIN FETCH j.company
            WHERE h.user.id = :userId AND h.clickedAt >= :since
            """)
    List<JobClickHistory> findByUserIdSince(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("""
            SELECT h FROM JobClickHistory h
            LEFT JOIN FETCH h.job j
            LEFT JOIN FETCH j.skills
            WHERE h.clickedAt >= :since
            """)
    List<JobClickHistory> findAllSince(@Param("since") LocalDateTime since);

    long countByUserIdAndClickedAtGreaterThanEqual(String userId, LocalDateTime since);
}

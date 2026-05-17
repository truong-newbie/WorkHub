package org.example.workhub.repository;

import org.example.workhub.domain.entity.ScreeningResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreeningResultRepository extends JpaRepository<ScreeningResult, Long> {

    Optional<ScreeningResult> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);

    @Query("""
            SELECT sr FROM ScreeningResult sr
            WHERE sr.application.job.id = :jobId
              AND sr.application.deleted = false
            ORDER BY sr.totalScore DESC
            """)
    List<ScreeningResult> findByApplicationJobIdOrderByTotalScoreDesc(@Param("jobId") Long jobId);
}

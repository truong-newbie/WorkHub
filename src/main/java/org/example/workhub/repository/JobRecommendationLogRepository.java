package org.example.workhub.repository;

import org.example.workhub.domain.entity.JobRecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRecommendationLogRepository extends JpaRepository<JobRecommendationLog, Long> {
}

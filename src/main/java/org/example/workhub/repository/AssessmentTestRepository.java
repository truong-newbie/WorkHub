package org.example.workhub.repository;

import org.example.workhub.constant.AssessmentStatus;
import org.example.workhub.domain.entity.AssessmentTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentTestRepository extends JpaRepository<AssessmentTest, Long> {

    Optional<AssessmentTest> findByIdAndDeletedFalse(Long id);

    List<AssessmentTest> findByJobIdAndDeletedFalse(Long jobId);

    @Query("SELECT t FROM AssessmentTest t WHERE t.recruiter.id = :recruiterId AND t.deleted = false")
    List<AssessmentTest> findByRecruiterId(@Param("recruiterId") String recruiterId);

    List<AssessmentTest> findByStatusAndDeletedFalse(AssessmentStatus status);
}

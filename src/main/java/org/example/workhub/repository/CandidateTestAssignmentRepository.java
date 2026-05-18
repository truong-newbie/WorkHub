package org.example.workhub.repository;

import org.example.workhub.constant.AssignmentStatus;
import org.example.workhub.domain.entity.CandidateTestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateTestAssignmentRepository extends JpaRepository<CandidateTestAssignment, Long> {

    List<CandidateTestAssignment> findByCandidateId(String candidateId);

    List<CandidateTestAssignment> findByTestId(Long testId);

    Optional<CandidateTestAssignment> findByIdAndCandidateId(Long id, String candidateId);

    boolean existsByTestIdAndApplicationId(Long testId, Long applicationId);

    boolean existsByTestIdAndStatus(Long testId, AssignmentStatus status);

    List<CandidateTestAssignment> findByTestIdAndStatus(Long testId, AssignmentStatus status);

    @Query("SELECT a FROM CandidateTestAssignment a WHERE a.test.id = :testId AND a.status = 'SUBMITTED'")
    List<CandidateTestAssignment> findSubmittedByTestId(@Param("testId") Long testId);
}

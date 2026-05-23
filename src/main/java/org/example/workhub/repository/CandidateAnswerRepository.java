package org.example.workhub.repository;

import org.example.workhub.domain.entity.CandidateAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateAnswerRepository extends JpaRepository<CandidateAnswer, Long> {

    List<CandidateAnswer> findByAssignmentId(Long assignmentId);

    boolean existsByAssignmentIdAndQuestionId(Long assignmentId, Long questionId);
}

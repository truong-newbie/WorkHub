package org.example.workhub.repository;

import org.example.workhub.domain.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    Optional<AssessmentQuestion> findByIdAndDeletedFalse(Long id);

    List<AssessmentQuestion> findByTestIdAndDeletedFalseOrderByOrderIndexAsc(Long testId);

    long countByTestIdAndDeletedFalse(Long testId);
}

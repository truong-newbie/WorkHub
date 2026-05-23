package org.example.workhub.repository;

import org.example.workhub.domain.entity.AssessmentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, Long> {

    Optional<AssessmentOption> findByIdAndQuestionId(Long id, Long questionId);

    List<AssessmentOption> findByQuestionId(Long questionId);
}

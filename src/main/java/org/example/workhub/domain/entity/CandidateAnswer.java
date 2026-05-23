package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "tbl_candidate_answers",
        uniqueConstraints = @UniqueConstraint(name = "uk_answer_assignment_question", columnNames = {"assignment_id", "question_id"})
)
@Getter
@Setter
public class CandidateAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private CandidateTestAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private AssessmentOption selectedOption;

    @Column(name = "essay_answer", columnDefinition = "TEXT")
    private String essayAnswer;

    private Double score;

    @Column(columnDefinition = "TEXT")
    private String feedback;
}

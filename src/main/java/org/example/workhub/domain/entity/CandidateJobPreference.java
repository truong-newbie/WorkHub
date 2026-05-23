package org.example.workhub.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.CandidateLevel;
import org.example.workhub.constant.EmploymentType;
import org.example.workhub.constant.WorkMode;
import org.example.workhub.domain.entity.common.DateAuditing;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tbl_candidate_job_preferences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_candidate_job_preference_candidate", columnNames = "candidate_id")
        }
)
@Getter
@Setter
public class CandidateJobPreference extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "desired_job_title", nullable = false)
    private String desiredJobTitle;

    @Column(name = "preferred_location", nullable = false)
    private String preferredLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "candidate_level", nullable = false)
    private CandidateLevel candidateLevel;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "expected_salary_min", precision = 19, scale = 2)
    private BigDecimal expectedSalaryMin;

    @Column(name = "expected_salary_max", precision = 19, scale = 2)
    private BigDecimal expectedSalaryMax;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_candidate_job_preference_skills",
            joinColumns = @JoinColumn(name = "preference_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();
}

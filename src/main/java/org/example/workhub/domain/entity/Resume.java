package org.example.workhub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.entity.common.UserDateAuditing;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tbl_resumes")
@Getter
@Setter
public class Resume extends UserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String email;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    private String fileUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ats_score")
    private Integer atsScore;

    @Column(name = "parsed_content", columnDefinition = "LONGTEXT")
    private String parsedContent;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    @JsonIgnore
    private Job job;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"jobs", "subscribers"})
    @JoinTable(
            name = "tbl_resume_skill",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;
}

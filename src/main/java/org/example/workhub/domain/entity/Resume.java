package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.StatusEnum;
import org.example.workhub.domain.entity.common.UserDateAuditing;

@Entity
@Table(name = "tbl_resumes")
@Getter
@Setter
public class Resume extends UserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email khong duoc de trong")
    private String email;

    @NotBlank(message = "url khong duoc de trong (upload cv chua thanh cong)")
    private String url;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;


}
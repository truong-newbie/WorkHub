package org.example.workhub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.entity.common.FlagUserDateAuditing;

import java.util.List;

@Entity
@Table(name = "tbl_companies")
@Getter
@Setter
public class Company extends FlagUserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ten cong ty khong duoc de trong")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    private String logo;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;


    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Job> jobs;

}
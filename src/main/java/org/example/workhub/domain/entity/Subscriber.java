package org.example.workhub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.entity.common.UserDateAuditing;

import java.util.List;

@Entity
@Table(name = "table_subscribers")
@Getter
@Setter
public class Subscriber extends UserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name khong duoc de trong")
    private String name;

    @NotBlank(message = "email khong duoc de trong")
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("subscribers")
    @JoinTable(
            name = "tbl_subscriber_skill",
            joinColumns = @JoinColumn(name = "subscriber_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;


}
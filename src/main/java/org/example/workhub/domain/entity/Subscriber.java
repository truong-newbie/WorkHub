package org.example.workhub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.entity.common.UserDateAuditing;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "table_subscribers")
@Getter
@Setter
public class Subscriber extends UserDateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "last_email_sent_at")
    private LocalDateTime lastEmailSentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("subscribers")
    @JoinTable(
            name = "tbl_subscriber_skill",
            joinColumns = @JoinColumn(name = "subscriber_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;
}

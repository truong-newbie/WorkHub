package org.example.workhub.domain.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.workhub.constant.GenderEnum;
import org.example.workhub.domain.entity.common.DateAuditing;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.io.Serializable;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User extends DateAuditing implements Serializable {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "name")
    private String username;

    @NotBlank(message = "email khong duoc de trong")
    @Column(name = "email")
    private String email;

    @NotBlank(message = "password khong duoc de trong")
    @Column(name = "password")
    private String password;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private GenderEnum gender;

    @Column(name =" dob" , nullable = false)
    private LocalDate dob;

    @Column(name = "address")
    private String address;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Resume> resumes;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

}

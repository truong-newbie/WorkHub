package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne
    private User user;
}

package org.example.workhub.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.workhub.constant.CommonConstant;

import java.time.LocalDateTime;


@Entity
@Table(name="token_blacklist")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token = CommonConstant.BEARER_TOKEN;

    @Column(name = "token_type", nullable = false)
    private String tokenType;

    @Column(nullable = false)
    private String reason;

    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blackListAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @PrePersist
    public void prePersist() {
        if (blackListAt == null) blackListAt = LocalDateTime.now();
        if (expiredAt == null) expiredAt = LocalDateTime.now().plusDays(1);
    }
}

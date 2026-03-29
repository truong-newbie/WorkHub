package org.example.workhub.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.workhub.domain.entity.common.DateAuditing;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="user_session")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(name="token" , columnDefinition = "TEXT", nullable = false)
    private String token;

    @Column(name="refresh_token" , columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @Column(name="ip_address" , nullable = false)
    private String ipAddress;

    @Column(name="is_active" , nullable = false , columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable= false)
    @OnDelete(action= OnDeleteAction.CASCADE)
    private User user;

    public void prePersist(){
        this.isActive= Boolean.TRUE;
    }

}

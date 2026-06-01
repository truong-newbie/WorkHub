package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.domain.entity.common.DateAuditing;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_chat_conversations",
        indexes = {
                @Index(name = "idx_chat_conversation_candidate", columnList = "candidate_id"),
                @Index(name = "idx_chat_conversation_last_message", columnList = "last_message_at")
        }
)
@Getter
@Setter
public class ChatConversation extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "last_message_at", nullable = false)
    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    void prePersist() {
        if (lastMessageAt == null) {
            lastMessageAt = LocalDateTime.now();
        }
        if (deleted == null) {
            deleted = false;
        }
    }
}


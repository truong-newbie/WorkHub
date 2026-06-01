package org.example.workhub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.workhub.constant.ChatIntent;
import org.example.workhub.constant.ChatResponseMode;
import org.example.workhub.constant.ChatSenderType;
import org.example.workhub.domain.entity.common.DateAuditing;

@Entity
@Table(
        name = "tbl_chat_messages",
        indexes = {
                @Index(name = "idx_chat_message_conversation", columnList = "conversation_id"),
                @Index(name = "idx_chat_message_created", columnList = "created_date")
        }
)
@Getter
@Setter
public class ChatMessage extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 30)
    private ChatSenderType senderType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ChatIntent intent;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_mode", length = 30)
    private ChatResponseMode responseMode;

    @Column(name = "out_of_scope", nullable = false)
    private Boolean outOfScope = false;

    @Column(name = "sources_json", columnDefinition = "TEXT")
    private String sourcesJson;

    @Column(name = "actions_json", columnDefinition = "TEXT")
    private String actionsJson;

    @PrePersist
    void prePersist() {
        if (outOfScope == null) {
            outOfScope = false;
        }
    }
}


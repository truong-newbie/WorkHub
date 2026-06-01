package org.example.workhub.repository;

import org.example.workhub.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByConversationId(Long conversationId, Pageable pageable);

    List<ChatMessage> findByConversationId(Long conversationId);

    Optional<ChatMessage> findFirstByConversationIdOrderByCreatedDateDesc(Long conversationId);
}


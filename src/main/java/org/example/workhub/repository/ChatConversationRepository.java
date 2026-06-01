package org.example.workhub.repository;

import org.example.workhub.domain.entity.ChatConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByIdAndCandidateIdAndDeletedFalse(Long id, String candidateId);

    Page<ChatConversation> findByCandidateIdAndDeletedFalse(String candidateId, Pageable pageable);
}


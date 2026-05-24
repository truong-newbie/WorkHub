package org.example.workhub.repository;

import org.example.workhub.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    Optional<Notification> findByIdAndRecipientIdAndDeletedFalse(Long id, String recipientId);

    long countByRecipientIdAndReadFalseAndDeletedFalse(String recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.recipient.id = :recipientId AND n.read = false AND n.deleted = false")
    void markAllAsRead(@Param("recipientId") String recipientId, @Param("readAt") LocalDateTime readAt);
}

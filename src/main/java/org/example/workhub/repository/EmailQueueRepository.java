package org.example.workhub.repository;

import org.example.workhub.constant.EmailQueueStatus;
import org.example.workhub.domain.entity.EmailQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {

    @EntityGraph(attributePaths = {"subscriber"})
    @Query("""
            SELECT q FROM EmailQueue q
            WHERE q.status IN :statuses
              AND q.nextAttemptAt <= :now
              AND q.retryCount < q.maxRetry
            ORDER BY q.createdDate ASC
            """)
    List<EmailQueue> findReadyToProcess(
            @Param("statuses") Collection<EmailQueueStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    boolean existsBySubscriberIdAndStatusIn(Long subscriberId, Collection<EmailQueueStatus> statuses);
}

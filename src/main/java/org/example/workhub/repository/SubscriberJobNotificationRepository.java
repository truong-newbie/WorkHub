package org.example.workhub.repository;

import org.example.workhub.constant.SubscriberJobNotificationStatus;
import org.example.workhub.domain.entity.SubscriberJobNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SubscriberJobNotificationRepository extends JpaRepository<SubscriberJobNotification, Long> {

    boolean existsBySubscriberIdAndJobIdAndStatusIn(
            Long subscriberId,
            Long jobId,
            Collection<SubscriberJobNotificationStatus> statuses
    );

    List<SubscriberJobNotification> findByEmailQueueId(Long emailQueueId);
}

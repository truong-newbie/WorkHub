package org.example.workhub.repository;

import org.example.workhub.constant.RecruiterRequestStatus;
import org.example.workhub.domain.entity.RecruiterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruiterRequestRepository extends JpaRepository<RecruiterRequest, Long> {

    boolean existsByUserIdAndStatus(String userId, RecruiterRequestStatus status);

    Page<RecruiterRequest> findByUserIdOrderByCreatedDateDesc(String userId, Pageable pageable);

    Page<RecruiterRequest> findAllByOrderByCreatedDateDesc(Pageable pageable);

    Page<RecruiterRequest> findByStatusOrderByCreatedDateDesc(RecruiterRequestStatus status, Pageable pageable);

    List<RecruiterRequest> findByUserIdAndStatusAndIdNot(
            String userId,
            RecruiterRequestStatus status,
            Long requestId);
}

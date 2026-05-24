package org.example.workhub.repository;

import org.example.workhub.domain.entity.FavoriteJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

@Repository
public interface FavoriteJobRepository extends JpaRepository<FavoriteJob, Long> {

    @Query("SELECT fj FROM FavoriteJob fj WHERE fj.job.id = :jobId AND fj.user.id = :userId AND fj.deleted = false")
    Optional<FavoriteJob> findByJobIdAndUserId(@Param("jobId") Long jobId, @Param("userId") String userId);

    boolean existsByJobIdAndUserIdAndDeletedFalse(Long jobId, String userId);

    @Query("SELECT fj FROM FavoriteJob fj WHERE fj.user.id = :userId AND fj.deleted = false")
    Page<FavoriteJob> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT COUNT(fj) FROM FavoriteJob fj WHERE fj.user.id = :userId AND fj.deleted = false")
    long countByUserId(@Param("userId") String userId);

    @Query("""
            SELECT fj FROM FavoriteJob fj
            LEFT JOIN FETCH fj.job j
            LEFT JOIN FETCH j.skills
            LEFT JOIN FETCH j.company
            WHERE fj.user.id = :userId
              AND fj.deleted = false
              AND (fj.createdAt IS NULL OR fj.createdAt >= :since)
            """)
    List<FavoriteJob> findActiveByUserIdSince(@Param("userId") String userId, @Param("since") Instant since);

    @Query("""
            SELECT fj FROM FavoriteJob fj
            LEFT JOIN FETCH fj.job j
            LEFT JOIN FETCH j.skills
            WHERE fj.deleted = false
              AND (fj.createdAt IS NULL OR fj.createdAt >= :since)
            """)
    List<FavoriteJob> findAllActiveSince(@Param("since") Instant since);
}

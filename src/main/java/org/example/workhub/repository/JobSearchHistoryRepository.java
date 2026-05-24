package org.example.workhub.repository;

import org.example.workhub.domain.entity.JobSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobSearchHistoryRepository extends JpaRepository<JobSearchHistory, Long> {

    @Query("""
            SELECT h FROM JobSearchHistory h
            WHERE h.user.id = :userId AND h.searchedAt >= :since
            """)
    List<JobSearchHistory> findByUserIdSince(@Param("userId") String userId, @Param("since") LocalDateTime since);

    long countByUserIdAndSearchedAtGreaterThanEqual(String userId, LocalDateTime since);
}

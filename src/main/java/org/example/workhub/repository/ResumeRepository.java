package org.example.workhub.repository;

import org.example.workhub.domain.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>, JpaSpecificationExecutor<Resume> {

    @EntityGraph(attributePaths = {"user", "job", "skills"})
    @Query("SELECT r FROM Resume r WHERE r.id = :id AND r.deleted = false")
    Optional<Resume> findByIdAndDeletedFalse(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "job", "skills"})
    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.deleted = false")
    Page<Resume> findByUserIdAndDeletedFalse(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.deleted = false AND r.isDefault = true")
    Optional<Resume> findDefaultByUserId(@Param("userId") String userId);

    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.deleted = false AND lower(r.title) = lower(:title)")
    Optional<Resume> findByUserIdAndTitleIgnoreCase(@Param("userId") String userId, @Param("title") String title);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Resume r WHERE r.user.id = :userId AND r.deleted = false AND lower(r.title) = lower(:title) AND r.id <> :id")
    boolean existsByUserIdAndTitleIgnoreCaseAndIdNot(@Param("userId") String userId, @Param("title") String title, @Param("id") Long id);

    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.deleted = false AND r.isDefault = true")
    List<Resume> findDefaultsByUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"user", "job", "skills"})
    @Query("""
            SELECT r FROM Resume r
            WHERE r.user.id = :candidateId
              AND r.deleted = false
              AND (r.isDefault = true OR r.isPublic = true)
            ORDER BY r.isDefault DESC, r.uploadedAt DESC
            """)
    List<Resume> findShareableByCandidateId(@Param("candidateId") String candidateId);
}

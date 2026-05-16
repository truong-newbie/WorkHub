package org.example.workhub.repository;

import org.example.workhub.domain.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long>, JpaSpecificationExecutor<Skill> {
    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndDeletedFalseAndIdNot(String name, Long id);

    boolean existsBySlugAndDeletedFalse(String slug);

    boolean existsBySlugAndDeletedFalseAndIdNot(String slug, Long id);

    Optional<Skill> findByIdAndDeletedFalse(long id);

    Optional<Skill> findByIdAndActiveTrueAndDeletedFalse(long id);

    Page<Skill> findByActiveTrueAndDeletedFalse(Pageable pageable);

    @Query("""
            SELECT s FROM Skill s
            WHERE s.deleted = false
              AND s.active = true
              AND LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY s.name ASC
            """)
    List<Skill> findActiveSuggestions(String keyword, Pageable pageable);

    @Query("""
            SELECT s, COUNT(j.id) FROM Skill s
            LEFT JOIN s.jobs j WITH j.deleted = false AND j.published = true
            WHERE s.deleted = false AND s.active = true
            GROUP BY s
            ORDER BY COUNT(j.id) DESC, s.name ASC
            """)
    List<Object[]> findPopularActiveSkills(Pageable pageable);

    @Query("SELECT COUNT(j.id) FROM Job j JOIN j.skills s WHERE s.id = :skillId AND j.deleted = false")
    long countJobsUsingSkill(Long skillId);

    @Query("SELECT COUNT(r.id) FROM Resume r JOIN r.skills s WHERE s.id = :skillId AND r.deleted = false")
    long countResumesUsingSkill(Long skillId);

    @Query("SELECT COUNT(sub.id) FROM Subscriber sub JOIN sub.skills s WHERE s.id = :skillId AND sub.deleted = false")
    long countSubscribersUsingSkill(Long skillId);
}

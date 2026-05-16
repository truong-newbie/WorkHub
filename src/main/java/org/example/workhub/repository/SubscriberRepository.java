package org.example.workhub.repository;

import org.example.workhub.domain.entity.Subscriber;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, JpaSpecificationExecutor<Subscriber> {

    @EntityGraph(attributePaths = {"user", "skills"})
    @Query("SELECT s FROM Subscriber s WHERE s.id = :id AND s.deleted = false")
    Optional<Subscriber> findByIdAndDeletedFalse(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "skills"})
    @Query("SELECT s FROM Subscriber s WHERE s.user.id = :userId AND s.deleted = false")
    Optional<Subscriber> findByUserIdAndDeletedFalse(@Param("userId") String userId);

    @Query("SELECT s FROM Subscriber s WHERE lower(s.email) = lower(:email) AND s.deleted = false")
    Optional<Subscriber> findByEmailIgnoreCaseAndDeletedFalse(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscriber s WHERE lower(s.email) = lower(:email) AND s.deleted = false")
    boolean existsByEmailIgnoreCaseAndDeletedFalse(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscriber s WHERE lower(s.email) = lower(:email) AND s.deleted = false AND s.id <> :id")
    boolean existsByEmailIgnoreCaseAndDeletedFalseAndIdNot(@Param("email") String email, @Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "skills"})
    @Query("SELECT DISTINCT s FROM Subscriber s LEFT JOIN s.skills sk WHERE s.enabled = true AND s.deleted = false")
    List<Subscriber> findAllEnabledWithSkills();
}

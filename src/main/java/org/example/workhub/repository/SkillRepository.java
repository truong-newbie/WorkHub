package org.example.workhub.repository;

import org.example.workhub.domain.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameAndDeletedFalse(String name);
    Optional<Skill> findByIdAndDeletedFalse(long id);

    Page<Skill> findAll(Specification<Skill> and, Pageable pageable);
}

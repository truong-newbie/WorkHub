package org.example.workhub.repository;

import org.example.workhub.domain.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameAndDeletedFalse(String name);
    Optional<Skill> findByIdAndDeletedFalse(long id);

    Page<Skill> findAll(Specification<Skill> and, Pageable pageable);
}

package org.example.workhub.repository;

import org.example.workhub.domain.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill,Long> {
    List<Skill> findByIdIn(List<Long> skillIds);
}

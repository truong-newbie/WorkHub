# Skill Module

## Purpose

Handles skill search, public active skill reads, admin skill management, enable/disable, suggestions, popular skills, and soft delete rules.

## Main Files

- `controller/SkillController.java`
- `service/SkillService.java`
- `service/impl/SkillServiceImpl.java`
- `domain.entity.Skill.java`
- `domain.mapper.SkillMapper.java`
- `domain.specification.SkillSpecification.java`
- `repository/SkillRepository.java`

## Key APIs

- `GET /api/v1/skills`
- `POST /api/v1/skills`
- `GET /api/v1/skills/{id}`
- `PUT /api/v1/skills/{id}`
- `DELETE /api/v1/skills/{id}`
- `PUT /api/v1/skills/{id}/enable`
- `PUT /api/v1/skills/{id}/disable`
- `GET /api/v1/skills/suggestions`
- `GET /api/v1/skills/popular`

## Business Rules

- Public read returns active, non-deleted skills.
- Admin manages create/update/delete/enable/disable.
- Delete is soft delete.
- Delete is blocked when skill is used by Job, Resume, or Subscriber relations.
- Skill names/slugs are normalized and checked for duplicates.

## AI Notes

Do not reimplement skill CRUD. Reuse current service and mapper patterns.

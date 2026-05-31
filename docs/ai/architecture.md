# WorkHub Architecture

## Overview

WorkHub is an ITviec-like recruitment backend built with Spring Boot 3, Java 17, Maven, MySQL, Spring Security, JWT, JPA/Hibernate, DTO/Mapper, MapStruct, i18n, global exception handling, and a base response wrapper.

Root package:

```text
org.example.workhub
```

API base path:

```text
/api/v1
```

## Main Layers

- `controller`: REST endpoints. Controllers should stay thin and delegate business logic to services.
- `service`: Service interfaces.
- `service.impl`: Business logic and transaction boundaries.
- `repository`: Spring Data JPA repositories.
- `domain.entity`: JPA entities.
- `domain.dto.request`: Request DTOs.
- `domain.dto.response`: Response DTOs.
- `domain.mapper`: MapStruct mappers.
- `domain.specification`: JPA specifications for search/filter.
- `constant`: Enums, URL constants, sort mappings, roles, i18n message keys.
- `exception`: Custom exceptions and global handler.
- `security`: JWT, user principal, method security and web security config.
- `queue`: RabbitMQ config, messages, producers, consumers.
- `listener`: Spring event listeners.
- `util`: Shared helpers.

## Existing Modules

- Auth
- User
- Company
- Skill
- Job
- Job Application
- Resume
- Subscriber
- ATS screening with RabbitMQ and local embedding-based AI worker
- Assessment
- Recommendation
- Notification
- Elasticsearch job search
- RabbitMQ background queue system

## Important Integration Patterns

- Controllers use `@RestApiV1` and return `VsResponseUtil.success(...)`.
- Business logic lives in service implementations, not controllers.
- Security is enforced both by `@PreAuthorize` and service-level ownership checks.
- Errors must use `ErrorMessage` constants and i18n properties.
- Search/filter should use specifications and existing pagination helpers.
- Async/background work should use RabbitMQ queue messages containing IDs, not full entities.

## Runtime Services

- MySQL: source of truth.
- RabbitMQ: background job transport.
- Elasticsearch: job search index.
- SMTP: email delivery through existing `EmailService`.
- WebSocket/STOMP: realtime notifications.
- AI worker: FastAPI service for resume parsing, keyword matching, and local embedding-based ATS semantic scoring.

## AI Loading Rule

When working on a feature, load this file plus only the relevant module docs under `docs/ai/modules`. Avoid loading every module doc unless the task is cross-cutting.

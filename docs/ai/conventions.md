# WorkHub Coding Conventions

## Response Format

Use the project response wrapper:

```java
return VsResponseUtil.success(data);
return VsResponseUtil.success(HttpStatus.CREATED, data);
```

Do not return raw DTOs directly from controllers unless the existing controller already does so.

## Error Handling

Never hardcode business error messages in exceptions.

Use:

```java
throw new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id});
throw new BadRequestException(ErrorMessage.INVALID_SOME_THING_FIELD);
```

Then add keys to:

- `src/main/resources/i18n/messages_en.properties`
- `src/main/resources/i18n/messages_vi.properties`
- `src/main/resources/i18n/messages_vn.properties`

## DTO Pattern

- Request DTOs live in `domain.dto.request`.
- Response DTOs live in `domain.dto.response`.
- Use validation annotations on request DTOs.
- Use `{i18n.key}` for validation messages.
- Do not expose entities directly from new APIs.

## Mapper Pattern

- Use MapStruct mappers in `domain.mapper`.
- Do not map auditing fields manually unless the existing mapper already does.
- For nested response values, prefer mapper expressions or service-layer assembly consistent with nearby code.

## Pagination Pattern

- Use existing pagination DTOs and `PagingMeta`.
- Existing modules may use `PaginationUtil`; follow the local module pattern.
- Page indexes are usually zero-based in requests and shown as one-based in metadata.

## Specification Pattern

- Use JPA Specification classes for dynamic filters.
- Do not implement ad hoc query parsing if a specification helper already exists.
- Keep allowed sort fields explicit per module.

## Soft Delete

Most business entities use soft delete:

```java
entity.setDeleted(true);
```

Repository queries and specifications should exclude deleted records unless admin/search behavior explicitly requires otherwise.

## Transactions

- Put `@Transactional` on service implementations.
- Use `@Transactional(readOnly = true)` for read methods.
- For RabbitMQ publishing after DB writes, publish after commit when consumers need to read the newly saved row.

## Background Jobs

- Message DTOs must contain IDs and minimal payload only.
- Consumers load entities from DB.
- Consumers should throw exceptions for retry/DLQ when processing fails.
- For non-critical jobs like email/notification, do not break the main business flow after the main DB transaction succeeds.

## Documentation Updates

After a large feature, update the corresponding module doc in `docs/ai/modules`. If the feature is cross-cutting, also update `architecture.md`, `database.md`, or `api-overview.md`.

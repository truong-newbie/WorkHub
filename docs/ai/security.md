# WorkHub Security

## Authentication

WorkHub uses JWT access and refresh tokens.

Typical login endpoint:

```text
POST /api/v1/auth/login
```

Authenticated requests use:

```text
Authorization: Bearer <accessToken>
```

## Role Constants

Role names in code and database include the `ROLE_` prefix:

```java
ROLE_ADMIN
ROLE_RECRUITER
ROLE_CANDIDATE
```

When using `hasRole`, Spring adds the prefix:

```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
```

## Common Access Rules

- Auth APIs: public.
- User profile: authenticated.
- User admin management: admin only.
- Skill read: public or authenticated depending on endpoint.
- Skill write: admin only.
- Job write: recruiter/admin.
- Job application candidate actions: authenticated candidate flow.
- Recruiter job/application actions: recruiter/admin plus ownership checks.
- Resume owner actions: owner/admin.
- Candidate resume access for recruiter: only after candidate applied to recruiter's job.
- Subscriber management: owner/admin; search/manual mail trigger recruiter/admin.
- Notifications: authenticated owner.
- Queue admin/test endpoints: existing protected endpoints only; do not add public queue APIs.

## Service-Level Ownership Checks

Do not rely only on controller annotations. Existing services validate:

- resource owner
- recruiter job ownership
- company ownership
- admin override

Follow the existing service method pattern when adding new APIs.

## WebSocket Notifications

Notification module uses STOMP over WebSocket:

```text
/ws
/user/queue/notifications
```

JWT is accepted through the `Authorization` header or `?token=`.

## Security Rule For New Features

Before adding an endpoint:

1. Identify whether it is public, authenticated, owner-only, recruiter/admin, or admin-only.
2. Add `@PreAuthorize`.
3. Add service-level ownership checks.
4. Avoid creating public APIs for internal background work.

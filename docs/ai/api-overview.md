# WorkHub API Overview

Base path:

```text
/api/v1
```

## Core Groups

- Auth: `/auth/**`
- User: `/user/**`
- Company: `/companies/**`
- Skill: `/skills/**`
- Job: `/job/**`, `/jobs/**`
- Job Application: `/job/{jobId}/apply`, `/applications/**`, `/recruiter/applications/**`
- Resume: `/resume/**`
- Subscriber: `/subscribers/**`
- Candidate recommendation: `/candidate/**`
- Recruiter assessment: `/recruiter/**`
- Notification: `/notifications/**`
- Job search: `/jobs/search/**`
- Candidate chatbot: `/chat/**`

## Response Wrapper

Successful responses usually follow:

```json
{
  "status": "SUCCESS",
  "data": {}
}
```

## Auth Header

```text
Authorization: Bearer <accessToken>
```

## API Design Rules

- Use plural resource paths where current module already uses plural paths.
- Keep backward compatibility with existing URLs.
- Do not add queue/internal public APIs unless explicitly required.
- Manual trigger endpoints must remain role-protected.

## Postman Collections

Queue feature test collection:

```text
postman/WorkHub_Background_Queue_APIs.postman_collection.json
```

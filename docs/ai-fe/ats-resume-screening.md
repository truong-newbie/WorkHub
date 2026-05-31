# ATS Resume Screening Frontend Contract

The ATS endpoints remain unchanged:

```text
POST /api/v1/recruiter/applications/{applicationId}/screen
GET  /api/v1/recruiter/applications/{applicationId}/screening-result
GET  /api/v1/recruiter/jobs/{jobId}/screening-results
```

## Queue Screening

Queue or re-screen an application:

```http
POST /api/v1/recruiter/applications/{applicationId}/screen
Authorization: Bearer <accessToken>
```

No request body is required.

## Result Fields

The result and ranking APIs already expose:

```json
{
  "totalScore": 78.4,
  "skillScore": 72.0,
  "semanticScore": 88.0,
  "matchedSkills": ["Java", "Spring Boot"],
  "missingSkills": ["Kafka"],
  "extraSkills": ["Redis"],
  "aiSummary": "ATS score 78.4. Matched skills: Java, Spring Boot. Missing skills: Kafka."
}
```

Field meaning:

| Field | Meaning |
| --- | --- |
| `skillScore` | Keyword skill match score from `0` to `100` |
| `semanticScore` | Embedding similarity score from `0` to `100` |
| `totalScore` | `skillScore * 0.60 + semanticScore * 0.40` |

The AI worker now calculates `semanticScore` with
`sentence-transformers/all-MiniLM-L6-v2`. It is no longer a fixed placeholder.

## Error Handling

- Queue endpoint success means processing has started asynchronously.
- Read result again after receiving the ATS completion notification.
- A result API `404` means there is no completed screening result yet.
- Re-screening uses the same queue endpoint. There is no separate retry endpoint.

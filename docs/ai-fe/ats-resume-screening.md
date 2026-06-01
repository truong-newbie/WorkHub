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
  "strengths": ["Strong Java and Spring Boot experience"],
  "weaknesses": ["Kafka is not shown in the resume"],
  "recommendation": "CONSIDER",
  "confidence": 86.0,
  "summary": "The candidate matches the core backend requirements.",
  "explanationStatus": "CALCULATED",
  "explanationReason": null,
  "aiSummary": "The candidate matches the core backend requirements."
}
```

Field meaning:

| Field | Meaning |
| --- | --- |
| `skillScore` | Keyword skill match score from `0` to `100` |
| `semanticScore` | Embedding similarity score from `0` to `100` |
| `totalScore` | `skillScore * 0.60 + semanticScore * 0.40` |
| `strengths` | Recruiter-readable strengths from Gemini or deterministic fallback |
| `weaknesses` | Recruiter-readable weaknesses from Gemini or deterministic fallback |
| `recommendation` | One of `PASS`, `CONSIDER`, or `REJECT` |
| `confidence` | Explanation confidence from `0` to `100` |
| `summary` | Main recruiter-readable explanation |
| `explanationStatus` | Whether Gemini ran or a fallback was used |
| `explanationReason` | Optional reason when Gemini was skipped or failed |
| `aiSummary` | Backward-compatible alias of `summary` |

The AI worker now calculates `semanticScore` with
`sentence-transformers/all-MiniLM-L6-v2`. It is no longer a fixed placeholder.
When enabled, Gemini 2.5 Flash adds explanation fields without changing the ATS
score formula.

## Error Handling

- Queue endpoint success means processing has started asynchronously.
- Read result again after receiving the ATS completion notification.
- A result API `404` means there is no completed screening result yet.
- Re-screening uses the same queue endpoint. There is no separate retry endpoint.
- A repeated screen for the same resume and job can reuse a persisted
  Gemini-enriched result.
- Existing PDF resumes uploaded under a restricted Cloudinary `image/upload`
  URL need to be uploaded again. New document uploads use Cloudinary
  `resource_type=raw`.

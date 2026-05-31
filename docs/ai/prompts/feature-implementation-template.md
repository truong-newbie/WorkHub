# Feature Implementation Prompt Template

Use this when asking AI to implement a new WorkHub feature.

```text
You are working in WorkHub.

Load only these docs first:
- docs/ai/architecture.md
- docs/ai/conventions.md
- docs/ai/security.md
- docs/ai/database.md
- docs/ai/modules/<relevant-module>.md

Goal:
<describe feature>

Requirements:
- Reuse existing services/entities/repositories when present.
- Do not create duplicate modules.
- Keep endpoints under /api/v1.
- Use DTOs, mappers, i18n ErrorMessage keys, GlobalExceptionHandler conventions.
- Add service-level security/ownership checks.
- Add or update docs/ai/modules/<module>.md after implementation.

Before coding:
1. Search existing code for related classes.
2. List files to reuse.
3. List files to create.
4. List files to modify.
5. Explain old flow and new flow.

After coding:
1. List created/modified files.
2. Explain API changes.
3. Explain security rules.
4. Explain how to test.
5. Run mvn clean package -DskipTests.
```

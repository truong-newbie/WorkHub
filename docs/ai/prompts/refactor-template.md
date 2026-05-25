# Refactor Prompt Template

Use this when asking AI to refactor WorkHub code.

```text
You are working in WorkHub.

Load only these docs first:
- docs/ai/architecture.md
- docs/ai/conventions.md
- docs/ai/modules/<relevant-module>.md

Refactor goal:
<describe desired improvement>

Constraints:
- No behavior change unless explicitly requested.
- No endpoint contract changes.
- Preserve security and ownership checks.
- Keep edits scoped to related files.
- Run build after refactor.

Before coding:
1. Identify current flow.
2. Identify risks.
3. List files to modify.

After coding:
1. Explain what changed.
2. Explain why behavior is preserved.
3. Run mvn clean package -DskipTests.
```

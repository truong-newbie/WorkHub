# Bugfix Prompt Template

Use this when asking AI to fix a bug.

```text
You are working in WorkHub.

Load only these docs first:
- docs/ai/architecture.md
- docs/ai/conventions.md
- docs/ai/security.md if the bug touches auth/access
- docs/ai/database.md if the bug touches JPA/entity/database
- docs/ai/modules/<relevant-module>.md

Bug:
<paste error, stack trace, request, expected behavior>

Constraints:
- Do not rewrite unrelated code.
- Do not revert user changes.
- Reuse current patterns.
- Preserve API compatibility.
- Use i18n error keys for new business errors.

Required output:
- Root cause
- Fix summary
- Files changed
- Verification command/result
```

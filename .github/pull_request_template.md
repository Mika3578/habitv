<!-- Keep this PR small, scoped, and reviewable. Use English only. -->

## Summary

<!-- One or two sentences describing what this PR delivers and why. -->

## Scope

<!-- Bullet list of what this PR changes. -->
-

## Out of scope

<!-- Bullet list of intentional non-changes to avoid scope creep. -->
-

## Validation

<!-- Commands actually run locally and their honest outcome. -->
- `git diff --check`
- `mvn -B -ntp -DskipTests validate`

## Risk

<!-- Reference docs/risk-register.md IDs (R-00X) impacted or introduced. -->
- Affected risks:
- New risks:
- Mitigation:

## Rollback

<!-- How to revert this PR safely if it breaks develop. -->
- Revert commit(s) via `git revert <sha>` and re-run validation.

## Linked tracker item

<!-- Descriptive slug from docs/dev-tracker.md; add (HBTV-XXX) when the entry
     has a legacy code. Use N/A when no tracker item applies. -->
- `tracker-slug` (HBTV-XXX)

## Checklist

- [ ] Branch was created from `develop`
- [ ] No unrelated source changes
- [ ] `git diff --check` passed
- [ ] `mvn -B -ntp -DskipTests validate` passed or baseline failure documented
- [ ] PR keeps linear history
- [ ] English used for branches, commits, comments, docs, and PR text
- [ ] Documentation updated (`docs/dev-tracker.md`, `docs/dev-tracker.json`, risk register, decision log as needed)
- [ ] No secrets, tokens, local paths, or build outputs committed

## Suggested squash merge commit (optional)

```text
<type>(optional-scope): short summary (#PR_NUMBER)

Short final summary of the merged change.

Includes:
- Main outcome.
- Validation or documentation update.
- Important compatibility note if needed.
```

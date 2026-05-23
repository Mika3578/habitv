<!-- Keep this PR small, scoped, and reviewable. Use English only. -->

## Summary

<!-- One or two sentences describing what this PR delivers and why. -->

## Changes

<!-- Bullet list of what this PR changes. -->
-

## Notes

<!-- Bullet list of intentional non-changes to avoid scope creep. -->
-

## Validation

<!-- Commands actually run locally and their honest outcome. -->
- `git status --short`
- `git diff --check`
- `mvn -B -ntp -DskipTests validate`

## Risk / rollback

<!-- Reference docs/risk-register.md IDs impacted/introduced and describe rollback. -->
- Risk:
- Rollback:

## Linked tracker item

<!-- Descriptive slug from docs/dev-tracker.md; add (HBTV-XXX) when the entry
     has a legacy code. Use N/A when no tracker item applies. -->
- `tracker-slug` (HBTV-XXX)

## Checklist

- [ ] Branch was created from `develop`
- [ ] Duplicate-prevention checks completed before branch creation (see `AGENTS.md`)
- [ ] No open PR already covered the same scope before opening this PR
- [ ] No unrelated source changes
- [ ] `git diff --check` passed
- [ ] `mvn -B -ntp -DskipTests validate` passed or baseline failure documented
- [ ] PR keeps linear history
- [ ] English used for branches, commits, comments, docs, and PR text
- [ ] Documentation updated (`docs/dev-tracker.md`, `docs/dev-tracker.json`, risk register, decision log as needed)
- [ ] If a `plugins/*/pom.xml` `<version>` is bumped, the trigger from `plugin-versioning-policy` is named in Summary/Scope (downloader/parser, user-facing endpoint, or user-facing configuration), and internal deps use `${project.parent.version}`
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

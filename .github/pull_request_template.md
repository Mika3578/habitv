<!-- Keep this PR small, scoped, and reviewable. Use English only. -->

## Summary

<!-- One or two sentences describing what this PR delivers and why. -->

## Scope

<!-- Bullet list of what this PR changes. -->
-

## Out of scope

<!-- Bullet list of intentional non-changes to avoid scope creep. -->
-

## Linked tracker item

<!-- HBTV-XXX from docs/dev-tracker.md (and matching docs/dev-tracker.json). -->
- HBTV-

## Validation commands

<!-- Commands actually run locally and their honest outcome. -->
- `git status --short`
- `git branch --show-current`
- `git log --oneline -5`
- `mvn -B -ntp -DskipTests validate`

## Risk assessment

<!-- Reference docs/risk-register.md IDs (R-00X) impacted or introduced. -->
- Affected risks:
- New risks:
- Mitigation:

## Rollback plan

<!-- How to revert this PR safely if it breaks master. -->
- Revert commit(s) via `git revert <sha>` and re-run validation.

## Checklists

- [ ] Linear history preserved (no merge commits in this branch)
- [ ] English used for branches, commits, comments, docs, and PR text
- [ ] Documentation updated (`docs/dev-tracker.md`, `docs/dev-tracker.json`, risk register, decision log as needed)
- [ ] No unrelated refactors, formatting, or dependency bumps
- [ ] No secrets, tokens, local paths, or build outputs committed

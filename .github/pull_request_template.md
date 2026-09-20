<!-- Keep this PR small, scoped, and reviewable. Use English only.
     Public git text stays short and generic (see AGENTS.md). -->

## Summary

<!-- One or two short sentences: outcome and why. See AGENTS.md Public git text. -->

## Scope

<!-- Short generic scope, e.g. tf1plus. -->
-

## Related issue

<!-- Optional: include a real GitHub issue number, e.g. #123. -->
- N/A

## Changes

<!-- Bullet list of what this PR changes. -->
-

## Validation

<!-- Commands actually run locally and their honest outcome. -->
- `git status --short`
- `git diff --check`
- `mvn -B -ntp -DskipTests validate` (required for code changes; optional for docs-only unless build files changed)

## Risk / rollback

- Risk:
- Rollback:

## Notes

<!-- Bullet list of intentional non-changes to avoid scope creep. -->
-

## Checklist

- [ ] Branch was created from `develop`
- [ ] Duplicate-prevention checks completed before branch creation (see `AGENTS.md`)
- [ ] No open PR already covered the same scope before opening this PR
- [ ] No unrelated source changes
- [ ] `git diff --check` passed
- [ ] `mvn -B -ntp -DskipTests validate` passed (or was N/A for docs-only PRs with no build file changes)
- [ ] PR keeps linear history
- [ ] English used for branches, commits, comments, docs, and PR text
- [ ] Docs updated if behavior or contributor workflow changed
- [ ] Public git text is short and generic (see AGENTS.md)
- [ ] If a `plugins/*/pom.xml` `<version>` is bumped, the trigger class is named (parser, endpoint, or configuration), and internal deps use `${project.parent.version}`
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

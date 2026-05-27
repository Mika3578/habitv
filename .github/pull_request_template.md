<!-- Keep this PR small, scoped, and reviewable. Use English only. -->

## Summary

<!-- One or two sentences describing what this PR delivers and why. -->

## Scope

<!-- Short descriptive scope, e.g. provider-youtube-ytdlp. -->
-

## Related issue

<!-- Optional: #123, or N/A with brief reason. -->
- N/A

## Changes

<!-- Bullet list of what this PR changes. -->
-

## Validation

<!-- Commands actually run, or justified N/A. Example for docs-only:
N/A — docs-only change; reviewed Markdown diff and ran git diff --check. -->
-

## Risk / rollback

<!-- Honest risk level and rollback steps. Example for docs-only:
Low — rules/documentation-only change. Rollback: revert this commit. -->
-

## Notes

<!-- Intentional non-changes, or N/A. -->
-

## Checklist

- [ ] Branch was created from `develop`
- [ ] Branch name uses an allowed prefix (`feat/`, `fix/`, `docs/`, `chore/`, `test/`, `refactor/`, `ci/`) — not `claude/**`, `cursor/**`, `ai/**`, `codex/**`, `wip/**`, `feature/**`, or other tool/session names (`AGENTS.md` branch naming)
- [ ] Duplicate-prevention checks run before **new PR branch** creation (optional for local WIP / existing branch — `AGENTS.md`)
- [ ] No open PR already covered the same scope before opening this PR
- [ ] Scope matches governance level (L0/L1/L2 in `AGENTS.md`)
- [ ] `git diff --check` passed
- [ ] `mvn -B -ntp -DskipTests validate` passed, or justified N/A in Validation
- [ ] PR keeps linear history
- [ ] English used for branches, commits, comments, docs, and PR text
- [ ] Documentation updated at PR readiness when required (tracker, risk, ADR per L1/L2)
- [ ] If a `plugins/*/pom.xml` `<version>` is bumped, the `plugin-versioning-policy` trigger is named and internal deps use `${project.parent.version}`
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

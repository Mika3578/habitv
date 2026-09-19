<!-- Keep this PR small, scoped, and reviewable. Use English only. -->

## Summary

<!-- Motivation and observable behavior change. -->

## Scope

<!-- Short concern name, e.g. catalog-parsing. -->
-

## Related issue

<!-- Optional: a real GitHub issue number, e.g. #123. -->
- N/A

## Changes

<!-- Implementation choices that matter for review. -->
-

## Validation

<!-- Commands actually run and their outcome. -->
- `git status --short`
- `git diff --check`
- `mvn -B -ntp -DskipTests validate` (required for code changes; optional for docs-only unless build files changed)

## Compatibility / limitations

<!-- User-config impact, Java 8, known gaps. -->
-

## Risk / rollback

- Risk:
- Rollback:

## Checklist

- [ ] Branch uses an allowed prefix from `develop` (`feat/`, `fix/`, `docs/`, `test/`, `refactor/`, `chore/`, `ci/`)
- [ ] No open PR already covered the same scope
- [ ] No unrelated source changes
- [ ] `git diff --check` passed
- [ ] `mvn -B -ntp -DskipTests validate` passed (or was N/A for docs-only)
- [ ] History is linear
- [ ] English used for branches, commits, comments, docs, and PR text
- [ ] Docs updated if behavior or contributor workflow changed
- [ ] If a `plugins/*/pom.xml` `<version>` is bumped, the trigger is named (parser, endpoint, or configuration) and internal deps use `${project.parent.version}`
- [ ] No secrets, tokens, local paths, or build outputs committed

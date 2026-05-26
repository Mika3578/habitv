# Pull request style guide

English-only policy applies to branches, commits, PR titles, PR bodies,
review comments, and squash merge metadata.

## PR title and description

- Use a clear summary in the PR title; Conventional Commit style is preferred
  for the eventual squash merge title.
- Keep the PR body focused on outcome, scope, validation, and risks.
- Reference one descriptive scope slug from `docs/dev-tracker.md` when
  applicable.
- Fill every required (non-optional) section of
  `.github/pull_request_template.md`.

## Branch and commit hygiene

- Create short-lived branches from `develop`.
- Keep feature-branch history linear (no merge commits on the branch).
- Use Conventional Commits for every commit on the branch.
- Prefer one logical change per PR; split unrelated work.

## Validation

Document commands actually run locally. Default safe validation:

```bash
git diff --check
mvn -B -ntp -DskipTests validate
```

Do not claim success for commands that were not run.

## Squash merge commit policy

When using GitHub **Squash and merge**, always review and rewrite the
generated commit message before confirming.

- Do not keep GitHub's default extended description if it only lists every
  commit from the PR.
- Do not keep noisy generated bullet lines such as:
  - `* ci: ...`
  - `* docs: ...`
- Do not keep `Co-authored-by` trailers unless co-authorship is intentional
  and should be preserved.
- Use one clean Conventional Commit title.
- Use a short body explaining the final merged change, not the PR's internal
  commit history.
- Keep the squash commit body concise and useful for future `git log` readers.
- Include the PR number in the squash title when merging through GitHub.

### Good squash title

```text
ci: add Maven PR validation workflow (#65)
```

### Good squash body

```text
Establish Java 8 Maven validation as the pull request CI baseline.

Includes:
- Required Java 8 validation, deterministic test, and package jobs.
- Non-blocking diagnostic jobs for newer Java versions.
- CI documentation for required checks and local command parity.
```

### Bad squash body

```text
* ci: add Maven PR validation workflow

Co-authored-by: Cursor <cursoragent@cursor.com>

* docs: document Maven CI checks

Co-authored-by: Cursor <cursoragent@cursor.com>
```

## Related documentation

- `CONTRIBUTING.md` — contributor workflow
- `docs/repository-maintenance.md` — merge metadata responsibilities
- `docs/repository-governance.md` — branch and merge model
- `.github/pull_request_template.md` — PR checklist and suggested squash block

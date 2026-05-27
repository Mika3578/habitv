# GitHub repository settings

Recommended GitHub UI settings for the Habitv repository. These
settings cannot be applied via this PR; the repository owner must
apply them manually under **Settings** on
`https://github.com/Mika3578/habitv`.

## Recommended branch model

- `master` — stable / release baseline.
- `develop` — integration branch for active modernization, created
  from `master` after the bootstrap PR is merged.
- Work branches branch from `develop` (after bootstrap) using:
  - `chore/...`
  - `ci/...`
  - `docs/...`
  - `test/...`
  - `fix/...`
  - `feat/...`
  - `refactor/...`
- Deprecated prefixes (`feature/...`, `build/...`, `runtime/...`,
  `provider/...`) are not used for new branches. Use the replacement
  mapping defined in `AGENTS.md`.

## Recommended default branch

- Keep `master` as the default branch until this bootstrap PR is
  merged.
- After bootstrap is merged, create `develop` from `master`.
- Optionally set `develop` as the default branch if the owner wants
  contributors to target the integration branch by default. `master`
  remains protected as the stable baseline either way.

## Recommended merge strategy for linear history

Under **Settings -> General -> Pull Requests**:

- [ ] Allow merge commits — **disabled**.
- [x] Allow squash merging — **enabled** (default merge strategy).
- [x] Allow rebase merging — enable only if the owner is comfortable
  resolving rebase conflicts.
- [x] Automatically delete head branches — **enabled**.
- [x] Always suggest updating pull request branches — **enabled**.

## Recommended branch protection for `master`

Under **Settings -> Branches -> Branch protection rules** for
`master`:

- [x] Require a pull request before merging.
  - [x] Require approvals (at least 1 from the owner).
  - [x] Dismiss stale approvals on new commits.
- [x] Require status checks to pass before merging.
- [x] Require branches to be up to date before merging (enable once
  CI is stable enough not to thrash).
- [x] Require linear history.
- [x] Do not allow force pushes.
- [x] Do not allow deletions.
- [x] Restrict who can push directly to matching branches (limit to
  the owner / maintainers if possible).

Apply the same rule set to `develop` once it is created.

## Live required checks for `develop`

The live `protect-develop` ruleset requires these exact status check contexts:

- `validate-java8`
- `deterministic-tests-java8`
- `compile-and-package-java8`
- `dependency-review`

The first three are Maven CI checks. `dependency-review` is already part of
the live required checks. The removed legacy check was `validate (zulu-8)` /
`CI / validate (zulu-8)`. Do not use workflow-prefixed names (for example
`Maven CI / validate-java8`) unless GitHub Settings later displays them that
way.

## Recommended initial required checks

Once the `build` workflow has run at least once on a PR, mark the
following check as required for `master`:

- `build / validate (windows, java8)`

This check name comes from `.github/workflows/build.yml` job name
`validate (windows, java8)`. The workflow is now Windows-only — the
Ubuntu Java 8 validation is covered by `Maven CI` (`ci-maven.yml`). If
the actual rendered check name differs in the GitHub UI, use the name as
displayed there.

## After bootstrap merge — recommended sequence

1. Merge this bootstrap PR into `master`.
2. Apply the branch model, merge strategy, and `master` branch
   protection above.
3. Create `develop` from `master` (`git switch -c develop master &&
   git push -u origin develop`) and apply the same branch
   protection.
4. Optionally set `develop` as the default branch.
5. Track this work under the `branch-protection` item in
   `docs/dev-tracker.md` (legacy code mapping remains historical only).

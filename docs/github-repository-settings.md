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

## Recommended initial required checks

Once the `build` workflow has run at least once on a PR, mark the
following checks as required for `master`:

- `build / validate (ubuntu-latest, java8)`
- `build / validate (windows-latest, java8)`

These check names come from `.github/workflows/build.yml` job name
`validate` with the matrix `os` values. If the actual rendered
check names differ in the GitHub UI, use the names as displayed
there.

## After bootstrap merge — recommended sequence

1. Merge this bootstrap PR into `master`.
2. Apply the branch model, merge strategy, and `master` branch
   protection above.
3. Create `develop` from `master` (`git switch -c develop master &&
   git push -u origin develop`) and apply the same branch
   protection.
4. Optionally set `develop` as the default branch.
5. Track this work as HBTV-003 in `docs/dev-tracker.md`.

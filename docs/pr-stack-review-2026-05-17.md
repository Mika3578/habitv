# PR stack review

## Current open PRs

| PR | Title | Source branch | Target branch | Status | Recommendation |
| --- | --- | --- | --- | --- | --- |
| #34 | build: standardize static repository workspace layout | `build/standard-cross-os-static-repo-layout` | `develop` | Open, clean, 3 commits, 6 files | Keep as deployment workflow PR; rebase on top of `develop` after `#33`, then revalidate deploy flow. |
| #33 | build: align own-version plugin internal dependencies | `fix/align-own-version-plugin-internal-deps` | `develop` | Open, clean, 1 commit, 4 files | Merge first (highest priority). It correctly switches internal `api/framework` resolution to parent/reactor version. |
| #32 | build: align JDT compliance with Java 8 | `fix/java8-jdt-compliance` | `develop` | Open, clean, 1 commit, 2 files | Keep this one and merge once build stack is stable, but only if #30 is closed as duplicate. |
| #31 | docs: record modernization status and next steps | `chore/modernization-status-next-steps` | `develop` | Open, clean, 1 commit, 2 files | Keep for last. Rebase after technical PRs (`#33`, `#34`, and chosen Java 8 PR) to avoid tracker conflicts. |
| #30 | build: align packaging modules with Java 8 baseline | `fix/java-8-compiler-baseline` | `develop` | Open, clean, 1 commit, 2 files | Close or supersede as duplicate of `#32` (identical patch). Do not merge both. |
| #29 | fix(youtube): externalize data api key and mask api errors | `fix/youtube-api-key-externalization` | `develop` | Open, clean, 2 commits, 14 files | Keep separate from Maven/static-repo work; merge after build/repository stabilization. |
| #25 | docs: plan legacy URL migration (HBTV-004) | `claude/analyze-audit-jrczu` | `master` | Open, clean, 3 commits, 39 files | Do not merge as-is. It is not docs-only (contains broad build/POM changes) and targets `master`; close as superseded and recreate a scoped docs-only PR on `develop` if needed. |

## Merge order

1. `#33` (highest technical priority, unblocks internal dependency resolution path).
2. `#34` rebased onto `develop` that already contains `#33`, then revalidated.
3. `#32` (or `#30`, but not both; recommendation: keep `#32`, close `#30`).
4. New follow-up PR: replace active legacy `dabiboo` Maven repository usage.
5. `#29` (kept separate from repository/build migration).
6. `#31` rebased and updated last as status documentation.
7. `#25` closed as superseded (or recreated as clean docs-only PR targeting `develop`).

## Duplicates / overlaps

- `#30` and `#32` are exact duplicates (same two files, same line changes in both packaging module POMs).
- `#31`, `#33`, `#34`, and `#29` all touch `docs/dev-tracker.md` and `docs/dev-tracker.json`; merge order and rebases are required to keep linear history clean.
- `#34` and `#33` both modify root `pom.xml`; `#34` should be rebased after `#33`.
- `#25` overlaps heavily with already-landed/ongoing build stabilization scope and carries mixed concerns despite docs title.

## Blockers

- `#33` must land first to remove the own-version internal dependency mismatch (`api/framework` version resolution for beinsport/own-version plugins).
- `#34` deploy validation is not trustworthy until rebased on top of `#33`.
- `#30` vs `#32` duplication must be resolved before merge to avoid duplicate commits and conflict churn.
- `#25` currently targets `master` and is oversized/mixed-scope for the current `develop` PR stack.

## Required follow-up PRs

- Replace/retire active legacy `http://dabiboo.free.fr/repository` usage in build configuration with the controlled static repository strategy.
- Recreate HBTV-004 docs migration content (from `#25`) as a clean docs-only PR targeting `develop`, if that content is still current after rebasing onto latest tracker/risk/decision docs.

## Validation

- Baseline `develop`: `mvn -B -ntp -DskipTests validate` -> **BUILD SUCCESS** (33-module reactor).
- Targeted check for `#33`: `mvn -B -ntp -DskipTests -pl plugins/beinsport -am compile` on PR head -> **BUILD SUCCESS** (reactor modules resolved; no `maven-default-http-blocker` for `framework/api:4.1.1-SNAPSHOT`).
- Additional note: full root `compile` on the `#33` branch failed earlier in `trayView` with a local classpath/class file access issue before reaching the previous beinsport blocker path; this did not invalidate the targeted dependency-resolution check above.

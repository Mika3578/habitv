# AGENTS.md

Instructions for AI coding agents (Codex, Cursor, Claude, Copilot, etc.)
working on the Habitv repository. These instructions complement, but do
not replace, the human review process.

## Repository context

Habitv is a Java 8 Maven multi-module application that downloads
French TV catch-up content via pluggable provider plugins. Top-level
layout (see `docs/audit-master-baseline.md` for details):

- `pom.xml` — root parent POM (no reactor `<modules>` yet).
- `fwk/` — `fwk/api`, `fwk/framework` (no aggregator `<modules>` yet).
- `application/` — aggregator of `core`, `consoleView`, `trayView`,
  `habiTv`.
- `plugins/` — aggregator of 22 plugin modules
  (`plugins/plugin-tester` is intentionally not in the aggregator).

The repository is in a controlled restart phase: most cleanup work
is staged via the tracker in `docs/dev-tracker.md`.

## Branching model

- `master` is the stable baseline and the protected default branch.
- `develop` will become the integration branch after the bootstrap
  PR is merged (see `docs/github-repository-settings.md`).
- Short-lived feature branches: `chore/...`, `build/...`, `ci/...`,
  `docs/...`, `test/...`, `runtime/...`, `provider/...`, `fix/...`,
  `feature/...`.
- Never base modernization branches on `develop` until the bootstrap
  PR is merged. The restart PR itself targets `master`.

## Commit policy

- Conventional Commits: `<type>(<scope>): <subject>`.
  Types: `feat`, `fix`, `refactor`, `perf`, `docs`, `test`, `chore`,
  `build`, `ci`, `style`, `revert`.
- Subject: imperative, lowercase, no trailing period, <= 72 chars.
- Body (when needed): explain the motivation and contrast with prior
  behavior. Wrap at ~72 chars.
- One logical change per commit. Split commits that need "and" in
  the subject.
- Never commit secrets, tokens, local paths, build outputs, or
  IDE files.

## PR policy

- One tracker item per PR. Reference it as `HBTV-XXX` in the body.
- Use `.github/pull_request_template.md` and fill every section.
- Keep diffs small and focused; no opportunistic refactors,
  formatting passes, or unrelated dependency bumps.
- Preserve linear history. No merge commits inside feature branches.
- Update documentation (`docs/dev-tracker.md`,
  `docs/dev-tracker.json`, `docs/risk-register.md`, and
  `docs/decision-log.md`) when the change is meaningful.

## Validation policy

Default validation command for the current phase:

```
mvn -B -ntp -DskipTests validate
```

Stronger commands (`compile`, `test`, `package`, `verify`) are
NOT default-safe yet because the reactor is incomplete (see
`docs/audit-master-baseline.md`). Use them only when a tracker
item explicitly asks for them, and document the exact command
in the PR body.

## Forbidden broad changes (in this restart phase)

Do not, without an explicit tracker item and ADR:

- Restructure Maven reactor or aggregator topology.
- Migrate Java baseline beyond Java 8.
- Migrate JavaFX (JDK-bundled `jfxrt`) to OpenJFX.
- Regenerate or replace JAXB-bound classes / move to `jakarta.*`.
- Replace `youtube-dl` with `yt-dlp` (HBTV-007).
- Change runtime updater URLs or layout (HBTV-005).
- Migrate FTP/HTTP repositories (HBTV-004).
- Remove or rename provider/plugin modules (HBTV-006).
- Add OWASP dependency-check, SBOM, or static-analysis plugins.

## How to update tracker / risk / decision docs

- `docs/dev-tracker.md` is the human-readable list of items. Each
  item must keep its fields: Status, Priority, Scope, Acceptance
  criteria, Validation, PR, Notes.
- `docs/dev-tracker.json` is the machine-readable mirror. Always
  update both in the same commit. Field names must match.
- `docs/risk-register.md` tracks risks `R-00X`. Add new risks with
  a description, likelihood/impact note, and mitigation plan.
- `docs/decision-log.md` uses ADR entries `ADR-00XX` with Status,
  Context, Decision, Consequences. Append-only; supersede rather
  than rewrite past decisions.

## How to handle failing tests

- Read the failing assertion AND the test before editing code.
- Do not edit a test only to make it pass; understand the contract.
- If the failure is environmental (network, missing tool, OS
  binary), do not retry blindly; record the limitation in the PR
  body and in `docs/risk-register.md` if novel.
- Never disable, delete, or `@Ignore` a test to ship green without
  documenting the reason in the PR and the tracker.

## How to report partial validation honestly

If the agent cannot run a required validation command (missing
Java, Maven, network, or external tool), it must:

1. State exactly which command could not be run and why.
2. Not claim success for that command.
3. Record the limitation in the PR body's "Validation results"
   section AND in `docs/audit-master-baseline.md` if it changes
   the picture of what is reproducible on a clean machine.
4. Propose the minimal next step needed to make that command
   runnable (e.g. install Temurin 8, restore network).

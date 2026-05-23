# GitHub Copilot instructions for Habitv

Habitv is a legacy Java 8 Maven multi-module application for downloading
French TV catch-up content via pluggable provider plugins. Modernization
is staged through tracker items in `docs/dev-tracker.md`.

Treat the codebase as production legacy: prefer conservative,
narrowly-scoped suggestions over rewrites.

## Workflow policy source of truth

Follow `AGENTS.md` as the source of truth for:
- branch naming
- duplicate branch and PR prevention
- commit style
- PR structure
- validation commands
- linear Git history
- documentation sync requirements

## Compatibility constraints

- Target Java 8 only. Do not suggest Java 9+ language features
  (e.g. `var`, records, switch expressions, sealed types, pattern
  matching), Java 9+ APIs (e.g. `List.of`, `Optional.orPrimitive`,
  `HttpClient`, `Stream.toList`), or module-system constructs.
- Preserve the existing Maven multi-module layout. Do not propose
  reactor restructures, parent POM rewrites, or aggregator merges
  unless an explicit tracker item is referenced in the prompt.
- Do not propose provider rewrites (e.g. canalPlus, arte, pluzz,
  6play, youtube). Provider changes go through dedicated tracker
  items HBTV-006 / HBTV-007 / future.

## Key modules

- `fwk/api` — public API contracts shared by core and plugins.
- `fwk/framework` — base utilities, retriever, updater helpers.
- `application/core` — config, DAOs, JAXB-bound entities, updater.
- `application/consoleView` — CLI front end.
- `application/trayView` — JavaFX 2.x tray UI.
- `application/habiTv` — application bundling entry point.
- `plugins/*` — provider, downloader, and tool wrapper plugins
  (e.g. youtube, ffmpeg, curl, RSS, canalPlus, arte, pluzz, 6play).
- `plugins/plugin-tester` — shared test harness for plugin modules.

## Default validation

Use this command as the default sanity check before opening a PR:

```
mvn -B -ntp -DskipTests validate
```

Do not invent richer commands (e.g. `verify`, `install`, `package`)
unless a tracker item explicitly requires them — the reactor is not
yet stabilized (see `docs/audit-master-baseline.md`).

## Things to avoid suggesting

- Replacing `javax.xml.bind` with `jakarta.xml.bind`.
- Replacing `youtube-dl` with `yt-dlp` (tracked under HBTV-007).
- Upgrading or replacing JavaFX dependencies (tracked under HBTV-008).
- Adding network-dependent tests to the default lifecycle.
- Adding OWASP, SBOM, or static-analysis plugins in this phase.
- Reformatting files, renaming variables outside a change, or moving
  packages.
- Bumping a `plugins/*/pom.xml` `<version>` for changes that do not
  match a `plugin-versioning-policy` trigger (downloader/parser
  behaviour, user-facing endpoint, user-facing configuration). See
  the "Plugin versioning" section of `CONTRIBUTING.md`. Inside a
  bumped plugin, internal reactor deps (`api`, `framework`,
  `plugin-tester`) MUST use `${project.parent.version}`, never
  `${project.version}`.

## Things to encourage

- Small, scoped diffs tied to one tracker item.
- English-only comments and identifiers in new code.
- Explicit error handling at meaningful boundaries (no swallow,
  no broad catch).
- Updating `docs/dev-tracker.md`, `docs/dev-tracker.json`,
  `docs/risk-register.md`, and `docs/decision-log.md` when the
  change is meaningful.

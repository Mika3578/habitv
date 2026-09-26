# Development

Authoritative Java, Maven, run, and CI notes. Other docs should link here
instead of restating compiler flags.

## Java baseline (current vs target)

Verified against: root `pom.xml` `maven-compiler-plugin` (`<source>1.8</source>`
`<target>1.8</target>`), no `maven.compiler.release`, no toolchains file,
required jobs in `.github/workflows/ci-maven.yml` (`java-version: "8"`,
Liberica `jdk+fx`), `.github/workflows/build.yml` (Temurin 8),
`.github/workflows/codeql.yml` (Liberica 8 `jdk+fx`). GUI modules still
use JavaFX 2.x / `jfxrt` on Java 8. Out-of-reactor
`habiTv-linux` / `habiTv-windows` pin `maven.compiler.source/target` 1.8
and `${jdk.home}`.

| | |
|--|--|
| **Current build / runtime baseline** | **Java 8** bytecode and required CI |
| **Active modernization target** | **Java 21** (not the merge baseline yet) |
| **Next target** | **Java 25** |
| `maven.compiler.release` | unset |

Required `develop` checks are `validate-java8`,
`deterministic-tests-java8`, `compile-and-package-java8`,
`dependency-review`. Jobs `compatibility-java17` / `21` (and 11/25 on
schedule or `workflow_dispatch`) are **diagnostic** (`continue-on-error`). Do not call them
supported runtimes.

Do not introduce Java 9+ language or APIs until a dedicated migration
changes the compiler and required CI.

GUI work needs a **JavaFX-capable JDK 8** (Liberica Full 8 or Zulu 8 with
FX). Plain Temurin 8 often has no JavaFX. JDK 11+ may compile via
`javafx-openjfx-compile`; that is not a runtime migration.

Prerequisites: JDK 8, Maven 3.6+, Git.
Default reactor: **35** modules. Out of reactor:
`application/habiTv-linux`, `application/habiTv-windows`.
`build/static-repo-publisher` is profile-only.

## Commands

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -DskipTests compile
mvn -B -ntp -DskipTests -pl "!application/trayView,!application/habiTv" package
mvn -B -ntp -pl <module> -am test
```

Default Surefire excludes live `*PluginManagerTest` and
`MessageReceiverTest`. Enable live provider tests only with
`-Plive-provider-tests`.

Console fat JAR:
`application/consoleView/target/consoleView-4.1.0-SNAPSHOT-all.jar`
(copy or rename to `habitv.jar` if you want the commands below).

## Run (console)

Copy the fat JAR and the plugin JARs you need into a runtime directory
with a `plugins/` folder. Place `configuration.xml` next to the JAR.

Unix `<cmdProcessor>`: `/bin/sh -c #CMD#`.
Windows: `cmd.exe /c #CMD#`.

```bash
java -jar habitv.jar -lp
java -jar habitv.jar "https://www.youtube.com/watch?v=jNQXAC9IVRw"
```

Disable plugin-JAR update checks: `-Dhabitv.update.enabled=false`.
Tool plugins may still run their own binary updater after that.
Update base (HTTPS):
`https://mika3578.github.io/habitv-repo/repository/`.

Local publish to a `habitv-repo` checkout uses Maven profiles
`static-repo-deploy` / `static-repo-publish` (`file://` only). Staging
path: `habitv.static.repo.path` (default
`${user.home}/dev/habitv-repo/repository`). Scripts live in
`scripts/static-repo/`; PowerShell wrappers expect `HABITV_REPO_DIR` to
point at the local `habitv-repo` checkout. This is maintainer publish
only — runtime clients still use the HTTPS URL above.

## CI

Workflow: `.github/workflows/ci-maven.yml`. Java version policy is in
the section above.

Ruleset JSON payloads (admin): `docs/github-rulesets/`.

## Agent instructions

| Layer | Role |
|-------|------|
| [`AGENTS.md`](../AGENTS.md) | Canonical persistent policy (single full constitution) |
| `docs/` | Technical reference (Java, providers, architecture); URLs and tool flags belong here, not in long source comments |
| Code comments | Concise *why*; see `AGENTS.md` **Engineering Baseline** |
| [`.agents/skills/`](../.agents/skills/) | Portable on-demand procedures |
| Tool adapters (`.cursor/`, `.continue/rules/`, `.github/copilot-instructions.md`) | Thin compatibility; point to `AGENTS.md` |
| `scripts/validate-agent-policy.*` + CI `agent-policy` | Deterministic policy layout checks |
| `docs/github-rulesets/` | Branch protection payloads (hard enforcement) |

Pull request orchestration: invariants in `AGENTS.md`; procedure in
[`.agents/skills/pr-review/SKILL.md`](../.agents/skills/pr-review/SKILL.md).
Live snapshots: `scripts/pr-gh-snapshot.sh` / `.ps1`.

**Other agents:** Claude Code, Gemini CLI, Jules, Junie, Cline, Roo, Windsurf,
Devin, and similar tools should read root `AGENTS.md` when supported. Portable
skills live under `.agents/skills/`. **Aider:** pass `AGENTS.md` explicitly
(for example `aider --read AGENTS.md`) rather than maintaining a separate
`.aider.conf.yml` in the repository.

**Cursor Cloud Agents — branch prefix:** In the Cursor Dashboard, open
[Cloud Agents defaults](https://cursor.com/dashboard/cloud-agents#my-defaults)
(or **Cursor Settings → Cloud Agents** in the desktop app). The **branch prefix**
is a single static string (empty falls back to `cursor/`). It cannot select
`feat/` vs `fix/` per task and does not read `AGENTS.md` before the platform
creates the initial branch. Do not set the prefix to one task type (for example
all `feat/`) to mimic HabiTV policy. For canonical `<type>/<scope>` branches,
use the strict workflow in
[`.agents/skills/git-workflow/SKILL.md`](../.agents/skills/git-workflow/SKILL.md)
(API with `workOnCurrentBranch` and a pre-created `startingRef`, or recovery
before first push). Optional helper:
[`scripts/launch-cloud-agent-strict.ps1`](../scripts/launch-cloud-agent-strict.ps1)
(`CURSOR_API_KEY` from local secrets only).

**GitHub enforcement gaps (documented):** `docs/github-rulesets/protect-develop.json`
sets `dismiss_stale_reviews_on_push` to `false`; prefer `true` in the hosted
ruleset so approvals of an older diff do not remain valid after new commits
(see [`github-rulesets/README.md`](github-rulesets/README.md)). Adding
`agent-policy` and `agent-policy (windows)` to required status checks is
recommended after agent-policy CI is stable on `develop`.

## Workflow

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) and [`../AGENTS.md`](../AGENTS.md)
(**Public git text**).
Do not bypass branch protection or required checks.

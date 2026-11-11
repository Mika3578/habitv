# Development

Authoritative Java, Maven, run, and CI notes. Other docs should link here
instead of restating compiler flags.

## Java baseline (current vs target)

Verified against: root `pom.xml` `maven-compiler-plugin`
(`<release>${maven.compiler.release}</release>` with `maven.compiler.release`
`21`), OpenJFX `${openjfx.version}` in dependency management, required jobs in
`.github/workflows/ci-maven.yml` (`java-version: "21"`, Temurin),
`.github/workflows/codeql.yml` (Temurin 21). GUI modules use OpenJFX Maven
dependencies on Java 21. Packaging modules `habiTv-linux` / `habiTv-windows` are
in the `application` reactor.

| | |
|--|--|
| **Current build / runtime baseline** | **Java 21** bytecode and required CI |
| **Next target** | **Java 25** |
| `maven.compiler.release` | `21` |

Required `develop` checks are `validate-java21`,
`deterministic-tests-java21`, `compile-and-package-java21`,
`dependency-review`. Jobs `compatibility-java*` (and broader sweeps on
schedule or `workflow_dispatch`) are **diagnostic** (`continue-on-error`).
JDKs below 21 cannot compile the reactor (`--release 21`).

Prerequisites: JDK 21, Maven 3.6+, Git.
Default reactor includes Linux and Windows packaging modules.
`build/static-repo-publisher` is profile-only.

## Commands

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -DskipTests compile
mvn -B -ntp -DskipTests package
mvn -B -ntp -pl <module> -am test
```

Default Surefire excludes live `*PluginManagerTest` and
`MessageReceiverTest`. Enable live provider tests only with
`-Plive-provider-tests`.

Console fat JAR:
`application/consoleView/target/consoleView-4.1.0-SNAPSHOT-all.jar`
(copy or rename to `habitv.jar` if you want the commands below).

Native installers (opt-in profiles):
`mvn -B -ntp -P linux-jpackage package` or `-P windows-jpackage package`.

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

## Editor (Cursor / VS Code)

Shared workspace files live under `.vscode/` (`settings.json`,
`extensions.json`, `java-formatter.xml`, `java.prefs`) plus a root
`.editorconfig`.

- Install the workspace recommended extensions when prompted.
- Keep the project compile runtime on **JDK 21** (set
  `java.configuration.runtimes` in your **user** settings to your local
  JDK 21).
- After POM changes, use **Java: Reload Projects** if the Problems view
  looks stale.

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

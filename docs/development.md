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

## Workflow

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) and [`../AGENTS.md`](../AGENTS.md)
(**Public git text**).
Do not bypass branch protection or required checks.

# Modernization

Durable direction only. Actionable work lives in GitHub issues and PRs.

## Direction

1. Keep the existing Maven application working on the **current Java 8**
   baseline ([`development.md`](development.md)).
2. Restore providers incrementally with offline fixtures, then live checks.
3. Treat the console fat JAR as the supported baseline; JavaFX packaging
   later.
4. Finish yt-dlp as the YouTube-family tool plugin (binary publish, Windows
   diagnostics) without mixing unrelated provider rewrites.
5. Runtime move: **Java 21 + OpenJFX 21.0.7** — implemented in PR
   [#237](https://github.com/Mika3578/habitv/pull/237) (Draft): compiler
   baseline `release 21`, OpenJFX as explicit Maven dependencies, packaging
   modules back in the reactor. Required CI alignment, then merge.
   Diagnostic CI on 11/17/21/25 is not runtime support.

## Scope

Personal-use recording of content the user can access with their own
accounts. Provider access paths are maintainer decisions documented per
provider ([providers.md](providers.md)). Nothing identifying the user goes
into git: credentials, device files, and personal configuration stay local.

## Do not mix into random PRs

Java baseline bump, JavaFX/OpenJFX migration, JAXB `jakarta.*`, reactor
topology, updater URL changes, batch dependency majors. Run each as a
dedicated single-concern migration (it may span the modules that concern
requires). Until JAXB and JavaFX migrations land, compatibility CI on
11/17/21/25 stays diagnostic (`continue-on-error`); use it for signal, not as
a required merge gate. After those migrations and CI alignment, the same jobs
become the required migration gate.

## Packaging

After the runtime move lands:

- `jpackage` native installers per OS — Windows MSI, macOS DMG (11+),
  Linux DEB/RPM (GTK3) — each built on its target CI runner (Windows,
  macOS, Linux runners already exist in CI).
- Runtime image via `jlink` with the matching-platform JavaFX jmods.
- Docker image for the console fat JAR (headless usage).
- Supersedes the JavaFX 2.x self-contained packaging.

## Known leftovers

- Email plugin tests and sample `config.xml` still contain illustrative
  credentials — do not reuse; dedicated cleanup later.
- `application/habiTv-linux` / `habiTv-windows` are out of reactor and
  still assume `${jdk.home}` + JavaFX 2.x.
- Shade duplicate-class warnings on fat JARs (JAXB/Activation overlap)
  are a known packaging smell, not a docs task.
- Snyk findings on the migration PR and required-checks alignment to
  Java 21 (see PR #237).


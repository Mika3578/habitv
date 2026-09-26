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
5. Later runtime move: **Java 21** (active target), then **Java 25**.
   Diagnostic CI on 11/17/21/25 is not runtime support.

## Out of scope

Circumvention of technical protection, encryption, subscription gates, or
license restrictions. Never implement that as a dedicated PR goal.

## Do not mix into random PRs

Java baseline bump, JavaFX/OpenJFX migration, JAXB `jakarta.*`, reactor
topology, updater URL changes, batch dependency majors.

## Known leftovers

- Email plugin tests and sample `config.xml` still contain illustrative
  credentials — do not reuse; dedicated cleanup later.
- `application/habiTv-linux` / `habiTv-windows` are out of reactor and
  still assume `${jdk.home}` + JavaFX 2.x.
- Shade duplicate-class warnings on fat JARs (JAXB/Activation overlap)
  are a known packaging smell, not a docs task.

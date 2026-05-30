# JAXB and Activation dependency deduplication plan

**Date:** 2026-05-29  
**Status:** Planning only — **no POM, exclusion, version, or Shade changes** in this document  
**Prerequisite:** PR [#135](https://github.com/Mika3578/habitv/pull/135) merged and `develop` CI green  
**Evidence base:** [`docs/shade-duplicates-audit.md`](shade-duplicates-audit.md) (PR #134, `develop` @ `69bad437`)  
**Related risk:** `jaxb-mismatch` in [`docs/risk-register.md`](risk-register.md)  
**Related ADR:** `keep-java8-baseline` (no Jakarta namespace migration in this phase)

## Problem statement

Maven Shade builds for `application/consoleView` and `application/habiTv` merge a
transitive graph that contains **overlapping JAXB API** and **Activation**
artifacts. Shade reports hundreds of duplicate-class warnings and keeps **one
arbitrary winner** per classpath entry inside the uber JAR.

That behavior is acceptable for duplicate `META-INF/LICENSE` files but is
**runtime-sensitive** for:

- User XML configuration load/save (`XMLUserConfig`, grab config JAXB models)
- XJC-generated types under `application/core`
- Mail/MIME paths via `javax.mail:mail:1.4.7` and the `plugins/email` module
- Future Java 17/21/25 diagnostic runs (module-path and `ServiceLoader` discovery)

Habitv must **choose a single coherent JAXB API + Activation stack** before any
POM edit. Blind removal of `jaxb-api`, exclusions on `jaxb-runtime`, or
`activation:1.1` downgrades can change which `javax.xml.bind` / `javax.activation`
bytecode ships in the fat JAR without failing compile.

## Current duplicate artifacts

| Artifact | Version | Role |
|----------|---------|------|
| `javax.xml.bind:jaxb-api` | 2.3.1 | Explicit API (root BOM + `application/core`) |
| `jakarta.xml.bind:jakarta.xml.bind-api` | 2.3.3 | Transitive API from `jaxb-runtime` (still `javax.*` packages at 2.3.x) |
| `org.glassfish.jaxb:jaxb-runtime` | 2.3.9 | Explicit implementation (root BOM + `application/core`) |
| `javax.activation:activation` | 1.1 | Transitive from `javax.mail:mail:1.4.7` |
| `javax.activation:javax.activation-api` | 1.2.0 | Transitive from `jaxb-api:2.3.1` |
| `com.sun.activation:jakarta.activation` | 1.2.2 | Transitive from `jaxb-runtime:2.3.9` (runtime scope; still shaded) |

Shade overlap (representative):

- `jakarta.xml.bind-api-2.3.3.jar` + `jaxb-api-2.3.1.jar` → **114** overlapping classes/resources
- `activation-1.1.jar` + `jakarta.activation-1.2.2.jar` + `javax.activation-api-1.2.0.jar` → **13–27** overlapping classes depending on pair

## Dependency paths (from shade audit)

Copied from [`docs/shade-duplicates-audit.md`](shade-duplicates-audit.md) §3–4; re-verify with `dependency:tree` on the implementation branch.

### JAXB

```
application/core
├── javax.xml.bind:jaxb-api:2.3.1:compile          (explicit in core + root dependencyManagement)
│   └── javax.activation:javax.activation-api:1.2.0
└── org.glassfish.jaxb:jaxb-runtime:2.3.9:compile  (explicit in core + root dependencyManagement)
    ├── jakarta.xml.bind:jakarta.xml.bind-api:2.3.3:compile   ← duplicates jaxb-api API surface
    ├── org.glassfish.jaxb:txw2:2.3.9
    ├── com.sun.istack:istack-commons-runtime:3.0.12
    └── com.sun.activation:jakarta.activation:1.2.2:runtime
```

POM anchors (read-only reference): root `pom.xml` `dependencyManagement`, `application/core/pom.xml` compile dependencies.

### Activation

| Artifact | Path |
|----------|------|
| `javax.activation:activation:1.1` | `fwk/framework` → `javax.mail:mail:1.4.7` (BOM at root `pom.xml`) |
| `javax.activation:javax.activation-api:1.2.0` | `jaxb-api:2.3.1` |
| `com.sun.activation:jakarta.activation:1.2.2` | `jaxb-runtime:2.3.9` |

Overlapping implementation packages include `com.sun.activation.registries.*`, `javax.activation.*`, and defaults such as `META-INF/mailcap.default`.

### Modules affected by Shade merge

| Module | Shade output | Notes |
|--------|--------------|-------|
| `application/consoleView` | Attached `*-all.jar` | First fat JAR; JAXB/Activation warnings here |
| `application/habiTv` | Replaces main artifact | Second Shade pass re-merges full graph |

## Runtime risks

| Area | Risk if wrong artifact wins | Severity |
|------|-----------------------------|----------|
| XML user config | `ContextFinder` / provider picks wrong implementation; subtle marshal/unmarshal drift | High |
| Grab/config JAXB models | Same as above for generated + hand-maintained bindings | High |
| Email plugin / MIME | Wrong `javax.activation` implementation; `mailcap` / handler registry conflicts | Medium |
| JDK 9+ / 17 / 21 diagnostics | Classpath vs module-path; duplicate `module-info` neighbors (separate audit item) | Medium (grows on migration) |
| CI “green” with duplicates | `verify` succeeds with warnings only — no proof of runtime correctness | Process |

Tied to open risk `jaxb-mismatch`: generation uses legacy `maven-jaxb-plugin` / XJC while runtime ships a **merged** Glassfish stack.

## Java 8 constraints

Per `keep-java8-baseline` and `AGENTS.md` Section 2:

- **Do not** bump Java baseline or introduce Java 9+ APIs in dedup PRs.
- **Do not** migrate to `jakarta.xml.bind` 3.x / `jakarta.activation` 2.x namespace (true Jakarta EE 9+) in the short-term dedup lane.
- Accept that `jakarta.xml.bind-api` **2.3.x** and `jaxb-api` **2.3.x** both expose **`javax.xml.bind`** packages — coordinate rename is historical, not EE 9 migration.
- Validate on **Temurin 8** first; treat Java 17/21 CI jobs as **compatibility signals**, not substitutes for Java 8 runtime proof.
- XJC generation in `application/core` must remain reproducible; do not regenerate JAXB sources in the first dedup PR unless scoped separately.

## Java 21/25 modernization constraints

Dedup choices here **constrain** later migration PRs:

| Topic | Implication |
|-------|-------------|
| Single API artifact | Prefer one API coordinate + one runtime before moving to JDK 11+ module path |
| `javax.mail:mail:1.4.7` | Legacy; Activation 1.1 is a drag on modern stacks — mail upgrade is a **separate** tracker/ADR, not bundled into JAXB-only dedup |
| Glassfish 2.3.x line | Compatible with Java 8; Java 21+ may need explicit `--add-opens` / module-path ADR (`jaxb-mismatch` follow-up) |
| Strategy C (defer) | Leaves duplicate winners in uber JAR until Java 21/25 migration — acceptable only with documented acceptance of arbitrary Shade resolution |

Any future `jakarta.*` 3.x migration requires a **dedicated** tracker item and ADR; this plan does not authorize it.

## Candidate cleanup strategies

### JAXB API — Strategy A

**Keep** `javax.xml.bind:jaxb-api:2.3.1`; **exclude** `jakarta.xml.bind-api` from `jaxb-runtime` (Maven exclusion on the `jaxb-runtime` dependency in BOM/core).

| | |
|---|---|
| **Pros** | Keeps explicit legacy `javax.xml.bind` API coordinate; matches mental model and existing docs referencing `jaxb-api`; BOM version stays the single API pin. |
| **Cons** | Must verify Glassfish `jaxb-runtime:2.3.9` works when its transitive API is excluded; wrong exclusion breaks compile or runtime provider wiring. |
| **Validation focus** | Full core XML tests; both Shade modules; `dependency:tree` shows exactly one API jar. |

### JAXB API — Strategy B

**Remove** explicit `jaxb-api:2.3.1`; rely on `jaxb-runtime` → `jakarta.xml.bind-api:2.3.3` only.

| | |
|---|---|
| **Pros** | Fewer explicit dependencies; aligns runtime with what Glassfish publishes. |
| **Cons** | Jakarta coordinate name confuses reviewers (still `javax.*` at 2.3.x); API version becomes transitive (2.3.3 vs former explicit 2.3.1) — need compatibility proof; BOM must drop duplicate management entry. |
| **Validation focus** | Same as A; diff `dependency:tree` before/after; confirm no other module re-adds `jaxb-api`. |

### JAXB API — Strategy C

**Defer** JAXB/Activation cleanup until Java 21/25 migration program.

| | |
|---|---|
| **Pros** | Lowest short-term runtime regression risk; avoids POM churn during stabilization. |
| **Cons** | Shade duplicate warnings remain; arbitrary duplicate winner in `consoleView-all` and `habiTv` uber JARs; technical debt compounds into migration. |
| **Validation focus** | Document acceptance in ADR; re-run shade audit after migration branch merges. |

### Recommended decision process (implementation phase)

1. Capture `mvn dependency:tree -pl application/core -Dverbose` and Shade warning grep **before** change.
2. Implement **one** of A or B in `fix/shade-jaxb-api-dedup` (not both).
3. Run validation matrix below on Java 8.
4. Record outcome in `docs/decision-log.md` (accept or reject chosen strategy).
5. Only then open `fix/shade-activation-dedup`.

**Working recommendation for implementation PR:** prefer **Strategy A** if exclusion is confirmed safe on Java 8 (explicit API pin preserved); otherwise **Strategy B** if Glassfish documents API-only-via-transitive for 2.3.9. **Strategy C** only if migration timeline is imminent and product owner accepts duplicate uber-JAR risk.

### Activation — investigation notes

- **Root of `activation:1.1`:** `javax.mail:mail:1.4.7` via `fwk/framework` (and `plugins/email`).
- **Do not** upgrade `mail` or exclude `activation:1.1` without email/MIME regression tests.
- **Goal:** one Activation implementation path in the shaded JAR (likely `jakarta.activation:1.2.2` aligned with `jaxb-runtime`, or `javax.activation-api` if JAXB strategy demands it — decide **after** JAXB API dedup stabilizes tests).
- Possible later actions (each needs its own PR + matrix row):
  - Exclude `activation:1.1` from `mail` once `jakarta.activation` supplies handlers on Java 8.
  - Upgrade `mail` to a Jakarta-compatible line (separate ADR; not combined with first JAXB PR).

## Validation matrix

Run on the **implementation** branch after POM changes. Commands are the contract for reviewers.

| # | Check | Command / test | Java 8 required | Java 17/21 diagnostic |
|---|--------|----------------|-----------------|------------------------|
| 1 | Core unit/integration | `mvn -B -ntp -pl application/core -am test` | Yes | Repeat if JAXB classpath changes |
| 2 | Console fat JAR | `mvn -B -ntp -pl application/consoleView -am package` | Yes | Optional |
| 3 | Launcher uber JAR | `mvn -B -ntp -pl application/habiTv -am package` | Yes | Optional |
| 4 | Reactor verify | `mvn -B -ntp -DskipTests verify` | Yes | CI already runs 17/21 |
| 5 | XML config load/save | `XMLUserConfigTest`, grab config tests in `application/core` | Yes | — |
| 6 | JAXB generated models | Build with XJC; smoke unmarshal/marshal user config XSD paths | Yes | — |
| 7 | Email/MIME plugin | Offline tests in `plugins/email` if present; manual SMTP/MIME smoke if auth configured | Yes | — |
| 8 | Dependency proof | `mvn -B -ntp dependency:tree -pl application/core -Dverbose` + Shade log grep for `jaxb-api` / `jakarta.xml.bind-api` / `activation` | Yes | — |
| 9 | Shade warning delta | Compare Shade duplicate lines vs baseline in `shade-duplicates-audit.md` | Yes | — |

**Manual application tests (developer):**

- Start console or tray launcher from shaded JAR; load existing user XML config; save and reload.
- Trigger a flow that uses email notification plugin if configured in local environment.

**Failure criteria:** Any test failure, new Shade *class* duplicate between API and runtime after dedup, or XML round-trip mismatch → revert POM change and document in decision log.

## Follow-up PR sequence

| Order | Branch (suggested) | Scope | Depends on |
|-------|-------------------|--------|------------|
| 1 | `docs/jaxb-activation-dedup-plan` | This plan only | PR #134 audit, PR #135 merged |
| 2 | `fix/shade-jaxb-api-dedup` | Strategy A or B; BOM + `application/core`; decision log update | Plan merged |
| 3 | `fix/shade-activation-dedup` | Single Activation stack; mail path analysis | (2) green on Java 8 |
| 4 | `refactor/habitv-shade-layout` | Second Shade pass / `consoleView-all` structure (audit §6) | (2)–(3) or parallel if independent |
| 5 | `build/shade-meta-filters` | `module-info`, license transformers | Optional after runtime stable |

Do **not** combine (2) and (3) in one PR. Do **not** change Shade plugin configuration in (2) or (3) unless a separate scoped PR is approved.

## Out of scope for implementation PRs (unless new tracker + ADR)

- Jakarta EE 9+ namespace migration (`jakarta.xml.bind` 3.x)
- Regenerating all JAXB sources
- Bumping Java baseline beyond 8
- `javax.mail` major upgrade
- Global Shade warning suppression

## References

- [`docs/shade-duplicates-audit.md`](shade-duplicates-audit.md)
- [`docs/risk-register.md`](risk-register.md) — `jaxb-mismatch`
- [`docs/decision-log.md`](decision-log.md) — `keep-java8-baseline`, proposed `jaxb-activation-dedup-defer`
- [`docs/java-runtime-policy.md`](java-runtime-policy.md)
- Root `pom.xml`, `application/core/pom.xml`, `fwk/framework/pom.xml`
- `AGENTS.md` Section 2 (no JAXB regeneration / no Jakarta migration without tracker)

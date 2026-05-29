# Maven Shade duplicate warnings audit

**Date:** 2026-05-29  
**Branch:** `build/audit-shade-duplicates` (audit-only; no POM changes)  
**Evidence:** `mvn -B -ntp -DskipTests verify` and `mvn -B -ntp dependency:tree -Ddetail=true` on `develop` @ `f4dfe74e` (after PR #133 merge)

## Executive summary

Habitv uses **maven-shade-plugin 3.6.2** in two application modules. Shade emits a large
warning set during `verify`. Most **META-INF license/notice** overlaps are harmless.
**JAXB API** and **Activation** overlaps are the highest runtime risk: multiple JARs
ship the same `javax.*` types and Glassfish/Jakarta implementation classes into the
same uber JAR. Shade keeps **one arbitrary winner** per duplicate path.

This document classifies warnings and proposes **narrow follow-up PRs**. It does **not**
recommend blind dependency removal, Jakarta migration, or log suppression.

## Modules that run Shade

| Module | POM | Execution | Output |
|--------|-----|-----------|--------|
| `application/consoleView` | `application/consoleView/pom.xml` | `shade` goal, id `fatjar`, phase `package` | Main JAR stays thin; **attached** `consoleView-*-all.jar` (`shadedArtifactAttached=true`) |
| `application/habiTv` | `application/habiTv/pom.xml` | `shade` goal, default execution, phase `package` | **Replaces** main artifact with uber JAR; writes `dependency-reduced-pom.xml` locally |

No other reactor modules configure `maven-shade-plugin` (repo grep, 2026-05-29).

### Shade configuration notes

- **consoleView** uses `ServicesResourceTransformer`, signature filters for `META-INF/*.SF|DSA|RSA`, and `createDependencyReducedPom=false`.
- **habiTv** only sets `ManifestResourceTransformer` (main class `com.dabi.habitv.HabitvLauncher`). No `ServicesResourceTransformer`, no `module-info` filters, no license transformers.
- **habiTv** depends on `consoleView` and `trayView` as normal compile JARs (not the `all` classifier). The second Shade pass still merges the **full transitive graph** (core, framework, JAXB, Jackson, etc.), which amplifies overlap warnings versus consoleView alone.

## Warning families (from build log)

### 1. `module-info.class` (JPMS)

```
[WARNING] Discovered module-info.class. Shading will break its strong encapsulation.
```

- **Count:** 7 warnings per Shade execution (~14 total per full reactor `verify`).
- **Typical sources:** Jackson 2.21.x, Commons CLI/Codec multi-release JARs (`META-INF.versions.9.module-info` also reported).
- **Runtime risk:** **Low on Java 8** (classpath, not module path). Relevant for future Java 11+ / module-path launches (`jaxb-mismatch` risk register).
- **Safe follow-up:** Optional Shade filter to exclude `module-info.class` / `META-INF/versions/**/module-info.class` **after** validating Java 8 and any modular test matrix — not in this audit PR.

### 2. Duplicate META-INF metadata (low risk)

| Overlap | Example JARs | Notes |
|---------|----------------|-------|
| `META-INF/LICENSE`, `META-INF/NOTICE` | Jackson, reload4j | License text only |
| `META-INF/LICENSE.txt`, `META-INF/NOTICE.txt` | activation, commons-*, jaxb-api, mail, jdom | Same |
| `META-INF/LICENSE.md`, `META-INF/NOTICE.md` | istack, jakarta.activation, jakarta.xml.bind-api, jaxb-runtime, txw2 | Same |
| `META-INF/MANIFEST.MF` | Many artifacts in consoleView fat JAR set | Expected when merging JARs |
| `META-INF/maven/**/pom.xml` (+ `.properties`) | Per-artifact Maven metadata | Harmless; cosmetic in uber JAR |

**Runtime risk:** **Negligible** unless a tool scans merged manifests/licenses at runtime.

### 3. JAXB API duplicate (`javax` vs `jakarta.xml.bind-api`)

```
jakarta.xml.bind-api-2.3.3.jar, jaxb-api-2.3.1.jar define 114 overlapping classes and resources
```

Sample listed types: `javax.xml.bind.Binder`, `ContextFinder`, `DatatypeConverter`, …

**Dependency paths (core module, representative):**

```
core
├── javax.xml.bind:jaxb-api:2.3.1:compile          (explicit in core + BOM)
│   └── javax.activation:javax.activation-api:1.2.0
└── org.glassfish.jaxb:jaxb-runtime:2.3.9:compile (explicit in core + BOM)
    ├── jakarta.xml.bind:jakarta.xml.bind-api:2.3.3:compile   ← duplicates jaxb-api API surface
    ├── org.glassfish.jaxb:txw2:2.3.9
    ├── com.sun.istack:istack-commons-runtime:3.0.12
    └── com.sun.activation:jakarta.activation:1.2.2:runtime
```

**Root cause:** Habitv declares **both** the legacy `jaxb-api` artifact and `jaxb-runtime`, while `jaxb-runtime` already pulls `jakarta.xml.bind-api` (javax-compatible API repackaged under Jakarta coordinates for 2.3.x).

**Runtime risk:** **Medium–high** for XML binding: wrong `ContextFinder` / provider discovery, subtle unmarshalling bugs, or divergent `javax.xml.bind` bytecode if versions drift. Tied to risk `jaxb-mismatch` in `docs/risk-register.md`.

**Not recommended in audit PR:** Removing `jaxb-api` or `jaxb-runtime` without a dedicated tracker item, ADR touchpoint, and full `mvn test` / config XML regression.

**Planning doc (no POM changes):** [`docs/jaxb-activation-dedup-plan.md`](jaxb-activation-dedup-plan.md) — strategies A/B/C, Activation notes, validation matrix, and PR sequence before any dedup implementation.

### 4. Activation duplicate (three stacks)

```
activation-1.1.jar, jakarta.activation-1.2.2.jar define 13 overlapping classes and resources
activation-1.1.jar, jakarta.activation-1.2.2.jar, javax.activation-api-1.2.0.jar define 27 overlapping classes
jakarta.activation-1.2.2.jar, javax.activation-api-1.2.0.jar define 4 overlapping classes
```

**Dependency paths:**

| Artifact | Path |
|----------|------|
| `javax.activation:activation:1.1` | `framework` → `javax.mail:mail:1.4.7` (BOM `mail` 1.4.7 at root) |
| `javax.activation:javax.activation-api:1.2.0` | `jaxb-api:2.3.1` |
| `com.sun.activation:jakarta.activation:1.2.2` | `jaxb-runtime:2.3.9` (runtime scope; still shaded into uber JAR) |

Overlapping implementation packages include `com.sun.activation.registries.*`, `javax.activation.*`, and defaults such as `META-INF/mailcap.default`.

**Runtime risk:** **Medium** for mail/JAXB/attachment code paths (email plugin, MIME handlers). Less visible on Java 8 until activation is used.

### 5. Multi-release / versioned classes

```
commons-cli-*.jar, commons-codec-*.jar, jackson-*.jar define 1 overlapping classes:
  - META-INF.versions.9.module-info
```

**Runtime risk:** **Low on Java 8**; Shade merges versioned entries unpredictably if ever run on newer JDKs without filters.

### 6. habiTv-only: “project JAR vs dependency” noise

During `habiTv` Shade, many lines pair `habiTv-4.1.0-SNAPSHOT.jar` with third-party JARs (e.g. `jackson-core`, `jaxb-runtime`, `guava`, `consoleView`, `trayView`). That pattern reflects **re-merging a large dependency set** into a second uber JAR while also depending on sibling modules whose **transitive** libraries are listed again as separate JARs. Structural follow-up (dependency scope, classifier `consoleView-all`, or not shading twice) should be analyzed before treating each overlap as a new duplicate library.

**Runtime risk:** **Medium** for duplicate classes already shaded into nested paths; **investigation PR** before exclusions.

## consoleView vs habiTv warning volume

| Module | Shade step (log) | Dominant themes |
|--------|------------------|-----------------|
| `consoleView` | `shade:3.6.2:shade (fatjar)` | module-info; JAXB/Activation; META-INF licenses |
| `habiTv` | `shade:3.6.2:shade (default)` | All of the above **plus** hundreds of overlaps involving `habiTv-4.1.0-SNAPSHOT.jar` vs every merged dependency and vs `consoleView` / `trayView` module JARs |

## What is safe to ignore (for now)

- Duplicate **LICENSE / NOTICE / MANIFEST.MF** resources (documented above).
- **`module-info.class`** warnings when the shipped runtime remains **Java 8 classpath-only**.
- **`META-INF/maven/**`** duplication.

Do **not** globally silence Shade warnings with `-q`, log filters, or blanket `<filters>` without per-artifact justification.

## What needs real cleanup (prioritized)

| Priority | Issue | Suggested scope |
|----------|--------|-----------------|
| P1 | `jaxb-api` + `jakarta.xml.bind-api` both on classpath | `fix/shade-jaxb-api-dedup` — dependency convergence on `core`/BOM; validate JAXB generation + config XML tests; link `jaxb-mismatch` |
| P1 | Triple Activation (`1.1`, `javax.activation-api`, `jakarta.activation`) | `fix/shade-activation-dedup` — mail vs JAXB alignment; may require `mail` upgrade or exclusions **with** runtime mail tests |
| P2 | habiTv second uber-JAR strategy | `refactor/habitv-shade-layout` — evaluate thin `consoleView` + single habiTv shade, or `consoleView-all` + reduced habiTv merge; avoid double-counting |
| P3 | Shade hygiene | `build/shade-meta-filters` — optional excludes for `module-info.class`, license transformers (`ApacheLicenseResourceTransformer` / `DontIncludeResourceTransformer`) |
| P3 | habiTv parity | Add `ServicesResourceTransformer` to habiTv if SPI providers are required in the launcher JAR |

## Proposed follow-up PRs (safe sequencing)

0. **`docs/jaxb-activation-dedup-plan`** — Planning-only PR: compare strategies, validation matrix, sequencing ([`jaxb-activation-dedup-plan.md`](jaxb-activation-dedup-plan.md)).
1. **`fix/shade-jaxb-api-dedup`** — Implement Strategy A or B from the plan; run validation matrix; document decision in `docs/decision-log.md` if behavior changes.
2. **`fix/shade-activation-dedup`** — Map `javax.mail` usage; exclude `activation:1.1` or align `mail` only after email plugin / MIME tests (see plan Activation notes).
3. **`refactor/habitv-shade-layout`** — Design doc + minimal POM change for single fat JAR boundary; re-run Shade grep from this audit.
4. **`build/shade-meta-filters`** — Metadata/module-info filters only after (1)–(3) stabilize runtime tests.

## Validation commands (audit capture)

```bash
mvn -B -ntp -DskipTests verify
# log: agent_space/habitv-before-shade-warnings.log (local; gitignored)

mvn -B -ntp dependency:tree -Ddetail=true
# log: agent_space/habitv-dependency-tree.log (local; gitignored)

mvn -B -ntp dependency:tree -pl application/core
```

**Result (2026-05-29):** `verify` **BUILD SUCCESS** (warnings only; no Shade failure).

## References

- Shade plugin: https://maven.apache.org/plugins/maven-shade-plugin/
- POMs: `application/consoleView/pom.xml`, `application/habiTv/pom.xml`, `application/core/pom.xml`, root `pom.xml` (JAXB + `mail` BOM)
- Plan: [`docs/jaxb-activation-dedup-plan.md`](jaxb-activation-dedup-plan.md)
- Risk: `jaxb-mismatch` in `docs/risk-register.md`
- Policy: Java 8 / no Jakarta migration in `AGENTS.md` Section 2

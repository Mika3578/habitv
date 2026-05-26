# Java and JavaFX runtime policy

This document is the canonical description of Habitv's **supported Java
baseline**, **JavaFX expectations**, and **planned runtime migration**. It
supersedes informal README wording where they conflict.

**Related:** [`keep-java8-baseline`](decision-log.md) (ADR),
[`javafx-modernization`](dev-tracker.md), risk [`javafx-jdk8`](risk-register.md).

---

## Current status

| Aspect | Policy |
|--------|--------|
| **Supported runtime baseline** | **Java 8** only |
| **Compiler `source` / `target`** | **1.8** across the in-reactor modules |
| **End-user GUI runtime** | JDK/JRE 8 with a **JavaFX-capable** distribution (see below) |
| **JDK 11+ on developer/CI machines** | Allowed for **build** (`validate`, `compile`, and scoped `package`) via the `javafx-openjfx-compile` Maven profile; **not** a supported end-user runtime yet |
| **Runtime migration to Java 11/17/21** | **Not complete** — documentation and CI diagnostics only where noted |

---

## Java 8 baseline

Habitv remains a **Java 8 application** for this modernization phase:

- No Java 9+ language features or JDK-only APIs in production code without
  an explicit baseline migration ADR.
- CI **required** jobs use Java 8 (Liberica 8 `jdk+fx` for Maven CI package
  work, Zulu 8 for legacy validate, Temurin 8 on `master`-only legacy build).
- Contributors should treat **Java 8** as the compatibility contract for
  behavior, bytecode, and dependency choices.

Building or packaging on a newer JDK does **not** change the runtime
contract until a dedicated migration item, ADR, and launcher/packaging work
land.

---

## JavaFX on Java 8

The tray/GUI modules (`application/trayView`, `application/habiTv`, and
out-of-reactor `application/habiTv-linux` / `application/habiTv-windows`)
use **JavaFX 2.x** APIs
and, on Java 8, expect **`jfxrt.jar`** on the classpath (resolved by
`HabitvLauncher` or `${jdk.home}` in legacy packaging POMs).

### Do not assume every Java 8 JDK includes JavaFX

Some **historical** Oracle JDK 8 and vendor builds shipped JavaFX inside
the JDK. Many **modern** Java 8 packages (plain Temurin 8, Zulu 8 without
the FX bundle, etc.) **do not** include JavaFX.

For **Java 8 GUI runtime**, use a distribution that explicitly ships
JavaFX, for example:

- [BellSoft Liberica Full JDK 8](https://bell-sw.com/pages/downloads/) (`jdk+fx`)
- [Azul Zulu 8](https://www.azul.com/downloads/) builds that include the JavaFX bundle

The **console** fat-jar path (`application/consoleView`) does not require
JavaFX for day-to-day download/export workflows.

### JDK 11 and later do not bundle JavaFX

From JDK 11 onward, JavaFX is **not** part of the JDK. OpenJFX is a
separate library set; shipping it to end users requires explicit packaging
(jlink, jpackage, shaded runtime, etc.) — tracked under
`javafx-modernization`, not finished in this policy phase.

---

## What PR #100 changed

[PR #100](https://github.com/Mika3578/habitv/pull/100) (`fix/javafx-openjfx-compile-profile`,
merged to `develop`) added a **`javafx-openjfx-compile`** Maven profile on
`application/trayView` and `application/habiTv`:

- Activates automatically when the build runs on **JDK 11+**.
- Adds **provided-scope** `org.openjfx` dependencies so JavaFX **APIs** are
  available at **compile** (and scoped **package** on modern build hosts).
- Aligns GUI modules with CI **diagnostic** JDK 11+ package jobs that exclude
  full JavaFX runtime packaging assumptions.

This is a **build-host bridge**, not a runtime migration.

---

## What PR #100 did not change

PR #100 did **not**:

- Raise the supported **end-user runtime** to Java 11, 17, or 21.
- Change `maven-compiler-plugin` **source/target** (still **1.8**).
- Replace `HabitvLauncher` **jfxrt.jar** classpath bootstrapping on Java 8.
- Bundle OpenJFX into release artifacts for users.
- Complete `javafx-modernization`, platform installers, or `${jdk.home}`
  legacy packaging modules.
- Retire risk `javafx-jdk8` or ADR `keep-java8-baseline`.

Habitv is **not** a Java 11/17/21 **runtime** application yet.

---

## Migration targets (why 17, then 21, not 25 now)

### Why Java 17 is the first runtime migration milestone

- **Long-term support (LTS)** with broad tooling, CI image, and library
  support already used in diagnostic jobs.
- Natural step after Java 11; many ecosystems treat 17 as the first
  "modern baseline" for enterprise desktop and server-side Java.
- Aligns with the **OpenJFX** versions used in the compile bridge profile
  (17.x line) while runtime packaging work is designed.
- Keeps scope bounded: one LTS jump from 8, with JAXB and JavaFX packaging
  addressed deliberately rather than in a single leap.

### Why Java 21 is the future stable target

- Current **LTS** cadence target for new deployments once 17 migration is
  proven in CI and release artifacts.
- Longer support window for a desktop app that may stay installed for years.
- Planned **after** 17 compatibility is demonstrated, not in parallel with
  the first migration.

### Why Java 25 is not the immediate target

- Newer, shorter track record in CI matrices and third-party plugins.
- Habitv still carries **legacy Maven plugins**, JAXB generation, and
  JavaFX 2.x assumptions; jumping to the newest JDK adds noise without
  reducing the real blockers below.
- JDK 25 may remain an **experimental diagnostic** JDK in CI; it is not the
  next supported runtime.

---

## Known blockers before real runtime migration

Real **runtime** migration (users run Habitv on JDK 17/21) is blocked until
at least:

| Blocker | Tracker / risk | Notes |
|---------|----------------|-------|
| **JavaFX runtime packaging** | `javafx-modernization`, `javafx-jdk8` | Move from `jfxrt.jar` / `${jdk.home}` to shipped OpenJFX or jpackage/jlink layout |
| **JAXB externalization** | `jaxb-mismatch`, `jaxb-launcher-recovery` | `javax.xml.bind` on modular JDKs; module-path vs classpath |
| **Old Maven plugins** | `java8-baseline`, audit baseline | Shade, Ant tasks, zenjava `javafx-maven-plugin`, compiler assumptions |
| **Launcher / runtime scripts** | `javafx-modernization` | `HabitvLauncher`, install scripts, `-Dhabitv.jfxrt.path` contract |
| **Plugin compatibility** | `provider-inventory` | Third-party JARs and native tools must stay Java 8 bytecode until coordinated bump |
| **CI matrix hardening** | `branch-protection`, [`required-checks-roadmap.md`](required-checks-roadmap.md) | Required gates only when stable; JDK 11+ package is diagnostic today |

---

## Migration roadmap

High-level steps (documentation-only sequencing; tracker items own delivery):

| Step | Goal | Status (approx.) |
|------|------|-------------------|
| **1** | Keep **Java 8** baseline stable (`validate` / `compile` / required CI on 8) | In progress — baseline done; maintenance ongoing |
| **2** | Make **build** compatible with **JDK 11+** hosts (compile/package bridge) | **Partial** — PR #100 (`javafx-openjfx-compile` profile) |
| **3** | Prepare **Java 17** runtime compatibility (JAXB, OpenJFX packaging, launcher) | Not started for runtime |
| **4** | Prepare **Java 21** runtime target (LTS stable deployment) | Future |
| **5** | Evaluate **Java 25** later (experimental CI only until blockers clear) | Diagnostic only |

Step 2 does **not** imply Step 3 is complete. Steps 3–4 require ADR
superseding `keep-java8-baseline`, green required CI on the target JDK, and
updated user-facing install docs.

---

## Build vs run (quick reference)

| JDK used for… | Java 8 | JDK 11+ |
|---------------|--------|---------|
| Official runtime for users | **Yes** | **No** (yet) |
| `mvn validate` / `compile` (reactor) | **Yes** (required CI) | Diagnostic CI |
| GUI `package` with JavaFX APIs | Liberica/Zulu **8 with FX** | **Provided** OpenJFX at compile via profile; not full runtime packaging |
| Console-only `package` | **Yes** | Often works; still Java 8 bytecode |

**Local parity (Java 8 baseline):**

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -DskipTests compile
mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package
```

See also [`docs/ci.md`](ci.md) and [`docs/runtime-quickstart.md`](runtime-quickstart.md).

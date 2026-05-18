# IHM build & launch — blocking points and proposed plan

**Tracker item** — `javafx-modernization` (HBTV-008) ·
**Phase** — `dev-plan.md` Phase 7 ·
**Companion risk** — `javafx-jdk8` (R-003).

This document captures the concrete blockers observed when trying to
build and launch the habiTv GUI (the "IHM" — `application/trayView`
packaged through `application/habiTv` and the `habiTv-linux` /
`habiTv-windows` native bundles) on a clean machine in 2026, and
proposes a phased plan to unblock it. It complements the high-level
roadmap entry for `javafx-modernization` with reproducible commands
and exact source citations.

It is plan-only: nothing here changes build, runtime or rule
behavior. Execution lands through the small follow-up PRs listed in
[§3](#3-proposed-plan).

---

## 1. Reproduction

All commands run from the repository root.

### 1.1 Baseline validate (still green)

```bash
mvn -B -ntp -DskipTests validate
```

→ `BUILD SUCCESS`, 33 reactor entries. Unchanged.

### 1.2 IHM compile attempt — JDK 21 (default in this environment)

```bash
mvn -B -ntp -DskipTests -pl application/trayView -am compile
```

→ `BUILD FAILURE` in `application/core` with 17 `cannot find symbol`
errors before the reactor even reaches `trayView`. All failures point
at JAXB-generated entities that expose `isBoolean()` instead of
`getBoolean()` getters. Representative excerpts:

```
application/core/src/com/dabi/habitv/core/dao/GrabConfigDAO.java:189
   symbol:   method getDownload()
   location: variable category of type
             com.dabi.habitv.grabconfig.entities.CategoryType
application/core/src/com/dabi/habitv/core/config/XMLUserConfig.java:474
   symbol:   method getUpdateOnStartup()
   location: class
             com.dabi.habitv.configuration.entities.Configuration.UpdateConfig
```

Inspection of `target/generated-sources/jaxb/.../CategoryType.java`
confirms the generator emitted:

```
public Boolean isDownload()       // <- expected getDownload()
public Boolean isDownloadable()   // <- expected getDownloadable()
public Boolean isTemplate()       // <- expected getTemplate()
public Boolean isDeleted()        // <- expected getDeleted()
```

The plugin used is the unmaintained
`com.sun.tools.xjc.maven2:maven-jaxb-plugin:1.1.1` (XJC core
`vhudson-jaxb-ri-2.1-833`, dated 2008). Under JDK 21 it falls back
to the `is*()` boxed-boolean naming. The Java consumer code in
`core` was written against the modern XJC contract (`get*()`).

### 1.3 IHM compile attempt — Temurin JDK 8

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 \
PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH \
mvn -B -ntp -DskipTests -pl application/trayView -am compile
```

→ `core` compiles successfully (JAXB output now uses `get*()`).
→ `trayView` then fails with `BUILD FAILURE` and dozens of
"package `javafx.*` does not exist" errors (sample):

```
trayView/.../HabiTvSplashScreen.java:8  package javafx.animation does not exist
trayView/.../HabitvViewMain.java:5      package javafx.application does not exist
trayView/.../controller/ConfigController.java:9
                                        package javafx.scene.control does not exist
```

Cause: `application/trayView/pom.xml` declares **no JavaFX
dependency**. Historically the module relied on
`$JAVA_HOME/jre/lib/ext/jfxrt.jar` being on the boot classpath
(Oracle JDK 7/8 only). The OpenJDK 8 packages on every supported
Linux distribution today (Ubuntu/Debian/Fedora/Alpine) ship
**without** JavaFX.

### 1.4 Full IHM package — `application/habiTv` shaded jar

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 \
mvn -B -ntp -DskipTests -pl application/habiTv -am package
```

→ `BUILD FAILURE` at `trayView` compilation (same root cause as
§1.3); `habiTv` is therefore unreachable. The shaded fat-jar
`com.dabi.habitv.HabitvLauncher` cannot be produced.

### 1.5 Native bundles — `habiTv-linux` / `habiTv-windows`

These modules are intentionally **out of the reactor** today and
cannot be activated with their current contents (cf.
`application/habiTv-linux/pom.xml:31`, `habiTv-windows/pom.xml`):

```
<jdk.home>/usr/lib/jvm/jdk1.7.0_45</jdk.home>           <!-- hardcoded -->
<javafx.runtime.lib.jar>${jdk.home}/jre/lib/ext/jfxrt.jar</javafx.runtime.lib.jar>
<javafx.tools.ant.jar>${jdk.home}/lib/ant-javafx.jar</javafx.tools.ant.jar>
...
<scope>system</scope>
<systemPath>${javafx.runtime.lib.jar}</systemPath>
...
<fx:deploy nativeBundles="all" ...>
```

Multiple compounding blockers:

- Hardcoded JDK 7 path that no longer exists.
- `<scope>system</scope>` plus a missing `<systemPath>` file fails
  Maven dependency resolution outright.
- The Ant FX tasks (`com.sun.javafx.tools.ant`) were removed from
  the JDK in JDK 11; replaced by `jpackage` (JDK 14+).
- `zenjava/javafx-maven-plugin:2.0` is unmaintained (last release
  2014) and incompatible with OpenJFX.
- `nativeBundles="all"` requires platform-specific tooling
  (`dpkg-deb`, `rpmbuild`, WiX, Inno Setup) that the CI does not
  install today.

---

## 2. Blocking points — consolidated

Severity column reflects "this blocks `mvn package` of
`application/habiTv` on a clean machine"; impact mirrors the
priorities already assigned in `risk-register.md`.

| # | Blocker | Severity | Affected file(s) | Risk slug |
|:-:|---|:--:|---|---|
| B1 | `maven-jaxb-plugin 1.1.1` emits `isBoolean()` instead of `getBoolean()` under any JDK > 8 | 🔴 P0 (consumers, not just IHM) | `application/core/pom.xml:51-91`, `application/core/src/.../GrabConfigDAO.java:189-362`, `application/core/src/.../XMLUserConfig.java:474-482` | `jaxb-mismatch` |
| B2 | `application/trayView/pom.xml` has no JavaFX dependency declaration | 🔴 P0 | `application/trayView/pom.xml` (13-29) | `javafx-jdk8` |
| B3 | `application/trayView` sources use 66 distinct `javafx.*` imports (JavaFX 2.x semantics; FXML namespace `http://javafx.com/javafx/8`) | 🟠 P1 | `application/trayView/src/**/*.java`, `application/trayView/src/**/*.fxml` | `javafx-jdk8` |
| B4 | `application/habiTv-linux/pom.xml` and `habiTv-windows/pom.xml` reference `${jdk.home}=/usr/lib/jvm/jdk1.7.0_45` | 🔴 P0 (for native bundles only) | `application/habiTv-linux/pom.xml:31-37`, `application/habiTv-windows/pom.xml` | `javafx-jdk8` |
| B5 | Native bundling uses `com.sun.javafx.tools.ant` Ant tasks (`<fx:deploy nativeBundles="all">`) — removed from JDK 11+ | 🔴 P0 (for native bundles only) | `application/habiTv-linux/pom.xml:65-105`, `habiTv-windows/pom.xml` | `javafx-jdk8` |
| B6 | `zenjava/javafx-maven-plugin:2.0` unmaintained, references missing JavaFX 2.x packaging hooks | 🟠 P1 | `application/trayView/pom.xml:42-49` | `javafx-jdk8` |
| B7 | `HabitvLauncher.main` adds `jfxrt.jar` to the system class loader via reflection on `URLClassLoader` — fails on JDK 9+ (`AppClassLoader` is no longer `URLClassLoader`) | 🟠 P1 | `application/habiTv/src/com/dabi/habitv/HabitvLauncher.java:16-44` | `javafx-jdk8` |
| B8 | `application/trayView/src/com/dabi/habitv/tray/view/HabiTvTrayView.java` mixes AWT `SystemTray` with JavaFX; no headless fallback (also blocks any CI smoke test of the launcher) | 🟡 P2 | `application/trayView/src/.../HabiTvTrayView.java:3-49` | new (proposed: `tray-headless-smoke`) |

### Why this matters

- The console CLI runs today (`console-runnable` is done) and the
  static repo (`static-repo-publish`) is published, so habiTv can be
  used in headless mode. The **IHM has been off the build map since
  the modernization restart**, and `docs/runtime-quickstart.md` (l.
  17-23) explicitly excludes `trayView`/`habiTv` from the documented
  `mvn package` command.
- The single hard rule `bump-java-baseline-beyond-8` (AGENTS.md §2)
  protects exactly the JAXB+JavaFX combination that B1+B3 sit on.
  Lifting either blocker without an ADR violates §2 of `AGENTS.md`.

---

## 3. Proposed plan

Six small PRs, each one independently mergeable, each one bounded
to a single tracker item. No PR in the list breaks the `Java 8
baseline` hard rule (§2 of `AGENTS.md`) on its own; only PR3 carries
the ADR that explicitly weakens it, with the residual risk
documented.

### PR1 · `ihm-build-doc` — Land this plan + capture the surface

| | |
|---|---|
| **Type** | `docs` |
| **Tracker** | `javafx-modernization`: criterion "Surface inventory captured in audit doc" → DONE |
| **Risk impact** | None — documentation only |
| **Validation** | `mvn -B -ntp -DskipTests validate` (unchanged) + AGENTS.md §11.3 checklist |

Deliverables:

- This file (`docs/ihm-build-blocking-points.md`).
- Update `docs/dev-tracker.{md,json}` to bump
  `javafx-modernization.progressPercent` from `5` to `15` and flip
  the "Surface inventory captured" criterion from 🟡 to ✅.
- Add the new criterion line **"Plan accepted via dedicated ADR"**
  pointing forward to PR3 below.
- Append a `Changed` entry to `CHANGELOG.md` `Unreleased` section.

### PR2 · `ihm-fx-system-profile` — Opt-in `system-fx-8` profile for trayView

| | |
|---|---|
| **Type** | `build` |
| **Tracker** | `javafx-modernization`: new criterion "trayView buildable with JavaFX 2.x on JDK 8 + Oracle/Zulu FX/Liberica Full" |
| **Risk impact** | Touches `javafx-jdk8` — does **not** mitigate (still tied to JDK 8) but unblocks reproducible compile for archaeology |
| **Validation** | Without `-Pmacaque -fx`: same as today. With profile + `JAVA_HOME` pointing at a JavaFX-bundling JDK 8: `mvn -B -ntp -DskipTests -pl application/trayView -am compile` → `BUILD SUCCESS` |

Deliverables:

- Add to `application/trayView/pom.xml` a profile `system-fx-8`
  (active only if `${env.JFXRT_JAR}` is set) that declares a
  `system`-scope `javafx:jfxrt:2.2` pointing at
  `${env.JFXRT_JAR}`. Profile is **not** active by default; the
  reactor remains unchanged when the env var is unset.
- Same for `application/habiTv-linux/pom.xml` and
  `habiTv-windows/pom.xml`, parameterizing `${jdk.home}` out of
  the property block (still out of reactor — no aggregator change).
- Document the supported JavaFX-bundling JDK 8 distributions
  (Zulu FX, Liberica Full, Corretto with javafx) in
  `docs/runtime-quickstart.md`.

Rationale — this PR has zero behaviour change for the green build
and gives the project a *reproducible* "this is what the legacy IHM
looks like compiled" reference, which is needed to verify the
migration in PR4 doesn't regress UI semantics.

### PR3 · `ihm-modernization-adr` — ADR proposing OpenJFX 17 LTS migration

| | |
|---|---|
| **Type** | `docs` (ADR + plan acceptance) |
| **Tracker** | `javafx-modernization`: criterion "Packaging blueprint accepted via dedicated ADR" → DONE |
| **Risk impact** | Proposes weakening AGENTS.md §2 row "Bump Java baseline beyond Java 8" — only for `application/trayView`, `application/habiTv`, `application/habiTv-linux`, `application/habiTv-windows`. Residual risk identified: `jaxb-mismatch` is still avoided because the new toolchain stays on JDK 8 for `core`/`framework`/`plugins` (Maven Toolchains, see PR4). |
| **Validation** | Cooling-off applies (§12.6) only if the ADR amends a meta-rule. Here it weakens a hard rule (§12.3) — needs explicit owner approval, no cooling-off. |

ADR content outline:

1. **Context** — restate B1–B8.
2. **Decision** — migrate the IHM modules to **OpenJFX 17 LTS** on
   **JDK 17** (compiled with `--release 17`), packaged with
   `jpackage`. Keep `core`, `framework`, all `plugins`, and
   `consoleView` on **JDK 8** via `<maven.compiler.release>8`. The
   reactor is split by Maven Toolchains.
3. **Alternatives rejected** — OpenJFX 21 (defers LTS until 2027
   when JDK 21 reaches FX LTS), OpenJFX 11 (out of FX LTS in
   2024), Swing rewrite (scope explosion).
4. **Consequences** — supersedes `keep-java8-baseline` partially;
   adds new risk `jpackage-platform-matrix`; obsoletes
   `zenjava/javafx-maven-plugin`.
5. **Touches** — `AGENTS.md §2`, `risk-register.md javafx-jdk8`,
   `dev-tracker.md javafx-modernization`.

### PR4 · `ihm-openjfx-trayview` — Port `trayView` to OpenJFX 17

| | |
|---|---|
| **Type** | `feat(ui)` (after PR3 merges and ADR moves to ✅ Accepted) |
| **Tracker** | `javafx-modernization`: criterion "trayView compiles with OpenJFX 17" |
| **Risk impact** | Mitigates `javafx-jdk8` for `trayView` |
| **Validation** | `mvn -B -ntp -DskipTests -pl application/trayView -am compile` (under JDK 17). Headless smoke: launch `Application.launch(HabitvViewMain.class, new String[0])` against a Monocle (`-Dprism.order=sw -Djava.awt.headless=true -Dtestfx.robot=glass -Dglass.platform=Monocle`) inside the surefire run, asserting the stage shows. |

Deliverables (single PR, minimal):

- Replace `${jdk.home}` and `<scope>system</scope>` JavaFX
  declarations with explicit `org.openjfx:javafx-controls:17`,
  `javafx-fxml:17`, `javafx-graphics:17` Maven dependencies in
  `application/trayView/pom.xml`.
- Replace `zenjava/javafx-maven-plugin:2.0` with
  `org.openjfx:javafx-maven-plugin:0.0.8`.
- Pin `<maven.compiler.release>17` on `trayView` only (toolchain
  switch via `maven-toolchains-plugin`).
- Bump FXML namespace from `http://javafx.com/javafx/8` to
  `http://javafx.com/javafx/17`.
- Refactor the 5 `EventHandler<WindowEvent>` callsites flagged in
  the audit (`PopinController.java:42`, `TrayMenu.java:20-36`,
  `HabiTvTrayView.java:44`, `WindowController.java:175`,
  `ViewController.java:282`) — the signatures don't change, but
  the lambdas must be tested under OpenJFX 17 (drift around
  `setOnCloseRequest` consume semantics).
- Confirm `Worker.State` and `Task` API surface is unchanged
  (`HabiTvSplashScreen.java:89-113`).
- Headless launch wrapper covered by a new test under
  `application/trayView/test/` (`testLauncherShowsSplash`).

### PR5 · `ihm-launcher-classpath` — Fix `HabitvLauncher` classpath manipulation

| | |
|---|---|
| **Type** | `fix(ui)` |
| **Tracker** | `javafx-modernization`: criterion "Shaded launcher boots on JDK 17" |
| **Risk impact** | Mitigates B7 — system class loader reflection hack |
| **Validation** | `java -jar application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar` prints `JavaFX runtime detected` without warnings, then exits headlessly with `--probe`. |

Deliverables:

- Delete the `addToClasspath` reflection at
  `application/habiTv/src/com/dabi/habitv/HabitvLauncher.java:16-44`.
  With OpenJFX as a real Maven dependency it is no longer
  needed; on JDK 9+ it throws because `AppClassLoader` is not
  `URLClassLoader`.
- Make the launcher idempotent — `--probe` exits 0 after JavaFX
  init for CI smoke.

### PR6 · `ihm-jpackage-bundles` — Replace `fx:deploy` with `jpackage` for `habiTv-linux` / `habiTv-windows`

| | |
|---|---|
| **Type** | `build` |
| **Tracker** | `javafx-modernization`: criterion "habiTv-linux + habiTv-windows re-enterable to the reactor" |
| **Risk impact** | Mitigates B4, B5; introduces `jpackage-platform-matrix` (already proposed in the ADR). |
| **Validation** | `mvn -B -ntp -DskipTests -pl application/habiTv-linux -am package` produces a `.deb` on Ubuntu CI; `application/habiTv-windows` produces an `.msi` on Windows CI. |

Deliverables:

- Replace the `<fx:deploy nativeBundles="all">` Ant block with
  `org.panteleyev:jpackage-maven-plugin` invocations.
- Drop `<scope>system</scope>` JavaFX entries (now transitive
  from `trayView`).
- Drop `<jdk.home>` property block; rely on
  `maven-toolchains-plugin` instead.
- Re-add both modules to `application/pom.xml` aggregator.
- Extend `.github/workflows/build.yml` with two `package` jobs
  (Ubuntu → `.deb`, Windows → `.msi`) gated on `develop`/`master`
  only.
- Publish artefacts to `habitv-repo` through the existing
  `static-repo-publish` profile.

---

## 4. Dependency graph between PRs

```
PR1 (docs)
   │
   ├──► PR2 (system-fx-8 profile, archaeology compile)
   │
   └──► PR3 (ADR — weakens AGENTS.md §2 row 2)
            │
            ├──► PR4 (trayView → OpenJFX 17)
            │       │
            │       └──► PR5 (launcher classpath fix)
            │               │
            │               └──► PR6 (jpackage native bundles)
            │
            └── (no other PR may start until PR3 ✅ Accepted)
```

Sequencing constraints:

- PR2 must land before PR4 so we have a side-by-side compile
  reference if PR4 introduces visible regressions.
- PR3 must merge before PR4/PR5/PR6 — they all depend on the
  `bump-java-baseline-beyond-8` weakening.
- PR5 must follow PR4 so the classpath fix is tested against
  the new OpenJFX dependency wiring.
- PR6 must follow PR5 so the bundled JAR boots before we wrap
  it in a native installer.

## 5. Out of scope here

The following are intentionally **not** part of this plan:

- Migrating `core`/`framework`/`plugins` off Java 8. The hard
  rule remains in force for non-IHM modules; PR3 only weakens it
  for the four IHM modules.
- Replacing `javax.xml.bind` 2.x with `jakarta.xml.bind`. Risk
  `jaxb-mismatch` still applies and is tracked separately.
- Rewriting the AWT-based `SystemTray` integration in
  `HabiTvTrayView.java`. Tracked as a new proposed item
  `tray-headless-smoke` (P2), out of scope for the IHM unblock.
- Replacing the runtime updater's HTTP-only mode with HTTPS +
  signed metadata. Already deferred under `legacy-update-pull`.

---

## 6. Validation summary table

| PR | Default validation | Stronger validation (only on that PR's branch) |
|:--:|---|---|
| PR1 | `mvn -B -ntp -DskipTests validate` | AGENTS.md §11.3 doc-sync checklist |
| PR2 | `mvn -B -ntp -DskipTests validate` | `JFXRT_JAR=... mvn -P system-fx-8 -pl application/trayView -am compile` |
| PR3 | `mvn -B -ntp -DskipTests validate` | ADR diff peer review |
| PR4 | `mvn -B -ntp -DskipTests -pl application/trayView -am compile` (JDK 17 toolchain) | `mvn -pl application/trayView test` (Monocle headless launch) |
| PR5 | `mvn -B -ntp -DskipTests -pl application/habiTv -am package` | `java -jar .../habiTv-*-shaded.jar --probe` exits 0 |
| PR6 | `mvn -B -ntp -DskipTests -pl application/habiTv-linux -am validate` (Ubuntu) and `… habiTv-windows -am validate` (Windows) | `mvn -pl application/habiTv-linux package` produces `.deb`; `.msi` on Windows job |

---

## 7. Open questions for the owner

These need a human call before PR3 can be opened:

1. **JDK 17 or JDK 21 for the IHM**? Picking 21 future-proofs but
   compresses the OpenJFX LTS window. The plan above assumes 17.
2. **`.msi` only or `.exe` too on Windows**? `jpackage` supports
   both; `.exe` needs WiX or Inno Setup on the Windows runner.
3. **Code signing**? Both `.deb` (`debsigs`) and `.msi` (Authenticode)
   benefit from signing for the runtime updater. Need certificates
   in `secrets` if so.
4. **Drop `habiTv-linux` and `habiTv-windows` in favour of a single
   `jpackage` module driven by an OS profile?** Would reduce
   duplication, but breaks the existing
   `mika3578.github.io/habitv-repo/repository/com/dabi/habitv/habiTv-linux/`
   updater path (`legacy-update-pull` risk).

When these are answered, PR3's ADR can land and PR4–PR6 can be
opened in the order shown.

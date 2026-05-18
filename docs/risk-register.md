# 🚨 Habitv Risk Register

Active risks for the modernization restart. Update when a risk is
added, mitigated, realized, or accepted.

> **Severity** = `Likelihood × Impact`. Priority sets execution order.

---

## 📊 Risk dashboard

| Status | Count |
|---|---:|
| 🟢 **Mitigated** | 4 |
| 🟠 **Open / High priority** | 4 |
| 🟡 **Open / Medium priority** | 4 |
| 🔴 **Open / Critical** | 0 |
| 🟢 **Open / Low priority** | 3 |
| **Total tracked** | **15** |

---

## 🗂️ Summary table

| ID | Title | Likelihood | Impact | Priority | Status |
|---|---|:--:|:--:|:--:|:--:|
| `R-001` | Legacy Maven repository / free.fr dependency | High | High | 🔴 P0 | 🟢 Mitigated (compile-time) |
| `R-002` | FTP deployment no longer viable | High | High | 🔴 P0 | 🟠 Open |
| `R-003` | JavaFX tied to JDK 8 assumptions | High | High | 🟠 P1 | 🟠 Open |
| `R-004` | JAXB generation / runtime mismatch | Med | High | 🟠 P1 | 🟠 Open |
| `R-005` | Live provider tests are non-deterministic | High | Med | 🟠 P1 | 🟡 Open |
| `R-006` | Provider endpoints obsolete or renamed | High | Med | 🟠 P1 | 🟡 Open |
| `R-007` | Auto-update pulls unexpected old artifacts | Med | High | 🟠 P1 | 🟠 Open |
| `R-008` | yt-dlp migration changes download behavior | Med | Med | 🟡 P2 | 🟡 Open |
| `R-009` | GitHub Pages layout mismatches updater | Med | High | 🟠 P1 | 🟡 Open |
| `R-010` | Intra-reactor version range excludes SNAPSHOTs | Low | Low | 🟢 P3 | 🟢 Mitigated |
| `R-011` | `maven-jaxb-plugin` missing pinned version | Low | Low | 🟢 P3 | 🟢 Mitigated |
| `R-012` | Plugin tester version mismatch blocks compile | Low | Low | 🟢 P3 | 🟢 Mitigated |
| `R-013` | Hardcoded YouTube Data API key | Med | Med | 🟡 P2 | 🟡 PR in flight |
| `R-014` | GitHub Pages directory autoindex gap | Med | Med | 🟡 P2 | 🟡 Open |
| `R-015` | Silent `stat()` ping at startup | Med | Med | 🟡 P2 | 🟡 Open |

---

## 🟢 Mitigated risks

### `R-010` — Intra-reactor version range excludes SNAPSHOTs

> 🟢 **Mitigated** · Likelihood **Low** · Impact **Low**

**Description** — Root `pom.xml` declared `dependencyManagement`
entries for `com.dabi.habitv:api` and `com.dabi.habitv:framework`
with the closed range `[4.1,4.2)`. Maven does not include
`4.1.0-SNAPSHOT` in that range by default, and the legacy
`dabiboo.free.fr` HTTP host is blocked by Maven 3.9+ defaults.

**Mitigation** — Replaced the range with `${project.version}` for
intra-reactor coordinates in HBTV-002. No code change; POM only.

---

### `R-011` — `maven-jaxb-plugin` missing pinned version

> 🟢 **Mitigated** · Likelihood **Low** · Impact **Low**

**Description** — `application/core/pom.xml` declared
`com.sun.tools.xjc.maven2:maven-jaxb-plugin` without a `<version>`.
Maven 3.9+ warns and may refuse to build in future versions.

**Mitigation** — Pinned to `1.1.1` in HBTV-002.

---

### `R-012` — Plugin tester version mismatch blocks compile

> 🟢 **Mitigated** · Likelihood **Low** · Impact **Low**

**Description** — Plugin modules declared test-scope dependencies
on `com.dabi.habitv:plugin-tester:4.1.0`, while the reactor builds
`4.1.0-SNAPSHOT`. During `mvn compile`, Maven failed at `6play`
trying to fetch the `4.1.0` descriptor from the blocked legacy
HTTP repository.

**Mitigation** — HBTV-010 aligned plugin test-harness versions to
reactor expressions and added `plugins/plugin-tester` to the
aggregator.

---

### `R-001` — Legacy Maven repository / free.fr dependency

> 🟢 **Compile-time mitigated** · Likelihood **High** · Impact **High** · 🔴 **P0**

**Description** — Root `pom.xml` and packaging POMs declared a
`<repository>` pointing at `http://dabiboo.free.fr/repository`
(plain HTTP, third-party hosting). If the host is down, removed, or
blocked by Maven defaults, project-specific artifacts cannot be
resolved.

**Mitigation status** — Compile-time mitigated by HBTV-012: root
`dependencyManagement` maps `api` and `framework` to
`${project.parent.version}`, so own-version plugin modules no
longer fetch plugin-local coordinates from blocked hosts.

**Residual risk** — Legacy repository migration is still required
for external publication and runtime updates. Tracked under
HBTV-004 / HBTV-005.

---

## 🟠 Open — High priority

### `R-002` — FTP deployment no longer viable

> 🟠 **Open** · Likelihood **High** · Impact **High** · 🔴 **P0**

**Description** — `<distributionManagement>` uses
`ftp://ftpperso.free.fr/repository` with `wagon-ftp 1.0-beta-6`.
FTP is unencrypted, the freebox personal pages target is
deprecated, and the password mechanism would expose credentials.

**Mitigation** — Remove FTP deploy in HBTV-004 / HBTV-005 and
replace with a documented static publication workflow.

---

### `R-003` — JavaFX tied to JDK 8 assumptions

> 🟠 **Open** · Likelihood **High** · Impact **High** · 🟠 **P1**

**Description** — `application/trayView` uses
`zenjava/javafx-maven-plugin 2.0` and JavaFX 2.x APIs;
`habiTv-linux` / `habiTv-windows` declare `system`-scope
`javafx:jfxrt` pointing at `${jdk.home}/jre/lib/ext/jfxrt.jar`
with hardcoded `jdk.home`. JDK 11+ no longer bundles JavaFX.

**Mitigation** — Tracked under HBTV-008. Do not migrate in this
restart phase; keep Java 8 baseline first.

---

### `R-004` — JAXB generation / runtime mismatch

> 🟠 **Open** · Likelihood **Medium** · Impact **High** · 🟠 **P1**

**Description** — `application/core` generates JAXB classes via
the unmaintained `com.sun.tools.xjc.maven2:maven-jaxb-plugin` and
depends on `javax.xml.bind:jaxb-api:2.0`. On JDK 9+,
`javax.xml.bind` is not on the default classpath.

**Mitigation** — Keep Java 8 baseline; revisit when migrating off
Java 8. Document under HBTV-001 follow-up if reactor surfaces
hidden generation failures.

---

### `R-007` — Auto-update pulls unexpected old artifacts during development

> 🟠 **Open** · Likelihood **Medium** · Impact **High** · 🟠 **P1**

**Description** — `UpdateManager` and `FindArtifactUtils` resolve
`FrameworkConf.UPDATE_URL` (`http://dabiboo.free.fr/repository`)
at runtime. A dev build can pull whatever is on that host (or
fail noisily if it is down).

**Mitigation** — Plan a feature flag / env override in HBTV-005 to
disable updates in development. Until then, document the risk.

---

## 🟡 Open — Medium / Low priority

### `R-005` — Live provider tests are non-deterministic

> 🟡 **Open** · Likelihood **High** · Impact **Medium** · 🟠 **P1**

**Description** — Many tests reach live endpoints (Canal+, beIN,
RSS, Dailymotion, kewego, etc.) via `BasePluginProviderTester` /
`BasePluginUpdateTester` and hardcoded URLs. They flap based on
remote availability and HTML/JSON changes.

**Mitigation** — Keep tests excluded from the default lifecycle
in HBTV-002; quarantine network tests behind an opt-in profile.

---

### `R-006` — Provider endpoints obsolete or renamed

> 🟡 **Open** · Likelihood **High** · Impact **Medium** · 🟠 **P1**

**Description** — Several providers (Pluzz, legacy Canal+, beIN)
are likely dead or renamed. Provider plugins may compile but never
produce results in production.

**Mitigation** — Inventory in HBTV-006. Removal/rename happens in
dedicated PRs, not in this restart bootstrap.

---

### `R-008` — yt-dlp migration can change download behavior

> 🟡 **Open** · Likelihood **Medium** · Impact **Medium** · 🟡 **P2**

**Description** — `yt-dlp` is not a drop-in for `youtube-dl`:
option parsing, output templates, and post-processors differ.

**Mitigation** — HBTV-007 plans the migration with a behavior diff
and deprecation note before any code change. Command wiring is
already covered by an offline test (HBTV-011).

---

### `R-009` — GitHub Pages / static repo layout may not match old updater expectations

> 🟡 **Open** · Likelihood **Medium** · Impact **High** · 🟠 **P1**

**Description** — The runtime updater expects a specific directory
layout (groupId-as-path, artifactId folder, version list, latest
marker). Hosting on GitHub Pages or another static host may diverge
subtly and break update discovery.

**Mitigation** — HBTV-005 defines the layout explicitly and
validates it against `FindArtifactUtils.findLastVersionUrl`
semantics before cutting over.

---

### `R-013` — Hardcoded YouTube Data API key

> 🟡 **PR in flight** · Likelihood **Medium** · Impact **Medium** · 🟡 **P2**

**Description** — `plugins/youtube` embedded a concrete YouTube
Data API key in source. If the key is revoked, quota-exhausted, or
restricted to another referrer/IP, the provider search fails with
HTTP 403 and requires a new build to change credentials.

**Mitigation** — HBTV-013: externalize key resolution to runtime
configuration with deterministic precedence (system property then
environment variable), add a tray configuration field, and include
sanitized request context in error messages. PR #29 awaits a
documentation merge conflict resolution.

---

### `R-014` — GitHub Pages autoindex gap

> 🟡 **Open** · Likelihood **Medium** · Impact **Medium** · 🟡 **P2**

**Description** — GitHub Pages does not provide Apache-style
`mod_autoindex` directory listings. `FindArtifactUtils` discovers
artifact versions by parsing index pages, which assumes autoindex.

**Mitigation** — HBTV-005 plans to generate static `index.html`
files (or a manifest) so listing semantics are preserved without
relying on the host.

---

### `R-015` — Silent `stat()` ping at startup

> 🟡 **Open** · Likelihood **Medium** · Impact **Medium** · 🟡 **P2**

**Description** — `application/core/.../CoreManager.java` pings
`HabitTvConf.STAT_URL` (`http://dabiboo.free.fr/cpt.php`) at
startup for telemetry. Dev builds and CI silently contact the
legacy host.

**Mitigation** — Plan a quarantine flag (`habitv.stat.enabled`,
default `false`) under HBTV-004; remove the legacy host once the
flag ships.

---

## 📜 Legend

| Symbol | Meaning |
|:---:|---|
| 🟢 Mitigated | Cause removed, secondary safeguards in place |
| 🟠 Open / High | P0 / P1 — active threat to the buildable or shippable baseline |
| 🟡 Open / Medium | P2 — needs action but not blocking the baseline |
| 🟢 Open / Low | P3 — known, accepted, monitored |
| 🔴 Open / Critical | Realized incident or imminent breakage |

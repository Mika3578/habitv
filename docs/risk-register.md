# 🚨 Habitv Risk Register

Active risks for the modernization restart. Update when a risk is
added, mitigated, realized, or accepted.

> 📝 **Identifier convention** — every risk uses a descriptive
> kebab-case slug. The opaque legacy codes (`R-XXX`) are preserved on
> each entry for compatibility with existing PRs and commits. See the
> `descriptive-slug-ids` ADR for the full mapping.

> **Severity** = `Likelihood × Impact`. Priority sets execution order.

---

## 📊 Risk dashboard

| Status | Count |
|---|---:|
| 🟢 **Mitigated** | 5 |
| 🟠 **Open / High priority** | 4 |
| 🟡 **Open / Medium priority** | 3 |
| 🔴 **Open / Critical** | 0 |
| 🟢 **Open / Low priority** | 3 |
| **Total tracked** | **15** |

---

## 🗂️ Summary table

| Risk | Likelihood | Impact | Priority | Status |
|---|:--:|:--:|:--:|:--:|
| `legacy-maven-repo` | High | High | 🔴 P0 | 🟢 Mitigated (compile-time) |
| `ftp-deploy` | High | High | 🔴 P0 | 🟠 Open |
| `javafx-jdk8` | High | High | 🟠 P1 | 🟠 Open |
| `jaxb-mismatch` | Med | High | 🟠 P1 | 🟠 Open |
| `live-tests-flaky` | High | Med | 🟠 P1 | 🟡 Open |
| `provider-endpoints-dead` | High | Med | 🟠 P1 | 🟡 Open |
| `legacy-update-pull` | Med | High | 🟠 P1 | 🟠 Open |
| `ytdlp-behavior-diff` | Med | Med | 🟡 P2 | 🟡 Open |
| `pages-layout-mismatch` | Med | High | 🟠 P1 | 🟡 Open |
| `reactor-version-range` | Low | Low | 🟢 P3 | 🟢 Mitigated |
| `jaxb-plugin-unpinned` | Low | Low | 🟢 P3 | 🟢 Mitigated |
| `plugin-tester-mismatch` | Low | Low | 🟢 P3 | 🟢 Mitigated |
| `youtube-key-hardcoded` | Med | Med | 🟡 P2 | 🟢 Mitigated |
| `pages-autoindex-gap` | Med | Med | 🟡 P2 | 🟡 Open |
| `silent-stat-ping` | Med | Med | 🟡 P2 | 🟡 Open |

---

## 🟢 Mitigated risks

### `reactor-version-range` — Intra-reactor version range excludes SNAPSHOTs

| | |
|---|---|
| **Status** | 🟢 Mitigated |
| **Likelihood** | Low · **Impact** Low |
| **Legacy code** | R-010 |

**Description** — Root `pom.xml` declared `dependencyManagement`
entries for `com.dabi.habitv:api` and `com.dabi.habitv:framework`
with the closed range `[4.1,4.2)`. Maven does not include
`4.1.0-SNAPSHOT` in that range by default, and the legacy
`dabiboo.free.fr` HTTP host is blocked by Maven 3.9+ defaults.

**Mitigation** — Replaced the range with `${project.version}` for
intra-reactor coordinates in `java8-baseline`. No code change;
POM only.

---

### `jaxb-plugin-unpinned` — `maven-jaxb-plugin` missing pinned version

| | |
|---|---|
| **Status** | 🟢 Mitigated |
| **Likelihood** | Low · **Impact** Low |
| **Legacy code** | R-011 |

**Description** — `application/core/pom.xml` declared
`com.sun.tools.xjc.maven2:maven-jaxb-plugin` without a `<version>`.
Maven 3.9+ warns and may refuse to build in future versions.

**Mitigation** — Pinned to `1.1.1` in `java8-baseline`.

---

### `plugin-tester-mismatch` — Plugin tester version mismatch blocks compile

| | |
|---|---|
| **Status** | 🟢 Mitigated |
| **Likelihood** | Low · **Impact** Low |
| **Legacy code** | R-012 |

**Description** — Plugin modules declared test-scope dependencies
on `com.dabi.habitv:plugin-tester:4.1.0`, while the reactor builds
`4.1.0-SNAPSHOT`. During `mvn compile`, Maven failed at `6play`
trying to fetch the `4.1.0` descriptor from the blocked legacy
HTTP repository.

**Mitigation** — `plugin-tester-align` aligned plugin test-harness
versions to reactor expressions and added `plugins/plugin-tester`
to the aggregator.

---

### `legacy-maven-repo` — Legacy Maven repository / free.fr dependency

| | |
|---|---|
| **Status** | 🟢 Compile-time mitigated |
| **Likelihood** | High · **Impact** High · **Priority** 🔴 P0 |
| **Legacy code** | R-001 |

**Description** — Root `pom.xml` and packaging POMs declared a
`<repository>` pointing at `http://dabiboo.free.fr/repository`
(plain HTTP, third-party hosting). If the host is down, removed, or
blocked by Maven defaults, project-specific artifacts cannot be
resolved.

**Mitigation status** — Compile-time mitigated by
`own-version-deps-align`: root `dependencyManagement` maps `api` and
`framework` to `${project.parent.version}`, so own-version plugin
modules no longer fetch plugin-local coordinates from blocked hosts.

**Residual risk** — Legacy repository migration is still required
for external publication and runtime updates. Tracked under
`legacy-url-migration` / `static-repo-publish`.

**Status update (`legacy-url-migration`)** — Active Maven repository
wiring no longer references `http://dabiboo.free.fr/repository` in
active POM paths (PR #36 merged). Functional publication cutover
remains blocked under `static-repo-publish` until
`https://mika3578.github.io/habitv-repo/repository` is published with
a layout compatible with local reactor builds.

---

## 🟠 Open — High priority

### `ftp-deploy` — FTP deployment no longer viable

| | |
|---|---|
| **Status** | 🟠 Open |
| **Likelihood** | High · **Impact** High · **Priority** 🔴 P0 |
| **Legacy code** | R-002 |

**Description** — `<distributionManagement>` uses
`ftp://ftpperso.free.fr/repository` with `wagon-ftp 1.0-beta-6`.
FTP is unencrypted, the freebox personal pages target is
deprecated, and the password mechanism would expose credentials.

**Mitigation** — Remove FTP deploy in `legacy-url-migration` /
`static-repo-publish` and replace with a documented static
publication workflow.

**Status update (`legacy-url-migration`)** — Active packaging POMs
no longer declare `ftp://ftpperso.free.fr/repository`
distributionManagement blocks (PR #36 merged).

---

### `javafx-jdk8` — JavaFX tied to JDK 8 assumptions

| | |
|---|---|
| **Status** | 🟠 Open |
| **Likelihood** | High · **Impact** High · **Priority** 🟠 P1 |
| **Legacy code** | R-003 |

**Description** — `application/trayView` uses
`zenjava/javafx-maven-plugin 2.0` and JavaFX 2.x APIs;
`habiTv-linux` / `habiTv-windows` declare `system`-scope
`javafx:jfxrt` pointing at `${jdk.home}/jre/lib/ext/jfxrt.jar`
with hardcoded `jdk.home`. JDK 11+ no longer bundles JavaFX.

**Mitigation** — Tracked under `javafx-modernization`. Do not
migrate in this restart phase; keep Java 8 baseline first.

---

### `jaxb-mismatch` — JAXB generation / runtime mismatch

| | |
|---|---|
| **Status** | 🟠 Open |
| **Likelihood** | Medium · **Impact** High · **Priority** 🟠 P1 |
| **Legacy code** | R-004 |

**Description** — `application/core` generates JAXB classes via
the unmaintained `com.sun.tools.xjc.maven2:maven-jaxb-plugin` and
depends on `javax.xml.bind:jaxb-api:2.0`. On JDK 9+,
`javax.xml.bind` is not on the default classpath.

**Mitigation** — Keep Java 8 baseline; revisit when migrating off
Java 8.

---

### `legacy-update-pull` — Auto-update pulls unexpected old artifacts during development

| | |
|---|---|
| **Status** | 🟠 Open |
| **Likelihood** | Medium · **Impact** High · **Priority** 🟠 P1 |
| **Legacy code** | R-007 |

**Description** — `UpdateManager` and `FindArtifactUtils` resolve
`FrameworkConf.UPDATE_URL` (`http://dabiboo.free.fr/repository`)
at runtime. A dev build can pull whatever is on that host (or
fail noisily if it is down).

**Mitigation** — Updates are disabled by default; explicit opt-in
required before any runtime fetch (`habitv.update.enabled=true`,
optional `habitv.update.url`).

**Status update (`legacy-url-migration`)** — `UpdateManager` returns
immediately unless `habitv.update.enabled=true`. The default target
base URL constant is `https://mika3578.github.io/habitv-repo/repository`
(legacy DabiBoo removed). Do not enable updates until
`static-repo-publish` publishes Apache-style directory indexes or an
equivalent manifest layout.

---

## 🟡 Open — Medium / Low priority

### `live-tests-flaky` — Live provider tests are non-deterministic

| | |
|---|---|
| **Status** | 🟡 Open |
| **Likelihood** | High · **Impact** Medium · **Priority** 🟠 P1 |
| **Legacy code** | R-005 |

**Description** — Many tests reach live endpoints (Canal+, beIN,
RSS, Dailymotion, kewego, etc.) via `BasePluginProviderTester` /
`BasePluginUpdateTester` and hardcoded URLs. They flap based on
remote availability and HTML/JSON changes.

**Mitigation** — Keep tests excluded from the default lifecycle
in `java8-baseline`; quarantine network tests behind an opt-in
profile.

---

### `provider-endpoints-dead` — Provider endpoints obsolete or renamed

| | |
|---|---|
| **Status** | 🟡 Open |
| **Likelihood** | High · **Impact** Medium · **Priority** 🟠 P1 |
| **Legacy code** | R-006 |

**Description** — Several providers (Pluzz, legacy Canal+, beIN)
are likely dead or renamed. Provider plugins may compile but never
produce results in production.

**Mitigation** — Inventory in `provider-inventory`.
Removal/rename happens in dedicated PRs, not in this restart
bootstrap.

---

### `ytdlp-behavior-diff` — yt-dlp migration can change download behavior

| | |
|---|---|
| **Status** | 🟡 Open |
| **Likelihood** | Medium · **Impact** Medium · **Priority** 🟡 P2 |
| **Legacy code** | R-008 |

**Description** — `yt-dlp` is not a drop-in for `youtube-dl`:
option parsing, output templates, and post-processors differ.

**Mitigation** — `ytdlp-migration` plans the migration with a
behavior diff and deprecation note before any code change. Command
wiring is already covered by an offline test from
`console-runnable`.

---

### `pages-layout-mismatch` — GitHub Pages / static repo layout may not match old updater expectations

| | |
|---|---|
| **Status** | 🟡 Open |
| **Likelihood** | Medium · **Impact** High · **Priority** 🟠 P1 |
| **Legacy code** | R-009 |

**Description** — The runtime updater expects a specific directory
layout (groupId-as-path, artifactId folder, version list, latest
marker). Hosting on GitHub Pages or another static host may diverge
subtly and break update discovery.

**Mitigation** — `static-repo-publish` defines the layout
explicitly and validates it against
`FindArtifactUtils.findLastVersionUrl` semantics before cutting
over.

**Status update (`legacy-url-migration` / `static-repo-publish`)** —
`FindArtifactUtils` parses HTML directory listings via anchor tags
(Apache `mod_autoindex` shape). GitHub Pages does not provide that
listing by default. Runtime updates stay disabled
(`habitv.update.enabled` defaults false) until static `index.html`
files or manifests are published and verified under
`static-repo-publish`. Keep this risk at P1 until cutover
validation completes.

---

### `youtube-key-hardcoded` — Hardcoded YouTube Data API key

| | |
|---|---|
| **Status** | 🟢 Mitigated |
| **Likelihood** | Medium · **Impact** Medium · **Priority** 🟡 P2 |
| **Legacy code** | R-013 |

**Description** — `plugins/youtube` embedded a concrete YouTube
Data API key in source. If the key is revoked, quota-exhausted, or
restricted to another referrer/IP, the provider search fails with
HTTP 403 and requires a new build to change credentials.

**Mitigation** — Mitigated in `youtube-apikey` (PR #29 merged):
`YoutubeConf` no longer embeds a concrete API key constant; keys
are resolved from runtime configuration (Java property
`habitv.youtube.apiKey`, then environment variable
`HABITV_YOUTUBE_API_KEY`); tray configuration exposes a
user-editable field; error messages mask the `key` parameter.

---

### `pages-autoindex-gap` — GitHub Pages autoindex gap

| | |
|---|---|
| **Status** | 🟡 Open |
| **Likelihood** | Medium · **Impact** Medium · **Priority** 🟡 P2 |
| **Legacy code** | R-014 |

**Description** — GitHub Pages does not provide Apache-style
`mod_autoindex` directory listings. `FindArtifactUtils` discovers
artifact versions by parsing index pages, which assumes autoindex.

**Mitigation** — `static-repo-publish` plans to generate static
`index.html` files (or a manifest) so listing semantics are
preserved without relying on the host.

---

### `silent-stat-ping` — Silent `stat()` ping at startup

| | |
|---|---|
| **Status** | 🟡 Open |
| **Likelihood** | Medium · **Impact** Medium · **Priority** 🟡 P2 |
| **Legacy code** | R-015 |

**Description** — `application/core/.../CoreManager.java` pings
`HabitTvConf.STAT_URL` (`http://dabiboo.free.fr/cpt.php`) at
startup for telemetry. Dev builds and CI silently contact the
legacy host.

**Mitigation** — Plan a quarantine flag (`habitv.stat.enabled`,
default `false`) under `legacy-url-migration`; remove the legacy
host once the flag ships.

---

## 📜 Legend

| Symbol | Meaning |
|:---:|---|
| 🟢 Mitigated | Cause removed, secondary safeguards in place |
| 🟠 Open / High | P0 / P1 — active threat to the buildable or shippable baseline |
| 🟡 Open / Medium | P2 — needs action but not blocking the baseline |
| 🟢 Open / Low | P3 — known, accepted, monitored |
| 🔴 Open / Critical | Realized incident or imminent breakage |

---

## 🗂️ Legacy code index

| Legacy code | Slug |
|---|---|
| R-001 | `legacy-maven-repo` |
| R-002 | `ftp-deploy` |
| R-003 | `javafx-jdk8` |
| R-004 | `jaxb-mismatch` |
| R-005 | `live-tests-flaky` |
| R-006 | `provider-endpoints-dead` |
| R-007 | `legacy-update-pull` |
| R-008 | `ytdlp-behavior-diff` |
| R-009 | `pages-layout-mismatch` |
| R-010 | `reactor-version-range` |
| R-011 | `jaxb-plugin-unpinned` |
| R-012 | `plugin-tester-mismatch` |
| R-013 | `youtube-key-hardcoded` |
| R-014 | `pages-autoindex-gap` |
| R-015 | `silent-stat-ping` |

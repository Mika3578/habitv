# 🗺️ Habitv Development Plan

High-level phased roadmap for the Habitv modernization restart.
**Integration branch:** `develop` (PR-based, linear history on feature
branches). Each phase is delivered by one or more small PRs tied to
[`dev-tracker.md`](dev-tracker.md). No phase is allowed to bundle work from a
later phase.

> **Legacy vs current** — Phases 0–4 removed obsolete Dabiboo/free.fr Maven
> and runtime URLs; active work is Phases 5–7 (yt-dlp, providers, JavaFX).
> Historical pre-restart findings live in
> [`audit-master-baseline.md`](audit-master-baseline.md) (snapshot only).

---

## 🎯 Phase progress

```
███████████████▋░░░░  79%   (tracker-aligned overall progress)
```

| Phase | Title | Status | Progress | Tracker items |
|:----:|---|:--:|---|---|
| 0 | 🏗️ Governance bootstrap | ✅ Done | `████████████████████` 100% | `gov-bootstrap` |
| 1 | ⚙️ Stabilize Maven reactor | ✅ Done | `████████████████████` 100% | `maven-reactor` |
| 2 | ☕ Stabilize Java 8 baseline | ✅ Done | `████████████████████` 100% | `java8-baseline`, `plugin-tester-align`, `own-version-deps-align` |
| 3 | 🔗 Remove legacy free.fr / SVN / FTP | ✅ Done | `████████████████████` 100% | `legacy-url-migration`, `youtube-apikey` |
| 4 | 📦 Publish artifacts via `habitv-repo` | ✅ Done | `████████████████████` 100% | `static-repo-publish` |
| 5 | 🎬 Replace `youtube-dl` with `yt-dlp` | 🟡 In progress | `███████████████░░░░░` 75% | `ytdlp-migration` |
| 6 | 🔌 Audit / deprecate obsolete providers | 🟡 In progress | `███████████░░░░░░░░░` 55% | `provider-inventory` |
| 7 | 🖼️ Modernize UI / runtime packaging | 🔵 Proposed | `█░░░░░░░░░░░░░░░░░░░` 5% | `javafx-modernization` |

---

## 🏗️ Phase 0 — Governance bootstrap from master

✅ **Done**

- Establish branching model, CI baseline, AI agent guidance, and the
  documentation skeleton.
- Deliverables: `.github/` templates and workflow, `.cursor` rules,
  `AGENTS.md`, `docs/` plan / tracker / risk / decision / settings / audit.
- Validation: `mvn -B -ntp -DskipTests validate` on Ubuntu + Windows
  with Temurin 8.

**Exit criteria** — bootstrap PR merged to `master`; integration
branch `develop` created.

---

## ⚙️ Phase 1 — Stabilize Maven reactor

✅ **Done**

- Wire root `pom.xml` as an actual reactor parent.
- Aggregate `fwk`, `application`, `plugins` so `mvn validate` walks
  the full project.
- Reconcile parent version mismatches (`4.1.0` vs `4.1.0-SNAPSHOT`,
  the `4.1.0-SNASPHOT` typo, etc.).
- Decide the status of `plugins/plugin-tester` and
  `application/habiTv-linux` / `habiTv-windows`.

**Tracker** — `maven-reactor`.

---

## ☕ Phase 2 — Stabilize Java 8 build baseline

✅ **Done**

- Make `mvn -B -ntp -DskipTests compile` succeed on Temurin 8 in CI.
- Pin `maven-compiler-plugin`, `maven-surefire-plugin`, and
  `maven-failsafe-plugin` to versions compatible with Java 8 and the
  legacy `testSourceDirectory` layout.
- Quarantine network-dependent tests behind an opt-in profile (next
  phase or a dedicated follow-up).

**Trackers** — `java8-baseline`, `plugin-tester-align`,
`own-version-deps-align`.

---

## 🔗 Phase 3 — Remove legacy free.fr / SVN / FTP references

✅ **Done**

- Replace SVN/Assembla `<scm>` blocks with the current GitHub URLs.
- Remove FTP `<distributionManagement>` and the `wagon-ftp` extension.
- Replace `http://dabiboo.free.fr/repository` with the planned
  GitHub Pages / static repo location (see Phase 4).
- Quarantine `STAT_URL` / `UPDATE_URL` behind feature flags so
  development builds do not ping legacy hosts.
- Externalize the hardcoded YouTube Data API key.

**Trackers** — `legacy-url-migration`, `youtube-apikey`.

---

## 📦 Phase 4 — Publish artifacts to `habitv-repo`

✅ **Done** · `████████████████████` 100%

- Stand up the `habitv-repo` static repository (GitHub Pages or
  equivalent HTTPS host) for artifacts, plugin drops, and update
  metadata.
- Define a directory layout compatible with the existing runtime
  updater (`FindArtifactUtils`, `UpdateManager`).
- Document the publication workflow and credential handling.
- Validate the generated local repository tree (`plugins.txt`,
  `com/dabi/habitv`, required `index.html` links) before publication.
- Validate live GitHub Pages URLs:
  `https://mika3578.github.io/habitv-repo/repository/`,
  `.../plugins.txt`, and `.../com/dabi/habitv/`.

**Tracker** — `static-repo-publish`. Deploy profile `static-repo-deploy`,
`deployAtEnd=true`, and opt-in live provider tests (`-Plive-provider-tests`)
keep `habitv-repo` publication deterministic.

---

## 🎬 Phase 5 — Replace `youtube-dl` with `yt-dlp`

🟡 **In progress** · `███████████████░░░░░` 75%

- Switch the `youtube` plugin's binary expectations from
  `youtube-dl` to `yt-dlp` (executable name, command flags,
  output parsing, post-processors).
- Migrate the default config (`application/core/configuration.xml`).
- Provide a deprecation note for users still on `youtube-dl`.

**Tracker** — `ytdlp-migration`. CLI contract: `docs/ytdlp-cli-compatibility.md`.
Remaining: live fixture validation, `habitv-repo` `yt-dlp` tool zip publication.

---

## 🔌 Phase 6 — Audit / deprecate obsolete providers

🟡 **In progress** · `███████████░░░░░░░░░` 55%

- Inventory all provider plugins and verify endpoints are reachable
  and parsable (offline against captured fixtures, not live).
- Mark obsolete or renamed providers with a deprecation plan and
  dedicated removal PRs.
- Baseline classification doc at `docs/provider-inventory.md`;
  offline fixture baseline for `6play`, `canalPlus`, `francetv` (ex
  `pluzz`), `arte`, and `youtube`.

**Tracker** — `provider-inventory`.

---

## 🖼️ Phase 7 — Modernize UI / runtime packaging

🔵 **Proposed** · `█░░░░░░░░░░░░░░░░░░░` 5%

- Migrate JavaFX 2.x (`jfxrt.jar` on Java 8; optional provided OpenJFX at
  compile on JDK 11+ per PR #100) to **shipped** OpenJFX runtime packaging
  (`jpackage`, `jlink`, or equivalent).
- Re-evaluate `application/habiTv-linux` and `habiTv-windows`
  packaging.
- Modernize the runtime updater (HTTPS, signed metadata) once the
  static repo from Phase 4 is in place.

**Tracker** — `javafx-modernization`.

---

## 🧭 Phase order reasoning

```
Phase 0 ─┬─► Phase 1 ─► Phase 2 ─┬─► Phase 3 ─► Phase 4 ─┬─► Phase 5
         │                       │                       │
         └──── governance ───────┘                       └─► Phase 6
                                                            │
                                                            └─► Phase 7
```

- Phases 0–2 establish a buildable baseline so later changes have a
  fixed reference point.
- Phase 3 cannot be safely flipped without Phase 4's static host in
  sight (otherwise the runtime updater hits the unreachable old host).
- Phase 5 depends on Phase 6's offline fixtures to avoid silent
  regressions.
- Phase 7 sits last because JavaFX packaging is the biggest behavior
  change and benefits from every earlier stabilization.

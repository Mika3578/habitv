# GitHub pre-release checklist (Community Alpha)

Use this checklist before publishing a **Community Alpha / Technical Preview** GitHub
pre-release. It complements [`community-alpha-release.md`](community-alpha-release.md).

**Tracker:** `community-alpha-release-readiness` (HBTV-016)

---

## Build validation

- [ ] Branch is based on current `develop` (or release branch agreed with maintainers).
- [ ] `git status --short` is clean (no accidental local artifacts staged).
- [ ] `mvn -B -ntp -DskipTests validate` → **BUILD SUCCESS** on Java 8.
- [ ] `mvn -B -ntp clean package` → **BUILD SUCCESS**; shaded JAR exists at  
      `application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar`.
- [ ] Release notes state **Java 8 + JavaFX** requirement (Zulu 8 FX or equivalent).
- [ ] Version string in docs matches `${project.version}` (`4.1.0-SNAPSHOT` until version bump PR).

---

## Test validation

- [ ] `mvn -B -ntp test` executed; results pasted in PR / release notes (failures documented).
- [ ] Failing modules classified as **release blocker** vs **known alpha limitation**.
- [ ] Live provider tests (`-Plive-provider-tests`) **not** required for alpha tag (opt-in only).
- [ ] Deterministic subset green: `fwk/api`, `fwk/framework`, `application/core`, `plugins/plugin-tester` (CI parity).

---

## Runtime launch validation

- [ ] Windows: `java -jar habiTv-4.1.0-SNAPSHOT.jar` opens tray GUI on JDK 8 with JavaFX.
- [ ] Windows: console mode starts with CLI args (no GUI).
- [ ] JavaFX missing path documented (`-Dhabitv.jfxrt.path=...`).
- [ ] Config folder `%USERPROFILE%\habitv\` created; `configuration.xml` load/save verified.
- [ ] Log file `%USERPROFILE%\habitv\habiTv.log` written on activity.

---

## Plugin update validation

- [ ] Default `UPDATE_URL` is `https://mika3578.github.io/habitv-repo/repository/` (no `dabiboo.free.fr`).
- [ ] Plugin update smoke test on Windows (optional snapshot channel if enabled in config).
- [ ] `UpdateRepositoryUrlsTest` / framework update tests still pass offline.

---

## Provider status review

- [ ] [`provider-status.md`](provider-status.md) reviewed and matches current code.
- [ ] Release notes **do not** claim all README providers work.
- [ ] Obsolete providers (`canalPlus`, `6play`, `wat`, `beinsport`, `clubic`, …) called out.
- [ ] `francetv` vs legacy `pluzz` id migration noted for existing configs.

---

## Known issues review

- [ ] [`docs/risk-register.md`](risk-register.md) open risks referenced where relevant.
- [ ] Security / dependency audit status: **in progress**, not fully remediated.
- [ ] `javafx-modernization` blocker for native installer documented.
- [ ] yt-dlp binary path documented; `ytdlp-migration` wiring called out.

---

## GitHub release draft

- [ ] Tag name agreed (e.g. `v4.1.0-alpha.1` — separate version PR if needed).
- [ ] Mark release as **pre-release** in GitHub UI.
- [ ] Title includes "Community Alpha" or "Technical Preview".
- [ ] Body links to `docs/community-alpha-release.md` and `docs/provider-status.md`.
- [ ] Limitations section copied or summarized from alpha release doc.
- [ ] No secrets, tokens, or local absolute paths in notes.

---

## Artifact upload

- [ ] Upload `habiTv-4.1.0-SNAPSHOT.jar` (and optional `bin\` README for external tools).
- [ ] SHA-256 checksum recorded in release notes (optional but recommended).
- [ ] Do **not** upload `target/`, `.class`, or full Maven reactor zips unless intentional.
- [ ] Confirm artifact size reasonable (shaded JAR only for alpha).

---

## Community announcement

- [ ] Issue template **Community alpha test report** enabled (`.github/ISSUE_TEMPLATE/`).
- [ ] Short announcement: alpha scope, Java 8 requirement, provider limitations.
- [ ] Point testers to Windows launch section and log attachment requirement.
- [ ] Set expectations: feedback welcome; stable release not promised.

---

## Post-publish

- [ ] Update `community-alpha-release-readiness` tracker progress / status.
- [ ] Triage incoming alpha issues with `alpha` label.
- [ ] Schedule follow-up PRs (provider rewrites, dependency audit, installer).

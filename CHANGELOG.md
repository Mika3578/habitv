# 📜 Changelog

All notable changes to **Habitv** are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and the project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

> 🚧 **Modernization phase** — the project is being restarted from a
> long-dormant baseline (last commit 2018-04-21, last published
> release 4.1.0 in 2017). The `Unreleased` section tracks the
> restart-from-master work toward the next publishable build.

---

## [Unreleased] — Modernization restart (2026-05)

### 🟢 Build & infrastructure

| Date | Commit | Change |
|------|--------|--------|
| 2026-05-17 | `d68f795` | Align JDT compliance with Java 8 (PR [#32](https://github.com/Mika3578/habitv/pull/32)) |
| 2026-05-17 | `a659383` | Standardize static repository workspace layout (PR [#34](https://github.com/Mika3578/habitv/pull/34)) |
| 2026-05-17 | `f30773c` | Align own-version plugin internal dependencies (PR [#33](https://github.com/Mika3578/habitv/pull/33)) |
| 2026-05-17 | `37dfa9a` | Stabilize console reactor packaging |
| 2026-05-16 | `47b1bb6` | Stabilize Maven reactor from master (PR [#22](https://github.com/Mika3578/habitv/pull/22)) |
| 2026-05-16 | `fab2d24` | Stabilize Java 8 compile baseline |
| 2026-05-16 | `b42fa49` | Include `plugin-tester` in reactor |
| 2026-05-16 | `8caab37` | Align `plugin-tester` dependency versions |
| 2026-05-16 | `e4d7e81` | Add Java 8 baseline validation workflow (CI) |

### ✨ Features

| Date | Commit | Change |
|------|--------|--------|
| 2026-05-18 | TBD | Migrate YouTube plugin downloader defaults to yt-dlp |
| 2026-05-17 | `7a61dd6` | Add runnable yt-dlp runtime path for `consoleView` |

### 🧪 Tests

| Date | Commit | Change |
|------|--------|--------|
| 2026-05-18 | TBD | Expand offline yt-dlp defaults and command wiring tests |
| 2026-05-17 | `0163d9a` | Cover yt-dlp command wiring (offline test) |

### 📚 Documentation

| Date | Commit | Change |
|------|--------|--------|
| 2026-05-18 | TBD | Document yt-dlp CLI compatibility and tracker progress |
| 2026-05-17 | `750640d` | Update modernization status after console baseline |
| 2026-05-16 | `be5b667` | Update plugin tester dependency tracker |
| 2026-05-16 | `993f3d0` | Add AI agent repository instructions |
| 2026-05-16 | `74a51ed` | Add development tracker and risk register |
| 2026-05-16 | `b7f51b8` | Add Habitv restart governance |

### 🎯 Scope summary

What this restart phase **delivers** so far:

- ✅ A walkable Maven multi-module reactor (33 modules build with `mvn validate` / `mvn compile`)
- ✅ Reproducible Java 8 baseline on Ubuntu and Windows in CI
- ✅ A runnable console fat-jar with a yt-dlp runtime path
- ✅ Offline test coverage for the YouTube command wiring
- ✅ Governance scaffolding (AI agent rules, PR/issue templates, ADR-driven tracker)

What it intentionally **does not yet change** (tracked separately):

- ⬜ Legacy URLs (`dabiboo.free.fr`, Assembla SVN, FTP) — still present
- ⬜ JavaFX 2.x / `${jdk.home}` packaging — `habiTv-linux` / `habiTv-windows` excluded from reactor
- 🟡 `youtube-dl` → `yt-dlp` plugin binary migration (in progress; see `ytdlp-migration`)
- ⬜ Provider plugin inventory / dead-endpoint cleanup
- ⬜ Hardcoded YouTube Data API key (PR [#29](https://github.com/Mika3578/habitv/pull/29) in flight)

---

## [4.1.0] — 2017-06-05

Last published release before the long dormancy.

### ✨ Highlights

- Email export plugin (POP3/IMAP receiver, SMTP sender).
- YouTube provider migrated to the YouTube Data API v3.
- `6play` provider added.

### 🐛 Fixes

- `c6e30b3` — HabiTV 4.1.0: email + youtube API + 6play + various fixes.
- `791d186` — fix arte.
- `e28410d` — fix pluzz plugin main archive (404).
- `b00e883` — fix ffmpeg.
- `25bd449` — fix refresh failed download.
- `62a346d` — fix beinsport.
- `37d5811` — youtube mp3.

### 🧹 Maintenance

- `7afecce` — versioning.
- `7f16c2b` — bump to 4.1.0.

---

## [4.1.0-post] — 2017-06 → 2018-04 (snapshot drift)

Post-release plugin-level fixes never published as a new version,
folded into the long `4.1.0-SNAPSHOT` line.

| Date | Commit | Plugin / Area |
|------|--------|---------------|
| 2018-04-21 | `ed736d8` | `footyroom` fix |
| 2018-02-17 | `024d82b` | `footyroom` initial |
| 2018-01-29 | `d170fd0` | `globalnews` |

---

## [4.0.x] — 2015-09 → 2017-02

Selected highlights from the `4.0.x` line before the `4.1.0` release.

### 🐛 Fixes
- Multiple `beinsport` fixes (2016-10 → 2017-02).
- `arte` parser fixes (2017-01-08).
- `sfr` provider (2016-09 → 2017-01).
- `wat` + Canal+ corrections and updater fix (2016-04-23).
- `pluzz` quality improvement (2016-05-22).

### 📦 Releases
- 2015-09-09 — release `beinsport-4.0.9` (last `maven-release-plugin` cut).

---

## Notation

| Symbol | Meaning |
|--------|---------|
| ✅ | Delivered and validated |
| ⬜ | Planned but not started |
| 🟡 | In progress |
| ⛔ | Blocked on another item |
| 🟢 | Mitigated risk |

[Unreleased]: https://github.com/Mika3578/habitv/compare/4.1.0...develop
[4.1.0]: https://github.com/Mika3578/habitv/tree/7f16c2b

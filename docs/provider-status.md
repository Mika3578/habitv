# Provider status for Community Alpha

Alpha-oriented view of plugin modules. Detailed evidence lives in
[`provider-inventory.md`](provider-inventory.md) (HBTV-006). **No provider code is removed**
in the alpha documentation track.

**Legend**

| Alpha class | Meaning |
|-------------|---------|
| **Testable for alpha** | Reasonable first target for community testers; offline tests and/or recent maintenance |
| **Present but unverified** | Shipped in reactor; live compatibility not confirmed offline |
| **Known broken / obsolete** | Legacy endpoints or branding; rewrite or deprecation expected |
| **Out of scope for alpha** | Infrastructure, not a broadcaster provider |

---

## Testable for alpha

| Plugin id | Module | Notes |
|-----------|--------|-------|
| `youtube` | `plugins/youtube` | yt-dlp binary contract; offline tests; needs local `yt-dlp` / `yt-dlp.exe` |
| `francetv` | `plugins/francetv` | Mobile API + yt-dlp delegation; replaces legacy `pluzz` module |
| `RSS` | `plugins/RSS` | Generic RSS / manual URL flows; depends on feed content |
| `file` | `plugins/file` | Local file-driven input; no remote endpoint |
| `arte` | `plugins/arte` | Offline fixture baseline; **live** site may fail (parser drift) — report both outcomes |

---

## Present but unverified

| Plugin id | Module | Notes |
|-----------|--------|-------|
| `globalnews` | `plugins/globalnews` | HTTPS URLs; live-only test evidence |
| `mlssoccer` | `plugins/mlssoccer` | HTTPS URLs; compatibility not validated offline |
| `sfr` | `plugins/sfr` | `sport.sfr.fr` API path; live-network tests only |
| `email` | `plugins/email` | Mailbox integration; environment-dependent |
| `footyroom` | `plugins/footyroom` | Legacy site URLs; rewrite likely needed |
| `lequipe` | `plugins/lequipe` | HTML scraping; historical stream stack references |

---

## Known broken / obsolete

| Plugin id | Module | Notes |
|-----------|--------|-------|
| `canalPlus` | `plugins/canalPlus` | Canal+ legacy services; includes embedded **D8** / **D17** sub-providers |
| `6play` | `plugins/6play` | Legacy HTTP `6play.fr` / M6-style markup; SPA site not supported |
| `wat` | `plugins/wat` | TF1 / WAT-era URLs (README “tf1”) |
| `beinsport` | `plugins/beinsport` | Legacy beinsports.com / Dailymotion mapping |
| `clubic` | `plugins/clubic` | Legacy Clubic video pages |

### Historical names (no standalone module)

| Name | Status |
|------|--------|
| `D8`, `D17` | Embedded in `canalPlus`; obsolete with canal family |
| `pluzz` | Renamed to `francetv`; update grab-config plugin ids |
| `nrj12` / `NRJ12` | Documented historically; **no module** in reactor |
| `tf1` | README alias; implementation is `wat` plugin |

---

## Out of scope for alpha

Downloader / exporter / tooling plugins (validate via binary wiring, not replay providers):

| Plugin id | Role |
|-----------|------|
| `adobeHDS`, `aria2`, `rtmpDump` | Downloaders |
| `cmd`, `curl`, `ffmpeg` | Exporters / command wrappers |
| `plugin-tester` | Test harness |
| `plugins` (aggregator) | Maven POM only |

---

## Alpha testing guidance

1. Start with **Testable for alpha** providers and document Java + launch method.
2. Do **not** treat **Known broken / obsolete** failures as regressions without a rewrite PR.
3. For **Present but unverified**, file reports with logs even if behaviour is inconclusive.
4. Use [`community-alpha-release.md`](community-alpha-release.md) for Windows launch and log paths.

---

## Maintainer cross-reference

| Doc | Purpose |
|-----|---------|
| [`provider-inventory.md`](provider-inventory.md) | Full module table, fixture policy, live-test profile |
| [`release-checklist.md`](release-checklist.md) | Pre-release verification |
| [`dev-tracker.md`](dev-tracker.md) | `provider-inventory`, `ytdlp-migration`, `javafx-modernization` |

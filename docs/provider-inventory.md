# 🔌 Provider and plugin inventory (HBTV-006)

**Tracker item**: `provider-inventory` (`HBTV-006`)  
**Status:** in progress (~55%) — inventory and offline fixtures; rewrites are
separate PRs per module.

---

## Current status and future work

| Area | Status | Next steps |
|------|--------|------------|
| Reactor / build | All listed modules compile in the Maven reactor | Keep `mvn validate` green |
| `francetv` (ex Pluzz) | **Keep** — France.tv mobile API + yt-dlp download | Migrate user grab-config `pluzz` → `francetv` |
| `youtube` | **Keep** — yt-dlp binary contract (see `ytdlp-migration`) | Publish `yt-dlp` tool zip to `habitv-repo` |
| `arte`, `6play`, `lequipe`, … | **Needs rewrite** or live drift | Fixture-first parser PRs |
| `canalPlus` (+ embedded D8/CStar) | **Obsolete** Canal-era endpoints | Dedicated canal-family rewrite |
| `wat`, `beinsport`, `clubic`, `footyroom` | **Obsolete** branding/URLs | Deprecation or rewrite PRs |
| `nrj12` | **Not in reactor** | Historical README name only |
| Live `*PluginManagerTest` | Quarantined (`-Plive-provider-tests`) | Replace with offline fixtures over time |

**Legacy naming (docs and old configs):**

- **Pluzz** → treat as **France Télévisions / France.tv**; module is
  `plugins/francetv`.
- **D8 / CStar** (CStar is the rebranded D17) → legacy Canal+ channel plugins
  inside `plugins/canalPlus`, not standalone modules.
- **6play** → legacy M6 branding; module `plugins/6play` targets old `6play.fr`
  (modern M6+ replay is a future rewrite target).

Unsupported or obsolete providers stay **documented** here until a dedicated
deprecation PR removes them (tracker + risk register), never silently dropped.

---

## Inventory scope (baseline PR)

Documentation, classification, and offline fixture baseline — no module removal,
no provider rewrite, no runtime behavior change in inventory-only work.

---

## Inventory method

- Source of truth for modules: `plugins/pom.xml` aggregator.
- Classification evidence pulled from:
  - plugin module `pom.xml` (`artifactId`)
  - plugin source interfaces and URL constants under `plugins/*/src`
  - plugin tests under `plugins/*/test`
  - existing documentation references (`README.md`, `docs/dev-tracker.md`, `docs/risk-register.md`)
- Runtime update behavior is intentionally unchanged (remains opt-in by flag).

---

## Module inventory

| Module path | Maven artifactId | Plugin type | Current status | Evidence | Recommended follow-up PR |
|---|---|---|---|---|---|
| `plugins/pom.xml` | `plugins` | utility | infrastructure-only | Aggregator defines all 23 plugin modules and no runtime logic | keep as-is |
| `plugins/6play` | `6play` | provider | needs live endpoint rewrite | `SixPlayPluginManager` is a provider; `SixPlayConf.HOME_URL` points to legacy HTTP `6play.fr`; live parser stability unknown | add fixture tests |
| `plugins/RSS` | `RSS` | provider | keep | `RSSPluginManager` implements provider; test class exists; references generic RSS templates | add fixture tests |
| `plugins/adobeHDS` | `adobeHDS` | downloader | keep | `AdobeHDSPluginDownloader` implements downloader/proxy interfaces; updater-style binary wrapper | keep as-is |
| `plugins/aria2` | `aria2` | downloader | keep | `Aria2PluginDownloader` wraps `aria2c`; dedicated test exists | keep as-is |
| `plugins/arte` | `arte` | provider | needs live endpoint rewrite | `ArteConf` uses legacy HTTP guide/rss URLs and scraping selectors; provider tests are live-network style | add fixture tests |
| `plugins/beinsport` | `beinsport` | provider | obsolete endpoint | `BeinSportConf` uses legacy `beinsports.com/us/videos` and Dailymotion mapping; known candidate in tracker notes | rewrite provider |
| `plugins/canalPlus` | `canalPlus` | provider | obsolete endpoint | `CanalPlusConf`/`D8Conf`/`CStarConf` use old Canal service URLs and channel-specific legacy endpoints | rewrite provider |
| `plugins/clubic` | `clubic` | provider | obsolete endpoint | `ClubicConf` targets legacy Clubic video pages via HTML selectors; provider test is live-network | deprecate provider |
| `plugins/cmd` | `cmd` | exporter | infrastructure-only | `CmdPluginExporterManager` and `CmdPluginDownloaderManager` are command wrappers, no provider endpoint logic | keep as-is |
| `plugins/curl` | `curl` | exporter | infrastructure-only | `CurlPluginExporterManager` plus downloader wrapper; utility integration layer | keep as-is |
| `plugins/email` | `email` | provider | unknown / needs fixture | `EmailPluginManager` is provider-based input channel (not broadcaster endpoint); tests exist but behavior depends on external mailbox integration | add fixture tests |
| `plugins/ffmpeg` | `ffmpeg` | exporter | infrastructure-only | `FFMPEGPluginExporterManager` and downloader classes are local tool wrappers | keep as-is |
| `plugins/file` | `file` | utility | keep | `FilePluginManager` is local file-driven provider/downloader bridge; no remote endpoint dependency | keep as-is |
| `plugins/footyroom` | `footyroom` | provider | needs live endpoint rewrite | `FootyroomConf` points to legacy site URLs and hardcoded host patterns in provider manager | rewrite provider |
| `plugins/globalnews` | `globalnews` | provider | unknown / needs fixture | `GlobalNewsPluginManager` provider/downloader interface with HTTPS source URL; only live-style test evidence | add fixture tests |
| `plugins/lequipe` | `lequipe` | provider | needs live endpoint rewrite | Provider/downloader plugin uses HTML scraping; tests include historical Kewego stream-init references | rewrite provider |
| `plugins/mlssoccer` | `mlssoccer` | provider | unknown / needs fixture | `MLSSoccerPluginManager` provider/downloader with HTTPS URLs; live endpoint compatibility not validated offline | add fixture tests |
| `plugins/plugin-tester` | `plugin-tester` | test harness | infrastructure-only | `BasePluginProviderTester` / `BasePluginUpdateTester` provide shared live-style harness utilities | keep as-is |
| `plugins/francetv` | `francetv` | provider | keep | `FranceTvPluginManager` queries `api-mobile.yatta.francetv.fr` catalogue; download delegated to `youtube` (yt-dlp); offline `FranceTvUrlsTest` + live `FranceTvPluginManagerTest`; replaces former `plugins/pluzz` (legacy `pluzz.` URLs still recognised by `canDownload`, but existing grab-config entries still need the plugin id renamed to `francetv`) | keep |
| `plugins/rtmpDump` | `rtmpDump` | downloader | keep | `RtmpDumpPluginDownloader` is binary wrapper with updater version pattern; dedicated test exists | keep as-is |
| `plugins/sfr` | `sfr` | provider | unknown / needs fixture | `SFRConf` uses `sport.sfr.fr` API path; provider tests are live-network style only | add fixture tests |
| `plugins/wat` | `wat` | provider | obsolete endpoint | `WatConf` points to TF1/WAT-era URLs; plugin naming and endpoint model reflect legacy provider branding | rewrite provider |
| `plugins/youtube` | `youtube` | provider | keep | `YoutubePluginManager` provider; offline tests; binary contract migrated to yt-dlp (`YtDlpCmdExecutor`, defaults `yt-dlp` / `yt-dlp.exe`) | keep (yt-dlp binary) |

---

## Historical and missing references

| Name in docs/config | Found in code/docs | In plugins aggregator? | Classification |
|---|---|---|---|
| `D8` | `README.md` provider list; `D8PluginManager` inside `plugins/canalPlus` | No standalone module | embedded legacy sub-provider in `canalPlus` |
| `CStar` (ex `D17`) | `README.md` provider list; `CStarPluginManager` inside `plugins/canalPlus` | No standalone module | embedded legacy sub-provider in `canalPlus` (renamed from `D17` when the channel rebranded to CStar; new home URL `https://www.canalplus.com/chaines/cstar`) |
| `NRJ12` / `nrj12` | Mentioned in `README.md` and tracker notes | No | historical reference only (missing module) |
| `FranceTV / Pluzz` | `plugins/francetv` module replaces former `plugins/pluzz`; mobile catalogue + yt-dlp flow | Yes (`francetv`) | rename completed (PR #58); existing grab-config entries still require manual `pluzz` → `francetv` migration |
| `Kewego` | Legacy stream references in `plugins/lequipe/test/TestInitStream.java`; risk register mentions kewego in live tests | No dedicated module | historical endpoint dependency in tests |

---

## Offline fixture policy baseline

### What counts as an offline fixture

- A **small static test asset** (JSON, XML, minimal HTML fragment, or
  structured metadata text) committed under module test resources.
- Fixture content must be **sanitized and deterministic**:
  - no secrets/tokens/cookies,
  - no generated logs,
  - no large copyrighted full-page dumps.
- Tests using fixtures must validate parser assumptions, URL extraction
  rules, or category mapping **without any live HTTP calls**.

### Fixture location convention

- Use this path pattern for provider modules:
  - `plugins/<provider>/test/resources/fixtures/<provider>/`
  - Fixture directory names are **normalized to lowercase** regardless of module casing
    (e.g. the `canalPlus` module uses `fixtures/canalplus/`).
- Keep fixtures short and purpose-specific (one behavior per fixture).
- Add a small `fixture-baseline.txt` metadata file first when parser
  fixtures are not yet stable.

### Default test behavior (no live network)

- New baseline tests must read only local fixture files under
  `test/resources/...`.
- Live integration tests should remain opt-in/quarantined and must not be
  required by default validation (`mvn -DskipTests validate/compile`).
- Any new test in this item must be Java 8 compatible and deterministic.

### Priority providers for fixture capture

| Provider | Baseline status in this PR | Why first |
|---|---|---|
| `6play` | Local fixture metadata + offline baseline test | Legacy scraper targets static markup while current site is SPA-driven |
| `canalPlus` (`D8`/`CStar` family) | Local fixture metadata + offline baseline test | Multiple legacy endpoint families, highest rewrite risk |
| `francetv` (ex `pluzz`) | Local fixture metadata + offline `FranceTvUrlsTest` covering URL/slug builders | Replacement of the legacy `pluzz` module against `api-mobile.yatta.francetv.fr` |
| `arte` | Local fixture metadata + offline baseline test | Legacy HTTP/RSS parsing assumptions need stable parser anchors |
| `youtube` | Local fixture metadata + offline baseline test | Binary contract migrated to yt-dlp; live provider validation still separate |

### Infrastructure-only modules not suitable for provider fixtures

- `adobeHDS`, `aria2`, `rtmpDump` (downloaders)
- `cmd`, `curl`, `ffmpeg` (exporter/downloader wrappers)
- `plugin-tester` (test harness)
- `plugins/pom.xml` (aggregator)

These modules are validated via command wiring or tool-wrapper tests, not
provider endpoint fixtures.

---

## Initial offline fixture baseline artifacts

- `plugins/6play/test/resources/fixtures/6play/fixture-baseline.txt`
- `plugins/canalPlus/test/resources/fixtures/canalplus/fixture-baseline.txt`
- `plugins/francetv/test/resources/fixtures/francetv/fixture-baseline.txt`
- `plugins/arte/test/resources/fixtures/arte/fixture-baseline.txt`
- `plugins/youtube/test/resources/fixtures/youtube/fixture-baseline.txt`

Baseline tests added (local fixture loading only):

- `SixPlayOfflineFixtureBaselineTest`
- `CanalPlusOfflineFixtureBaselineTest`
- `FranceTvOfflineFixtureBaselineTest` (+ `FranceTvUrlsTest` unit coverage)
- `ArteOfflineFixtureBaselineTest`
- `YoutubeOfflineFixtureBaselineTest`

---

## Live provider tests vs default Maven lifecycle

Root `pom.xml` excludes `**/*PluginManagerTest.java` and live mailbox
`MessageReceiverTest.java` from default Surefire runs.
These tests call live broadcaster endpoints via `BasePluginProviderTester` and
are opt-in only:

```bash
mvn -B -ntp test -Plive-provider-tests
```

Offline fixture baseline tests (for example `ArteOfflineFixtureBaselineTest`,
`SixPlayOfflineFixtureBaselineTest`) remain in the default `mvn test` lifecycle.

### Arte live provider drift (2026-05)

`ArtePluginManagerTest` currently fails with `categorie liste vide` because the
live Arte site no longer returns categories through the legacy parser. This is
provider/runtime drift, not a static-repository deploy regression.

- Tracked for repair in a dedicated provider PR (endpoint/parser rewrite).
- Must not block `mvn deploy -Pstatic-repo-deploy` or default `mvn test`.
- Reproduce with: `mvn -B -ntp test -Plive-provider-tests -pl plugins/arte -am`

---

## Follow-up PR queue (safe order)

1. **test(provider-inventory): add offline fixtures for unknown providers**
   - Scope: `globalnews`, `mlssoccer`, `sfr`, `email`, plus RSS regression fixtures.
2. **fix(provider-canal-family): rewrite canalPlus + d8 + cstar provider endpoints**
   - Scope: `plugins/canalPlus` only, with fixture-backed parser behavior.
3. ✅ **fix(provider-francetv): replace pluzz provider with france.tv metadata flow** — delivered in PR #58 (`plugins/francetv`).
4. **fix(provider-legacy-football): rewrite wat, beinsport, footyroom, lequipe**
   - Scope: endpoint/parser modernization with offline fixtures first.
5. **docs(provider-cleanup): propose dedicated deprecation PRs for non-recoverable providers**
   - Scope: doc + tracker + risk updates only, no silent removals.

---

## Out-of-scope confirmations

- No plugin modules were removed.
- No provider logic was rewritten.
- No runtime updater behavior was changed.
- yt-dlp binary migration is tracked under `ytdlp-migration` (separate PRs).
- No JavaFX modernization work started in this inventory PR.
- No Maven publication layout or `habitv-repo` contract was changed.

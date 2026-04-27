# Plugin Provider Inventory Audit (2026-04-27)

Scope: documentation-only audit of modules listed in `plugins/pom.xml`.  
No plugin deletion, parser rewrite, updater behavior change, or build/deploy policy change is included.

## Inventory

| module | type | current upstream/service | status | runtime risk | default build impact | recommendation | evidence/source notes | follow-up task |
|---|---|---|---|---|---|---|---|---|
| 6play | provider-renamed | 6play moved to M6+ (`m6.fr`) | legacy provider name; parser likely stale | high (brand/platform shift) | medium (live test can fail) | keep module for now; plan replacement as `m6plus` | `plugins/6play/src/.../SixPlayConf.java` uses `https://www.m6.fr`; `https://www.6play.fr` redirects to `https://www.m6.fr/` | replace provider (`6play` -> `m6plus`) |
| plugin-tester | test-only/support | shared test harness | active support module | low | low | keep as test utility | `BasePluginProviderTester` performs live category/episode fetching loops | keep and use for test isolation migration |
| adobeHDS | tool-wrapper | Adobe HDS helper script | legacy/low-priority | medium (format/protocol aging) | low | keep pending relevance review | module is downloader/helper, not site provider | review relevance before any deprecation |
| aria2 | tool-wrapper | aria2 downloader wrapper | active | low | low | keep | update-style tests only (`Aria2PluginDownloaderTest`) | none |
| arte | provider-active | arte.tv | active but parser health uncertain | high (legacy RSS endpoint in code) | medium (live test non-deterministic) | keep; audit parser compatibility | `ArteConf` still references `http://videos.arte.tv/...rss.xml` | parser repair PR or mark deprecated if broken |
| beinsport | provider-active | beIN Sports FR | likely broken/blocked | high (site blocks + old assumptions) | medium (live test may fail) | keep for now; prioritize parser repair/deprecation decision | `https://www.beinsports.com/fr-fr/videos` returns HTTP 403 to automated request; test extends live harness | repair parser or deprecate |
| canalPlus | provider-unknown | myCanal/Canal+ APIs | uncertain; likely auth/API drift | high (token/API endpoints legacy) | medium/high (live tests) | keep; classify as private-access/provider-unknown until validated | `CanalPlusConf` uses `http://service.mycanal.fr/...token...` and `service.canal-plus.com` | dedicated access + parser audit |
| clubic | provider-unknown | clubic.com/video | unknown current usefulness | medium | medium | keep pending runtime verification | `ClubicConf` URLs are HTTPS but provider parser age unknown | runtime/parser health audit |
| cmd | tool-wrapper | local command execution helper | active | low | low | keep | command wrapper module, not provider | none |
| curl | tool-wrapper | curl wrapper | active | low | low | keep | downloader wrapper + update test | none |
| email | tool-wrapper | POP3/IMAP receiver | active but network-credential dependent | medium (external auth required) | low/medium (tests non-deterministic) | keep; move live mailbox tests behind profile | tests include hardcoded mailbox credentials and live server calls | isolate email live tests under `-Pnetwork-tests` |
| ffmpeg | tool-wrapper | ffmpeg wrapper | active | low | low | keep | downloader wrapper module | none |
| file | tool-wrapper | local file downloader/reader | active | low | low | keep | local file-oriented test exists (`FilePluginManagerTest`) | none |
| footyroom | provider-unknown | footyroom (`footyroom.com` -> `footyroom.co`) | unknown parser state | medium/high (domain redirect) | medium | keep pending relevance review | `FootyroomConf` still uses `http://footyroom.com`; curl resolves to `https://footyroom.co/` | audit + update parser/domain mapping |
| globalnews | provider-unknown | globalnews.ca videos | unknown parser state | medium | medium | keep pending relevance review | `GlobalNewsConf` points to `https://globalnews.ca/national/videos/`; redirects to `/videos/` | parser health audit |
| lequipe | provider-unknown | l'Equipe video / possible TF1+ integration | unknown; likely changed platform | high (legacy endpoint + ecosystem move) | medium | keep; perform parser/access audit | `LEquipeConf` uses `http://video.lequipe.fr/morevideos` which redirects to `https://www.lequipe.fr/tv/` | parser repair or provider strategy update |
| mlssoccer | provider-unknown | mlssoccer.com highlights | unknown parser state | medium | medium | keep pending parser health validation | `MLSSoccerConf` uses `https://www.mlssoccer.com` | parser health audit |
| pluzz | provider-renamed | pluzz replaced by france.tv | legacy provider with old API endpoints | high (endpoint host unavailable) | medium (live test likely fails) | do not delete now; plan replacement by `france.tv` provider | `PluzzConf` has dead `pluzz.webservices.francetelevisions.fr` and old webservices URLs; base URL already `https://www.france.tv` | replace provider (`pluzz` -> `france.tv`) |
| RSS | provider-active | generic RSS feeds | active generic ingestion | medium (feed volatility) | medium (depends on external feeds) | keep; classify as active with network variance | `RSSPluginManager` templates include external feeds (e.g., Dailymotion RSS) | keep and gate network-dependent tests if flaky |
| rtmpDump | tool-wrapper | rtmpdump wrapper | legacy but still wrapper | medium (RTMP aging) | low | keep pending relevance review | downloader wrapper module; protocol considered legacy | relevance review before deprecation |
| sfr | provider-unknown | SFR Sport API/content | private-access/non-public behavior likely | high (DNS/access/auth variability) | medium/high (live test instability) | keep only if still needed; treat as private-access | `SFRConf` targets `https://sport.sfr.fr/...`; direct host resolution unstable from audit environment | decide keep vs deprecate after operator-access validation |
| youtube | provider-active | youtube-dl based extractor for multiple hosts | active but tooling outdated | medium/high (youtube-dl drift) | medium | keep now; migrate tooling later | `YoutubePluginDownloader` and `YoutubeConf` reference `youtube-dl` binaries; supports youtube/dailymotion/vimeo/tf1/wat/6play inputs | modernize downloader (`youtube-dl` -> `yt-dlp`) in follow-up |
| wat | provider-obsolete | wat.tv / legacy TF1 ecosystem | obsolete | high (service closed/renamed) | medium (live test likely non-deterministic) | keep for deprecation window only; no functional investment | WAT service closed (2016); `https://www.wat.tv` now redirects to `https://www.tf1.fr/`; module still named `wat` | deprecate then remove `wat` |

## URL and Auth Notes

- HTTP endpoints still embedded in provider code: `pluzz`, `arte` (RSS API), `canalPlus` family (`service.mycanal.fr`, `service.canal-plus.com`, d8/d17), `lequipe` (`morevideos`), `footyroom`.
- Verified redirects during audit:
  - `https://www.6play.fr` -> `https://www.m6.fr/`
  - `http://footyroom.com` -> `https://footyroom.co/`
  - `http://video.lequipe.fr/morevideos` -> `https://www.lequipe.fr/tv/`
  - `https://www.wat.tv` -> `https://www.tf1.fr/`
  - `https://globalnews.ca/national/videos/` -> `https://globalnews.ca/videos/`
- Suspected/de-facto auth or access restrictions:
  - `canalPlus` uses tokenized API calls.
  - `sfr` likely requires operator/customer context and may be non-public.
  - `beinsport` can return 403 to automated traffic.

## Test Classification (Plugin Scope)

### Deterministic (unit/support, suitable for default lifecycle)

- Updater/wrapper style tests extending `BasePluginUpdateTester` (e.g., `aria2`, `curl`, `ffmpeg`, `rtmpDump`, youtube updater tests).
- Local file-based checks (`plugins/file/.../FilePluginManagerTest.java` partially local).

### Live provider website dependent (non-deterministic, should become `*IT.java` under `-Pnetwork-tests`)

- All tests extending `BasePluginProviderTester` for provider modules and network providers:
  - `arte`, `beinsport`, `canalPlus` (`CanalPlus`, `D8`, `D17`), `clubic`, `footyroom`, `globalnews`, `lequipe`, `mlssoccer`, `pluzz`, `sfr`, `wat`, `youtube`, `RSS`, plus `email`/`file` provider-style tests that perform external interactions.

### Known migration target

- Rename live-site tests to `*IT.java` and execute only via `mvn -B -ntp -Pnetwork-tests verify`.
- Keep default Surefire lifecycle limited to deterministic fixture/mock/unit tests.

## Follow-up Backlog Groups

- Remove obsolete plugins after deprecation window:
  - `wat`
- Rename/replace provider plugins:
  - `pluzz` -> `france.tv`
  - `6play` -> `m6plus`
- Repair active provider parsers:
  - `arte`
  - `beinsport`
  - `canalPlus`
  - `lequipe`
  - `sfr` (if still useful)
- Modernize downloader tooling:
  - `youtube` -> `yt-dlp`
- Keep tool wrappers:
  - `cmd`
  - `curl`
  - `ffmpeg`
  - `file`
  - `email`
  - `aria2`
  - `rtmpDump` (pending relevance review)
  - `adobeHDS` (pending relevance review)

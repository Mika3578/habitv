# URL Inventory (HTTP/Provider Audit)

This inventory tracks hardcoded URLs found during the HTTPS modernization audit. It focuses on provider/public URLs, smoke-test URLs, Maven repository URLs, and historical SCM metadata.

| File path | Current URL (after this PR) | Replacement URL | Category | Status | Notes |
|---|---|---|---|---|---|
| `fwk/framework/test/com/dabi/habitv/framework/plugin/utils/RetrieverUtilsNetworkIT.java` | `https://example.com/` | N/A | test-smoke-url | updated | Generic framework smoke test now uses a stable public test URL; provider-specific URLs are excluded from this test. |
| `plugins/beinsport/src/com/dabi/habitv/provider/beinsport/BeinSportConf.java` | `https://www.beinsports.com/fr-fr/videos` | N/A | production-provider-url | updated | Updated public landing URL to HTTPS beIN Sports France videos page. |
| `plugins/beinsport/src/com/dabi/habitv/provider/beinsport/BeinSportConf.java` | `https://www.beinsports.com` | N/A | production-provider-url | updated | HTTPS upgrade for provider home URL. |
| `plugins/pluzz/src/com/dabi/habitv/provider/pluzz/PluzzConf.java` | `https://www.france.tv` | N/A | production-provider-url | updated | Public/base reference modernized; legacy webservice endpoints remain unchanged in this PR. |
| `plugins/pluzz/src/com/dabi/habitv/provider/pluzz/PluzzConf.java` | `http://pluzz.webservices.francetelevisions.fr/pluzz/liste/type/replay/nb/10000/chaine` | `https://www.france.tv/` (public reference only) | production-provider-url | needs-dedicated-provider-rewrite | Legacy Pluzz API endpoint likely obsolete and tied to old parser behavior. |
| `plugins/pluzz/src/com/dabi/habitv/provider/pluzz/PluzzConf.java` | `http://webservices.francetelevisions.fr/tools/getInfosOeuvre/v2/?idDiffusion=%s&catalogue=Pluzz&callback=webserviceCallback_%s` | `https://www.france.tv/` (public reference only) | production-provider-url | needs-dedicated-provider-rewrite | Not switched blindly to avoid plugin rewrite in this PR. |
| `plugins/arte/src/com/dabi/habitv/provider/arte/ArteConf.java` | `https://www.arte.tv` | N/A | production-provider-url | updated | Updated public ARTE URL to current HTTPS host; language prefix (`/fr`) is carried by page-provided hrefs. |
| `plugins/arte/src/com/dabi/habitv/provider/arte/ArteConf.java` | `http://videos.arte.tv/fr/do_delegate/videos/programmes/#ID_EMISSION#,view,rss.xml` | `https://www.arte.tv/fr/` (public reference only) | production-provider-url | needs-dedicated-provider-rewrite | Legacy feed endpoint remains; API behavior needs dedicated rewrite validation. |
| `plugins/wat/src/com/dabi/habitv/provider/wat/WatConf.java` | `https://www.tf1.fr` | N/A | production-provider-url | updated | Updated TF1 public URLs to HTTPS. |
| `plugins/6play/src/com/dabi/habitv/provider/sixplay/SixPlayConf.java` | `https://www.m6.fr` | N/A | production-provider-url | updated | Updated legacy 6play host reference to M6+ public URL. |
| `plugins/6play/test/com/dabi/habitv/provider/sixplay/SixPlayPluginManagerIT.java` | N/A | N/A | test-provider-network | isolated-opt-in | 6play live provider smoke test; Failsafe-only (`network-tests`). |
| `plugins/**/test/**/*PluginManagerIT.java` (see `docs/testing.md`) | N/A | N/A | test-provider-network | isolated-opt-in | Live provider smoke tests (beIN, ARTE, Canal+/D8/D17, Pluzz, SFR, YouTube, WAT, 6play, Clubic, GlobalNews, Footyroom, L'Équipe, MLS Soccer, etc.); not part of default Surefire. **Failures under `-Pnetwork-tests` are opt-in volatility** (empty category lists, 403, network down). |
| `plugins/clubic/src/com/dabi/habitv/provider/clubic/ClubicConf.java` | `https://www.clubic.com` | N/A | production-provider-url | updated | Updated Clubic public URLs to HTTPS. |
| `plugins/lequipe/src/com/dabi/habitv/provider/lequipe/LEquipeConf.java` | `https://video.lequipe.fr` / `http://video.lequipe.fr/morevideos` | `https://www.tf1.fr/l-equipe/videos/replay` | production-provider-url | needs-dedicated-provider-rewrite | Kept unchanged in code to avoid accidental provider behavior changes without parser rewrite/tests. |
| `plugins/canalPlus/src/com/dabi/habitv/provider/canalplus/*.java` | Legacy `http://service.canal-plus.com/...`, `http://service.mycanal.fr/...`, `http://www.d8.tv`, `http://www.d17.tv` | `https://www.canalplus.com/` or `https://www.canalplus.com/replay-gratuit/` (public references only) | production-provider-url | needs-dedicated-provider-rewrite | Endpoints are service/API-specific and channel-specific; HTTPS/public-site substitution alone is not safe. |
| `pom.xml`, `application/habiTv-*/pom.xml` | `http://dabiboo.free.fr/repository` | Deferred | maven-repository-url | blocked-by-runtime-updater-migration | Intentionally unchanged in this PR per scope guardrails. |
| `fwk/framework/src/com/dabi/habitv/framework/FrameworkConf.java` | `http://dabiboo.free.fr/repository` | Deferred | maven-repository-url | blocked-by-runtime-updater-migration | `FrameworkConf.UPDATE_URL` intentionally unchanged in this PR. |
| `application/core/src/com/dabi/habitv/core/config/HabitTvConf.java` | `http://dabiboo.free.fr/cpt.php` | Deferred | documentation-only-url | obsolete | Legacy stats endpoint; not migrated in this scoped PR. |
| Root/module `pom.xml` `<scm>` blocks | `scm:svn:http://subversion.assembla.com/svn/habitv/trunk...` | N/A | scm-historical-url | kept-historical | Preserved as historical SCM metadata. |
| `docs/modernization-build-stabilization-report-2026-04-24.md` | `http://dabiboo.free.fr/repository` | Deferred | documentation-only-url | kept-historical | Historic evidence retained; follow-up runtime updater migration needed. |
| `plugins/pluzz/pluzz.txt` | `http://www.pluzz.fr/...` | `https://www.france.tv/` (public reference only) | documentation-only-url | obsolete | Legacy notes/sample endpoints kept for historical context. |
| `application/consoleView/grabconfig.xml` | Multiple `http://www.d8.tv/program/...` IDs | `https://www.canalplus.com/replay-gratuit/` (public reference only) | documentation-only-url | obsolete | Large static sample config references legacy channel URLs; requires provider-specific migration plan. |

## Provider URL Matrix (plugin audit 2026-04-27)

| Plugin module | Base URL(s) in code | Protocol | Redirect observation (audit run) | Renamed/dead indicator | Auth/access note |
|---|---|---|---|---|---|
| `6play` | `https://www.m6.fr` | HTTPS | `https://www.6play.fr` redirects to `https://www.m6.fr/` | yes (6play -> M6+) | public web, parser likely stale |
| `arte` | `https://www.arte.tv`; `http://videos.arte.tv/...rss.xml` | mixed | legacy RSS endpoint not verified | likely legacy endpoint | public web |
| `beinsport` | `https://www.beinsports.com/fr-fr/videos` | HTTPS | URL responds `403` to automated request | no rename confirmed | anti-bot/access restrictions likely |
| `canalPlus` | `http://service.mycanal.fr/...`; `http://service.canal-plus.com/...`; `http://www.d8.tv`; `http://www.d17.tv` | HTTP | not fully validated in this audit | yes (legacy D8/D17 and old API hosts) | tokenized/private API behavior |
| `clubic` | `https://www.clubic.com/video` | HTTPS | no redirect issue observed | unknown | public web |
| `footyroom` | `http://footyroom.com` | HTTP in code | redirects to `https://footyroom.co/` | possible domain migration | public web |
| `globalnews` | `https://globalnews.ca/national/videos/` | HTTPS | redirects to `https://globalnews.ca/videos/` | likely path migration | public web |
| `lequipe` | `https://video.lequipe.fr`; `http://video.lequipe.fr/morevideos` | mixed | `http://video.lequipe.fr/morevideos` redirects to `https://www.lequipe.fr/tv/` | likely platform/path migration | public web |
| `mlssoccer` | `https://www.mlssoccer.com` | HTTPS | no redirect issue observed | unknown | public web |
| `pluzz` | `http://pluzz.webservices.francetelevisions.fr/...`; `http://webservices.francetelevisions.fr/...`; base `https://www.france.tv` | mixed | pluzz webservice host unresolved (unknown host) | yes (pluzz -> france.tv) | public web + obsolete legacy API |
| `RSS` | external feed URLs (template includes Dailymotion RSS) | mostly HTTP in template | depends on feed endpoint | provider depends on external platforms | public feeds |
| `sfr` | `https://sport.sfr.fr/...` | HTTPS | host resolution failed in audit environment | no rename confirmed | likely operator/customer access dependency |
| `wat` | `https://www.tf1.fr`; historical WAT ecosystem | HTTPS | `https://www.wat.tv` redirects to `https://www.tf1.fr/` | yes (WAT closed/absorbed) | public web, legacy plugin |
| `youtube` | downloader handles multiple hosts; binary `youtube-dl` | n/a | n/a | tooling legacy (not URL rename) | external extractor behavior |

## Validation notes

- Network/provider URLs are volatile; a successful HTTPS substitution does **not** guarantee provider plugin functionality.
- Any provider that still relies on legacy service API URLs after this PR is marked `needs-dedicated-provider-rewrite`.

- Provider-specific URLs such as beIN Sports may return HTTP 403 for automated requests and are unsuitable for generic framework smoke tests.
- Live provider tests are opt-in (`mvn -B -ntp clean -Pnetwork-tests verify`). They exercise real sites; **plugin compilation and default `mvn clean install` passing do not prove providers are still functional.**
- Under `network-tests`, expect intermittent failures: no episodes found, empty categories, HTTP 403, or offline networks — document as provider volatility, not a deterministic CI gate.

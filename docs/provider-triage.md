# Provider triage (maintainers)

Working order for French replay and shared tooling. This table **does not**
re-test providers live or claim current download support. Canonical module
grouping: [`providers.md`](providers.md#status-documentation-pass-not-a-live-re-test).

## Priority queue

| Priority | Scope | Intent |
|----------|--------|--------|
| **P0** | `francetv`, `novo19`, `arte`, `youtube` (yt-dlp) | Core French catalogues and the shared retrieval tool; keep discovery fixtures current and yt-dlp integration healthy. |
| **P1** | `6play` | M6 Group legacy module; classified “needs rewrite / investigation” in `providers.md`. |
| **P2** | `tf1plus` (new module) | **One** focused PR when scoped: no shipped `plugins/tf1plus` today; legacy TF1 listing is `wat` (obsolete). Do not split TF1+ work across parallel provider PRs. |
| **P4** | Open `fix/*` draft PRs | Diagnostic/listing drafts for degraded modules; finish or close before starting duplicate scope. |

There is no separate **P3** band in the current roadmap; backlog items below P2 stay
in the draft queue or issue tracker until reprioritized.

### P0 detail (from status doc)

| Module | Maintainer focus |
|--------|------------------|
| `francetv` | Public hub / API discovery; retrieval often delegates to yt-dlp when public URLs exist. |
| `novo19` | Catalogue provider; same delegation pattern where applicable. |
| `arte` | EMAC catalogue in HabiTV; stream extraction via yt-dlp on public Arte URLs (`providers.md`). |
| `youtube` | yt-dlp binary, commands, Windows preflight — see [`ytdlp-reliability.md`](ytdlp-reliability.md). |

### P4 open `fix/*` drafts (snapshot)

| PR | Branch | Title |
|----|--------|--------|
| [#233](https://github.com/Mika3578/habitv/pull/233) | `fix/6play` | fix(6play): diagnose listing |
| [#232](https://github.com/Mika3578/habitv/pull/232) | `fix/wat` | fix(wat): diagnose listing |
| [#231](https://github.com/Mika3578/habitv/pull/231) | `fix/clubic` | fix(clubic): restore listing |
| [#230](https://github.com/Mika3578/habitv/pull/230) | `fix/lequipe` | fix(lequipe): diagnose listing |
| [#229](https://github.com/Mika3578/habitv/pull/229) | `fix/sfr` | fix(sfr): diagnose listing |
| [#227](https://github.com/Mika3578/habitv/pull/227) | `fix/globalnews` | fix(globalnews): diagnose listing |
| [#226](https://github.com/Mika3578/habitv/pull/226) | `fix/mlssoccer` | fix(mlssoccer): diagnose listing |
| [#224](https://github.com/Mika3578/habitv/pull/224) | `fix/beinsport` | fix(beinsport): diagnose listing |

Refresh this table when drafts merge or new `fix/*` PRs open.

## French catalogue gap themes (inventory only)

Themes from the French provider gap pass — **not** new work commitments:

| Theme | Notes |
|-------|--------|
| **TF1+ vs legacy TF1** | Current TF1 replay product has no dedicated module; `wat` targets legacy `tf1.fr`. `providers.md` treats Canal+ / WAT / TF1+ as protected catalogues, not free replay. |
| **NRJ12** | Documented “no module” under historical names in `providers.md`. |
| **Partner hubs on france.tv** | TV5 Monde+, France 24, INA, LCP, Sport, Franceinfo, etc. ride `francetv` public hub discovery — not separate plugins. |
| **Generic URL download** | The `youtube`/yt-dlp downloader may accept some host patterns without a catalogue tree; that is not a provider implementation. |
| **National brands without modules** | BFM, Molotov, Orange TV, Léman Bleu, RTBF Auvio, and similar names have no `plugins/*` entry in the reactor; scope only when explicitly requested. |
| **Renames and samples** | Pluzz → `francetv`; sample `grabconfig.xml` channel names may still show legacy ids (`pluzz`, `tf1`, `m6w9`). |
| **Obsolete / degraded bucket** | `canalPlus`, `cstar`, `wat`, `beinsport`, `clubic`, plus investigation modules (`lequipe`, `sfr`, …) per `providers.md` status table. |

## Related docs

- [`providers.md`](providers.md) — legal constraints, yt-dlp defaults, Arte discovery
- [`ytdlp-reliability.md`](ytdlp-reliability.md) — tool plugin testing and Windows diagnostics
- [`AGENTS.md`](../AGENTS.md) — preferred work order and public git text

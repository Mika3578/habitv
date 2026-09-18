# French replay providers (2026 research)

**Tracker item:** `french-replay-providers`

**Related:** [`provider-inventory.md`](provider-inventory.md), [`obsolescence-register.md`](obsolescence-register.md), [`provider-policy.md`](provider-policy.md)

**Last refresh:** 2026-09-18

This note maps current French catch-up platforms against Habitv plugins.
It is a research baseline for new provider work. Public catalog plus
yt-dlp download is in scope. DRM, login, and license-material bypass
are out of scope unless a dedicated maintainer-directed PR says
otherwise (root `AGENTS.md` Section 18.4).

## How the landscape is grouped

French TNT and catch-up is organised by **platform**, not by one plugin
per channel:

| Platform | Channels (replay) | Habitv module | Status |
|----------|-------------------|---------------|--------|
| france.tv | France 2, 3, 4, 5, franceinfo, La 1ere; partner hubs (Arte, TV5 Monde Plus, France 24, INA, LCP, Public Senat) | `francetv` | keep |
| Arte.tv | Arte | `arte` | needs rewrite |
| TF1+ (`tf1.fr`) | TF1, TMC, TFX, TF1 Series Films, LCI | **`tf1plus` (new)** | public catalog; download via yt-dlp (many items DRM/auth) |
| M6+ (ex 6play) | M6, W9, 6ter, Gulli | `6play` | needs rewrite to M6+ |
| Canal+ / myCanal | Canal+, C8, CStar, CNews | `canalPlus` | degraded / protected |
| NOVO19 | NOVO19 | `novo19` | public catalog + replay download |
| RMC+ (ex RMC BFM Play) | BFMTV live/full replay, RMC Story, RMC Decouverte, RMC Life | none | protected (Widevine); not implemented |
| BFMTV.com news replay | BFMTV and BFM Business clip/show replay | **`bfmtv` (new)** | public NextRadioTV catalog; download via yt-dlp |
| L'Equipe | L'Equipe | `lequipe` | needs rewrite |
| NRJ Play | Cherie 25 (NRJ12 gone) | none | account / protected; follow-up |
| WAT / MyTF1 HTML | legacy TF1 | `wat` | obsolete; do not remove until a deprecation PR |

Partner hubs already listed under france.tv are **not** duplicated as
standalone plugins in this wave.

## Priority for new plugins

Implemented in this wave (public catalog, offline fixtures, yt-dlp
delegation, graceful empty results on failure):

1. **`tf1plus`** — largest TNT gap after france.tv. Public GraphQL
   catalog on `www.tf1.fr/graphql/web`. Episode pages are public URLs
   that yt-dlp already classifies as TF1. Protected streams fail at
   download time with sanitized errors; catalog still lists metadata.
2. **`bfmtv`** — public NextRadioTV session token (anonymous, memory
   only) plus replay program/video JSON. Download uses the public
   `bfmtv.com` replay page URL via yt-dlp. Full-channel RMC+ replay
   remains out of scope.

## Follow-up (not in this PR)

| Candidate | Why not now |
|-----------|-------------|
| M6+ rewrite of `6play` | Existing module; dedicated rewrite PR, not a second plugin |
| Canal+ family rewrite | Protected / legacy endpoints; tracker already queued |
| RMC+ | Widevine; needs maintainer-directed protected-replay scope |
| `arte` rewrite | Existing module drift |
| `lequipe` rewrite | Existing module drift |
| Cherie 25 / NRJ Play | Account-gated live/replay |
| Deprecate `wat` | Keep module until a dedicated deprecation PR (no silent removal) |

## Validation notes

- Live network probes on 2026-09-18 confirmed TF1+ GraphQL category /
  program / replay-video queries and BFMTV NextRadioTV replay lists.
- Default tests are **offline fixtures only**. Live provider tests stay
  opt-in (`-Plive-provider-tests`).
- GraphQL persisted-query hashes and NextRadioTV paths can drift; treat
  that as `provider-endpoints-dead` residual risk, not a new risk class.

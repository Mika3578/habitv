# Habitv obsolescence register

Track obsolete providers, endpoints, and features deliberately. Canonical
rule: [`AGENTS.md`](../AGENTS.md) Section 18.16.

**Related inventory:** [`provider-inventory.md`](provider-inventory.md) —
current provider status table.

Do not delete legacy providers silently. Mark, replace, deprecate, or remove
in focused PRs.

**Last refresh:** 2026-05-31

## Register

| Provider / feature | Old endpoint or behavior | Replacement | Status | Removal plan | Risk | Related PR |
|--------------------|--------------------------|-------------|--------|--------------|------|------------|
| `plugins/pluzz` | Pluzz module id and branding | `plugins/francetv` | removed | Users rename grab-config plugin to `francetv` | low | #58 |
| Canal+ `D8` sub-provider | `www.d8.tv`, `service.canal-plus.com` D8 paths | C8 branding (no Habitv sub-provider) | removed | Removed from `canalPlus` module | low | #92 |
| Canal+ `D17` | Legacy D17 channel naming | CStar sub-provider in `canalPlus` | obsolete | Graceful degrade; rewrite TBD | medium | #91 |
| `plugins/wat` | TF1/WAT-era URLs | Modern TF1+ replay (not implemented) | obsolete | Dedicated rewrite or deprecate PR | medium | — |
| `plugins/beinsport` | Legacy beinsports.com video pages | Protected / platform-specific replay | obsolete | Deprecate or auth-aware rewrite | high | — |
| `plugins/clubic` | Legacy Clubic HTML video pages | N/A (site model changed) | obsolete | Deprecate provider PR | low | — |
| `youtube-dl` binary name | `youtube-dl` executable defaults | `yt-dlp` (`ytdlp-migration`) | obsolete | User config migration documented | medium | #52 |
| `dabiboo.free.fr` Maven/update host | HTTP free.fr repository and updater | `habitv-repo` GitHub Pages HTTPS | removed | `legacy-url-migration`, `static-repo-publish` | high | #36, habitv-repo #2 |
| NRJ12 module | Historical README provider name | None in reactor | removed | Document only | low | — |

### Status values

Use the provider status matrix from [`provider-policy.md`](provider-policy.md):
working, degraded, protected, obsolete, removed, unknown.

## When to add an entry

- endpoint confirmed dead or replaced;
- provider removed from active list;
- external tool replaced (see [`external-tools-recommendations.md`](external-tools-recommendations.md));
- platform DRM/auth change makes prior approach obsolete.

## When to update

- provider restored or degraded status changes;
- replacement service validated;
- removal completed — move to **removed** with PR reference.

---
applyTo: "plugins/**"
---

# Habitv plugin instructions (Copilot)

Extends `AGENTS.md` Section 18 and `docs/provider-policy.md`.

## Scope

Provider, downloader, and export plugins under `plugins/`.

## Extends AGENTS.md

- Phase declaration (18.1); status matrix (18.2); metadata contract (18.3).
- Offline-first testing (18.4); external tools in dedicated PRs (18.5).

## Validation expected

- `mvn -B -ntp -pl plugins/<module> -am test`
- offline fixtures preferred; document skipped live tests

## Do not change

- DRM bypass; cookies/tokens in repo; provider + dependency in same PR.

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

- Protected content and provider policy: Section 18.4 → `docs/provider-policy.md`
  (includes [site authentication for download](../../docs/provider-policy.md#site-authentication-for-download)).

- Provider public communication safety (20.14): high-level PR/commit text only;

  see [`docs/provider-policy.md#public-communication-safety`](../../docs/provider-policy.md#public-communication-safety).



## Validation expected



- `mvn -B -ntp -pl plugins/<module> -am test`

- offline fixtures preferred; document skipped live tests



## Do not change (without maintainer request)



- protected-replay handling in unrelated docs/CI/dependency PRs; secrets in repo.

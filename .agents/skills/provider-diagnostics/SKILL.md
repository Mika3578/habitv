---
name: provider-diagnostics
description: Diagnoses replay provider listing or retrieval failures using fixtures and safe reproduction. Use for plugin/provider bugs, parsing errors, or network/tool integration issues.
---

# Provider diagnostics

Provider scope and safety: [`docs/providers.md`](../../../docs/providers.md).
Project constraints: [`AGENTS.md`](../../../AGENTS.md).

## Workflow

1. **Reproduce safely** — prefer offline fixtures; use live network only with
   `-Plive-provider-tests` when explicitly appropriate.
2. **Identify provider/source** — one plugin module per change when possible.
3. **Collect deterministic diagnostics** — logs, fixture diffs, minimal repro
   commands. No secrets, cookies, tokens, or session data in git.
4. **Classify failure** — parsing vs network vs external tool vs access
   limits. Stay within the personal-use scope and maintainer provider
   decisions in `docs/providers.md`; keep secrets and device files out of
   git.
5. **Preserve non-blocking behavior** — avoid breaking unrelated providers.
6. **Add or update fixtures/tests** — offline proof is the default bar.
7. **Layer errors** — clear user-facing message; technical detail in logs or
   developer notes, not public git text.

## Do not

- Embed transient site selectors, credentials, or session recipes in skills or
  committed agent configuration.
- Claim a provider works without code, tests, or evidence.

Validation path: [`code-change-verification`](../code-change-verification/SKILL.md).

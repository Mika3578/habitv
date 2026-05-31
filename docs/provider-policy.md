# Habitv provider policy

Canonical source: [`AGENTS.md`](../AGENTS.md) Section 18 and Section 15.24.

Related: [`provider-inventory.md`](provider-inventory.md),
[`obsolescence-register.md`](obsolescence-register.md),
[`external-tools-recommendations.md`](external-tools-recommendations.md),
[`plugins/AGENTS.md`](../plugins/AGENTS.md).

## Project phase

Declare phase, risk, expected files, validation plan, and Java 8 impact
before provider work (Section 18.1).

## Provider status matrix

Update [`provider-inventory.md`](provider-inventory.md) and/or
[`obsolescence-register.md`](obsolescence-register.md) when status changes.

| Status | When to use |
|--------|-------------|
| working | tests + real behavior validated when feasible |
| degraded | partial function or known gaps |
| protected | auth, encryption, geoblocking, anti-bot |
| obsolete | dead/replaced endpoint |
| removed | intentionally delisted |
| unknown | not recently validated |

## Metadata quality contract

Target fields when the provider exposes them: channel, program title,
episode title, broadcast date, duration, summary, thumbnail, season/episode
numbers, replay URL, download URL or downloader command, availability end.

Document why metadata is missing. Do not claim support without validation.

## Testing

**Preferred:** offline fixtures, parser tests, URL extraction, command-building,
`mvn -B -ntp -pl plugins/<module> -am test`.

**Live checks:** supplementary only. If skipped, document reason.

## Protected content (tiered)

Full rule: `AGENTS.md` Section 18.4. Governance:
[tiered policy ADR](decision-log.md#-provider-protected-content-tiered-policy--tiered-protected-content-policy-for-providers),
[residual risk](risk-register.md#provider-protected-replay-residual--provider-protected-replay-residual-risk).

**Default (maintainer has not requested protected-replay handling):**

- public discovery plus external downloaders (yt-dlp where applicable);
- `protected` / `degraded` status for restricted replay;
- sanitized failures when download is unavailable.

**Maintainer-directed protected replay (opt-in):**

Opt-in when the maintainer requests it in the **current conversation** **or**
a **scoped provider PR** documents maintainer direction (tracker + PR scope).

- Stremio-equivalent support in a **scoped provider PR** is allowed;
- prefer external tools and user-local config; plugin helper scripts that
  contain **logic only** (no secrets) may ship under `plugins/<name>/scripts/`;
- optional paths may be **disabled until** user-local env/config is set;
- document legal/ToS residual risk in the PR body;
- Proposed or Accepted ADR update in the same batch is sufficient.

**Allowed in source (provider plugins):**

- public catalog, GraphQL/HTML parsers, offline fixtures;
- auth and protected-replay **orchestration** (Java calling external helpers);
- helper scripts or command builders with **no** embedded credentials or license
  material;
- provider-published public API identifiers when required for login.

**Always forbidden in the repository:**

License material, key material, sessions, tokens, browser profiles,
shared credentials, hardcoded account passwords.

## Site authentication for download

Some providers require a **logged-in account** before a replay URL, manifest,
or stream becomes available. This is separate from encryption-only protected
streams (see [Protected content (tiered)](#protected-content-tiered) above).

**Default (current Habitv scope):**

- public catalog and replay pages that do not require login;
- download delegated to yt-dlp or ffmpeg when URLs are already reachable;
- auth-gated items marked `protected` or `degraded`;
- clear sanitized failure when login is required and not configured.

Habitv does **not** ship browser session import or an embedded browser in the
default lifecycle.

**When site authentication is required (maintainer-directed work):**

Document the requirement in [`provider-inventory.md`](provider-inventory.md).
May ship in the **same scoped provider PR** as public catalog when maintainer
direction is documented. Prefer, in order:

1. **User-local credentials** — account login or API tokens in local config
   or environment variables only (never in git). Reference pattern: Stremio
   TF1+ addons (`habitv-references/stremio-addon-tvlegal`).
2. **External downloader auth** — yt-dlp `--username` / `--password` or
   `.netrc` when the extractor supports it; user-configured paths only (see
   [`ytdlp-cli-compatibility.md`](ytdlp-cli-compatibility.md)). Habitv does
   not pass authentication flags by default.
3. **Browser-mediated login** — when the provider only issues tokens after a
   web flow:
   - **Embedded browser** (CEF / CefSharp-style in-app login) — not in Habitv
     today; needs a dedicated JavaFX/tooling tracker item;
   - **System browser + manual step** — user logs in externally; session
     material stays user-local only;
   - **User-local automation** (Selenium, Playwright, etc.) — out of repo;
     optional reference: `habitv-references/TF1-Downloader` (Firefox +
     Selenium).

Do not commit session exports, browser profiles, or credential files.

## External tools

Tools: yt-dlp, ffmpeg, aria2, rclone, curl, rtmpdump, optional user-local
helpers when maintainer-requested (see Section 18.4).

Dedicated PRs for tool updates. No binaries in git unless requested. No mixing
unrelated provider behavior with dependency or CI changes.

### External tool update report

```markdown
## External tool update report

* Tool:
* Old version:
* New version:
* Source:
* Checksum:
* Reason:
* Compatibility:
* Validation:
* Risk:
```

## Public communication safety

Provider/plugin **public text** (PR titles and bodies, commit messages,
review comments, user-facing docs) must **not** expose unnecessary
operational detail. Keep wording high-level so reviews stay safe and
maintainers retain flexibility when endpoints change.

**Avoid in public text:**

- exact replay endpoints or undocumented API paths;
- request headers, auth/session mechanics;
- browser profile paths, token names, sensitive URL parameters;
- selector chains, step-by-step extraction flows, bypass-like mechanics;
- full provider-specific downloader command lines with auth or protected-content flags.

**Prefer high-level wording:**

- provider routing updated;
- metadata parsing improved;
- diagnostics clarified;
- protected or auth-gated content fails gracefully;
- offline fixture coverage added;
- command construction covered by tests.

Code, fixtures, and tests may contain technical detail **inside the
repository** when required for offline validation — but PR/commit **narrative**
should still summarize at this level unless the maintainer explicitly requests
more detail in the current conversation.

Canonical rule: `AGENTS.md` Section 20.14.

## Runtime diagnostics

Short root-cause messages for expected provider failures; avoid noisy stack
traces; preserve debug detail where useful; never swallow unexpected errors.

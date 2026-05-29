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
| protected | auth, cookies, DRM, geoblocking, anti-bot |
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

**Never:** bypass DRM; commit cookies, sessions, tokens, browser profiles.

## External tools

Tools: yt-dlp, ffmpeg, aria2, rclone, curl, rtmpdump.

Dedicated PRs for tool updates. No binaries in git unless requested. No mixing
with provider behavior changes.

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

## Runtime diagnostics

Short root-cause messages for expected provider failures; avoid noisy stack
traces; preserve debug detail where useful; never swallow unexpected errors.

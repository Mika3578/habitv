# Habitv maintenance dashboard

Lightweight planning view. Update in dedicated docs/planning PRs or when
directly relevant (Section 19.24).

**Sources:** [`dev-tracker.md`](dev-tracker.md), [`risk-register.md`](risk-register.md),
[`provider-inventory.md`](provider-inventory.md), [`modernization-backlog.md`](modernization-backlog.md).

---

## Current phase

**Provider restoration / modern replay** (dev-plan Phases 5–6): `ytdlp-migration`,
`provider-inventory`, France.tv hub discovery merged ([#137](https://github.com/Mika3578/habitv/pull/137)).
**Stabilization** (Phases 0–4) largely complete on `develop`.

**Java:** supported **runtime** remains **Java 8**; planned LTS **runtime target**
is **Java 21**, then **25** — see [`java-runtime-policy.md`](java-runtime-policy.md).

---

## Open PR priorities

| Priority | Scope | Branch/PR | Status |
|----------|-------|-----------|--------|
| P1 | Confirm `protect-develop` ruleset in GitHub UI | `branch-protection` | In progress (docs + #118 merged) |
| P2 | `habitv-repo` publish `yt-dlp` tool zip | `ytdlp-migration` | Follow-up |
| P2 | Provider fixture/rewrite PRs (6play, arte, lequipe, …) | per module | Queued |

---

## Security alerts

| Alert | Scope | Action | PR |
|-------|-------|--------|-----|
| Dependabot baseline | 294 reported (audit doc) | Triage per `dependency-security-audit` | Ongoing |
| Log4j 1.x CVEs | Runtime | Mitigated via reload4j | Done |

---

## Dependency updates

| Dependency | Type | Status | PR |
|------------|------|--------|-----|
| reload4j | security | Done | critical-log4j item |
| Maven plugins / transitives | audit | Documented; selective bumps | #132–#134, #136 (docs) |

---

## Providers

| Provider | Status | Notes |
|----------|--------|-------|
| `francetv` | working | Public hubs API (#137); yt-dlp download |
| `youtube` | working | yt-dlp wiring; API key externalized; #138 diagnostics |
| `canalPlus` / CStar | degraded | Graceful empty categories when protected/unreachable |
| `arte`, `6play`, `lequipe` | needs rewrite | Live drift; offline fixtures started |
| `wat`, `beinsport`, `clubic` | obsolete | Deprecation PRs TBD |

### Broken / degraded summary

- **Broken:** none classified as hard-fail in inventory (live tests quarantined)
- **Degraded:** `canalPlus` family (protected/legacy endpoints)
- **Obsolete:** see [`obsolescence-register.md`](obsolescence-register.md)

---

## CI issues

| Check | Status | Notes |
|-------|--------|-------|
| Maven CI (Java 8) | Required on `develop` | `ci-maven.yml`; four blocking contexts |
| Dependency review | Required | High severity fails PR |
| CodeQL | Enabled | Manual Maven build; non-blocking Lombok tracer warnings ([`ci.md`](ci.md)) |

---

## Packaging status

Windows launcher/installer foundation merged (#124, #126, #128). Full GUI
package still needs JavaFX-capable JDK 8 or future OpenJFX runtime packaging
(`javafx-modernization`).

---

## Java migration status

**Current support:** Java 8 only ([`java-runtime-policy.md`](java-runtime-policy.md))

**Preparation:** JDK 11+ compile bridge (PR #100); CI diagnostics on 11/17/21/25;
**planned runtime target:** Java 21 LTS, then Java 25 evaluation.

---

## Next 5 recommended PRs

1. Owner-verify `protect-develop` → close `branch-protection`
2. Publish `yt-dlp` to `habitv-repo` (`ytdlp-migration`)
3. Offline fixture PR for next priority provider (e.g. `arte` or `6play`)
4. Opt-in runtime update smoke test against live Pages manifest
5. Dedicated provider rewrite or deprecation PR per inventory row

---

Last reviewed: **2026-05-31**

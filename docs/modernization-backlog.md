# Habitv modernization backlog

Fast-moving modernization work tracked here instead of bloating `AGENTS.md`.
Canonical rule: [`AGENTS.md`](../AGENTS.md) Section 18.18.

Also see [`dev-tracker.md`](dev-tracker.md) for formal tracker items.

## Categories

- build blockers
- security
- dependencies
- CI
- providers
- metadata enrichment
- external tools
- packaging
- Java migration
- documentation
- cleanup/removal

## Item template

### \<title\>

* **Priority:** P0 / P1 / P2 / P3
* **Risk:** low / medium / high
* **Estimated scope:** small / medium / large
* **Suggested branch:** `fix/...`, `feat/...`, etc.
* **Validation required:** Maven commands, manual tests
* **Blocked by:** tracker item, CI, or dependency
* **Target phase:** see Section 18.1

## Active candidates

### Provider metadata enrichment pass

* **Priority:** P2
* **Risk:** medium
* **Estimated scope:** medium (per provider)
* **Suggested branch:** `feat/provider-<name>-metadata`
* **Validation required:** offline fixtures + targeted module tests
* **Blocked by:** provider status unknown in inventory
* **Target phase:** metadata enrichment

### CodeQL workflow for Java 8 Maven

* **Priority:** P2
* **Risk:** low
* **Estimated scope:** small
* **Suggested branch:** `ci/codeql-java8`
* **Validation required:** workflow run on PR
* **Blocked by:** `docs/required-checks-roadmap.md` phase
* **Target phase:** CI hardening

### CycloneDX SBOM in CI

* **Priority:** P3
* **Risk:** low
* **Estimated scope:** small
* **Suggested branch:** `ci/sbom-cyclonedx`
* **Validation required:** dedicated security PR; no SBOM committed
* **Blocked by:** explicit security modernization scope
* **Target phase:** dependency/security update

### OpenSSF Scorecard evaluation

* **Priority:** P3
* **Risk:** low
* **Estimated scope:** small
* **Suggested branch:** `ci/scorecard`
* **Validation required:** dedicated security PR
* **Blocked by:** maintainer decision
* **Target phase:** CI hardening

## Completed

*(move items here with PR reference when done)*

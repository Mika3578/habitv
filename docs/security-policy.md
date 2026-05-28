# Security update policy (agents and contributors)

Canonical sources:

- [`AGENTS.md`](../AGENTS.md) Section 16.4
- [`SECURITY.md`](../SECURITY.md) — vulnerability reporting
- [`docs/risk-register.md`](risk-register.md) — tracked risks

## Priority

Security fixes take priority over cosmetic cleanup, refactors, and
non-blocking docs work (see Section 16.19 PR queue).

## When an alert exists

For Dependabot alerts, CodeQL findings, dependency review failures, or
documented CVEs, the agent must:

1. Identify scope: runtime, test-only, plugin-only, build-only, or
   transitive.
2. Prefer upgrading to a **Java 8-compatible** safe version.
3. Document if no Java 8-compatible fix exists and propose alternatives.
4. Run full Maven validation after the fix.
5. Avoid suppressing alerts without a clear written reason.

## Agent must not

- ignore security alerts
- silence CodeQL without justification
- disable Dependabot or dependency review
- weaken branch protection to make CI green
- add broad `permissions:` to workflows
- commit secrets, tokens, cookies, or credentials
- disable analysis instead of fixing Java 8 / Maven workflow setup

## Reporting vulnerabilities

Do **not** open public issues for security vulnerabilities. Use GitHub
private security advisories per [`SECURITY.md`](../SECURITY.md).

## CodeQL

CodeQL should remain enabled when possible. See
[`docs/required-checks-roadmap.md`](required-checks-roadmap.md) for rollout
status. Do not remove CodeQL workflows without explicit approval.

If CodeQL fails on Java 8 Maven builds, fix JDK/workflow configuration
rather than disabling analysis.

## Dependency Review

High-severity vulnerable dependencies introduced in PRs should fail
dependency review (see `.github/workflows/dependency-review.yml`).

## Workflow changes

Before modifying security-related workflows, complete the **Workflow
safety checklist** (Section 16.8):

- document permission changes
- avoid `pull_request_target` unless justified
- pin trusted action versions
- never expose secrets to fork PRs

## Known sensitive areas

See `SECURITY.md` and `docs/risk-register.md` for hardcoded credentials,
legacy hosts, and other tracked risks. Do not extend or re-enable unsafe
patterns.

## Security update PR scope

Security PRs should be focused:

- dependency/security update classification
- dependency update report when versions change
- exact validation evidence
- no unrelated refactors or formatting

## SBOM (Section 18.9)

Propose CycloneDX or equivalent in a **dedicated security/tooling PR**.
Document generation location. Do not commit generated SBOM unless required.
Prefer CI artifacts.

## OWASP Dependency-Check (Section 18.10)

If used:

- do not disable to green CI;
- stable cache; no corrupted DB cache;
- NVD API key via GitHub secrets only;
- document suppressions:

```markdown
* CVE:
* Dependency:
* Reason:
* Scope:
* Expiry/review date:
* Link to upstream issue if available:
```

## OpenSSF Scorecard (Section 18.12)

Dedicated security PR only. Document permissions, schedule, Security tab,
noise, and follow-ups.

## Workflow tiers (Section 18.13)

See `AGENTS.md` Section 18.13 and `.cursor/rules/workflows.mdc` for PR fast,
security, packaging, and scheduled tiers.

# Habitv release policy

Canonical source: [`AGENTS.md`](../AGENTS.md) Section 18.17.

Related: [`java-runtime-policy.md`](java-runtime-policy.md),
[`provider-inventory.md`](provider-inventory.md),
[`dependency-policy.md`](dependency-policy.md),
[`security-policy.md`](security-policy.md).

## Before release or packaging work

Verify:

- full Maven build and tests;
- provider status summary (working / degraded / obsolete);
- dependency and security check status;
- external tool versions documented;
- Java 8 and JavaFX runtime assumptions documented;
- packaging scripts reviewed;
- changelog updated;
- known limitations listed.

Use phase **release preparation** or **packaging** (Section 18.1).

## Release readiness report

```markdown
## Release readiness report

* Version:
* Java runtime:
* JavaFX strategy:
* Maven validation:
* Tests:
* Providers working:
* Providers degraded:
* Providers obsolete:
* Security checks:
* External tools:
* Packaging:
* Known limitations:
* Release blocker list:
```

## Packaging PR scope

Keep packaging changes in dedicated PRs. Do not mix with provider refactors,
dependency updates, or docs-only cleanup.

See [`packaging/AGENTS.md`](../packaging/AGENTS.md) when present.

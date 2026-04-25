# Habitv Risk Register

| Risk ID | Description | Probability | Impact | Exposure | Mitigation | Owner role | Trigger | Status |
|---|---|---|---|---|---|---|---|---|
| R-001 | Reactor wiring reveals many latent compile failures | Medium | High | Medium | Enable module profiles and fix iteratively; track compile fallout per module | Build/Release Eng | First full reactor CI run | Monitoring |
| R-002 | Legacy external repository remains unavailable | Medium | High | High | Mirror artifacts into managed HTTPS repository | Build/Release Eng | Dependency resolve failure / 403 | Open |
| R-003 | JavaFX packaging modernization breaks installer output | Medium | High | High | Keep packaging migration behind dedicated branch/profile + smoke tests | Desktop Lead | First jpackage trial | Open |
| R-004 | Dependency upgrades introduce runtime regressions | Medium | Medium | Medium | Upgrade by slice and run targeted integration tests | Architect | Failed regression tests | Open |
| R-005 | Process docs created but not adopted | Low | Medium | Low | Add PR checklist requiring tracker/risk updates; baseline CI now enforces build checks | Tech Lead | PRs missing tracker delta | Monitoring |

| R-006 | Legacy provider URLs may be obsolete or moved to new platforms | High | Medium | High | Maintain `docs/url-inventory.md` and review URLs during each provider touchpoint | QA/Build Eng | URL-based smoke checks fail or redirect unexpectedly | Open |
| R-007 | HTTPS URL replacement alone may not restore provider plugins due to API/page changes | High | High | High | Track as `needs-dedicated-provider-rewrite` and schedule provider-specific rewrite PRs | Provider Maintainer | Parsing/downloading fails after URL modernization | Open |
| R-008 | Network-dependent tests make local/CI builds non-deterministic | Medium | High | High | Keep network tests opt-in under `-Pnetwork-tests`; do not run by default lifecycle | Build/Release Eng | Flaky failures in default build pipelines | Open |
| R-009 | JAXB-generated model drift breaks `application/core` compilation when schema-derived boolean accessors diverge from hand-coded `getXxx()` calls | Medium | High | High | Pin `maven-jaxb-plugin` version and align core code with generated JAXB accessor contract (`isXxx()` for booleans) | Architect + Build Eng | `application/core` compile errors against generated entities | Open |

## Escalation
- If any exposure remains **High** for >10 calendar days, escalate to Architect and Engineering Manager.

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
| R-009 | JAXB generated model/accessor drift can reintroduce compile breaks across `application/core` | Medium | High | High | Lock JAXB plugin config + XSD expectations and validate generated accessors in CI checks | Build/Release Eng | JAXB regeneration changes method signatures unexpectedly | Open |
| R-010 | JavaFX is missing from modern/non-JDK8 toolchains and blocks `application/trayView` compilation | High | High | High | Add explicit JavaFX dependency/profile strategy for non-JDK8 builds without touching current JAXB scope | Desktop Lead | `mvn ... compile` reaches trayView and fails on missing JavaFX classes | Open |
| R-011 | JAXB runtime provider is absent in runtime/test classpaths, blocking install-time verification | High | Medium | High | Add explicit JAXB runtime provider dependency and regression tests in follow-up PR | Build/Release Eng | `mvn install` fails in tests/runtime wiring with JAXB provider errors | Open |
| R-012 | Remaining network-dependent `TestListHttp` test keeps `mvn install` non-deterministic | High | Medium | High | Isolate `TestListHttp` as opt-in integration/network test and remove it from default install path | QA/Build Eng | `mvn install` fails in restricted networking or unstable remote conditions | Open |

## Escalation
- If any exposure remains **High** for >10 calendar days, escalate to Architect and Engineering Manager.

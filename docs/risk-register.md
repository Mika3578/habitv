# Habitv Risk Register

| Risk ID | Description | Probability | Impact | Exposure | Mitigation | Owner role | Trigger | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R-001 | Reactor wiring reveals many latent compile failures | Medium | High | Medium | Enable module profiles and fix iteratively; track compile fallout per module | Build/Release Eng | First full reactor CI run | Monitoring |
| R-002 | Legacy external repository remains unavailable | Medium | High | High | Mirror artifacts into managed HTTPS repository | Build/Release Eng | Dependency resolve failure / 403 | Open |
| R-003 | JavaFX packaging modernization breaks installer output | Medium | High | High | Keep packaging migration behind dedicated branch/profile + smoke tests | Desktop Lead | First jpackage trial | Open |
| R-004 | Dependency upgrades introduce runtime regressions | Medium | Medium | Medium | Upgrade by slice and run targeted integration tests | Architect | Failed regression tests | Open |
| R-005 | Process docs created but not adopted | Low | Medium | Low | Add PR checklist requiring tracker/risk updates; baseline CI now enforces build checks | Tech Lead | PRs missing tracker delta | Monitoring |

| R-006 | Legacy provider URLs may be obsolete or moved to new platforms | High | Medium | High | Maintain `docs/url-inventory.md` and review URLs during each provider touchpoint | QA/Build Eng | URL-based smoke checks fail or redirect unexpectedly | Open |
| R-007 | HTTPS URL replacement alone may not restore provider plugins due to API/page changes | High | High | High | Track as `needs-dedicated-provider-rewrite` and schedule provider-specific rewrite PRs | Provider Maintainer | Parsing/downloading fails after URL modernization | Open |
| R-008 | Network-dependent tests make local/CI builds non-deterministic | Medium | High | High | Keep network tests opt-in under `-Pnetwork-tests`; use stable public URLs for generic framework smoke tests and reserve provider URLs for provider-specific integration tests | Build/Release Eng | Flaky failures in default build pipelines | Mitigated for generic framework smoke tests |
| R-009 | JAXB model/accessor drift may recur until full `compile` + `install` are green in target toolchains | Medium | High | High | Keep deterministic generation + generated-source path controls; align production accessor calls with generated JAXB getter API and run full compile/install gates before closing HBTV-007 | Architect + Build/Release Eng | JAXB accessor mismatch compile/runtime regression | Mitigated (not closed) |
| R-010 | `application/trayView` depends on JavaFX classes unavailable on modern/non-JDK8 toolchains | High | Medium | High | Track dedicated JavaFX blocker task (HBTV-007a) and validate with explicit JavaFX-enabled profile/toolchain | Desktop Lead + Build Eng | Compile fails when toolchain lacks JavaFX | Open |
| R-011 | JAXB runtime provider (`com.sun.xml.bind.v2.ContextFactory`) missing during tests/runtime on modern JDKs | Medium | Medium | Medium | Mitigated by explicit Java 8-compatible `javax` JAXB provider wiring (`jaxb-api:2.3.1` + `jaxb-impl:2.3.3`) validated in core install path | Build/Release Eng | `install` test phase fails creating JAXB context | Mitigated |
| R-012 | Remaining network-dependent `TestListHttp` keeps `mvn install` non-deterministic | Medium | Medium | Medium | Mitigated by isolating `ListHttpNetworkIT` behind `-Pnetwork-tests` Failsafe path and excluding it from default Surefire lifecycle | QA/Build Eng | `mvn install` fails in restricted networking or unstable remote conditions | Mitigated |

## Escalation

- If any exposure remains **High** for >10 calendar days, escalate to Architect and Engineering Manager.

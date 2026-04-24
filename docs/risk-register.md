# Habitv Risk Register

| Risk ID | Description | Probability | Impact | Exposure | Mitigation | Owner role | Trigger | Status |
|---|---|---|---|---|---|---|---|---|
| R-001 | Reactor wiring reveals many latent compile failures | Medium | High | Medium | Enable module profiles and fix iteratively; track compile fallout per module | Build/Release Eng | First full reactor CI run | Monitoring |
| R-002 | Maven CI repository path is partially mitigated, but runtime updater still depends on legacy listing behavior | Medium | High | High | Keep Maven builds on reactor/Central, then migrate updater URL + listing logic (`FrameworkConf.UPDATE_URL` / `FindArtifactUtils`) in dedicated PR | Build/Release Eng | Runtime updater fails to discover release artifacts | Open (Partially mitigated) |
| R-003 | JavaFX packaging modernization breaks installer output | Medium | High | High | Keep packaging migration behind dedicated branch/profile + smoke tests | Desktop Lead | First jpackage trial | Open |
| R-004 | Dependency upgrades introduce runtime regressions | Medium | Medium | Medium | Upgrade by slice and run targeted integration tests | Architect | Failed regression tests | Open |
| R-005 | Process docs created but not adopted | Low | Medium | Low | Add PR checklist requiring tracker/risk updates; baseline CI now enforces build checks | Tech Lead | PRs missing tracker delta | Monitoring |
| R-006 | Dependency scan bootstrap may be slow/cancelled on first NVD update and may produce no report artifact; stale local H2 DB may cause scanner crashes | High | Medium | High | Keep scan non-blocking, pin plugin version, cache plugin artifacts only (not `~/.dependency-check`), clear local DB before run/retry, optional NVD API key, warn explicitly when report files are missing | Security Eng | Security workflow runs without artifact, feed update fails, or logs DB-closed/NoDataException errors | Open |
| R-007 | Java 17 profile diverges from default Java 8 behavior over time | Medium | Medium | Medium | Keep dual-JDK CI matrix and require both lanes green before merge | Build/Release Eng | Java 17 lane fails while Java 8 remains green | Open |
| R-008 | JAXB generation outputs incompatible model accessors under Java 17 compilation path | Medium | High | High | Pin/upgrade JAXB plugin config and add generated-source contract checks in CI | Architect + Build Eng | Java 17 compile fails in `application/core` with missing generated methods | Open |

## Escalation
- If any exposure remains **High** for >10 calendar days, escalate to Architect and Engineering Manager.

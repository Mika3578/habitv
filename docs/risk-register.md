# Habitv Risk Register

| Risk ID | Description | Probability | Impact | Exposure | Mitigation | Owner role | Trigger | Status |
|---|---|---|---|---|---|---|---|---|
| R-001 | Reactor wiring reveals many latent compile failures | High | High | High | Enable module profiles and fix iteratively | Build/Release Eng | First full reactor CI run | Open |
| R-002 | Legacy external repository remains unavailable | Medium | High | High | Mirror artifacts into managed HTTPS repository | Build/Release Eng | Dependency resolve failure / 403 | Open |
| R-003 | JavaFX packaging modernization breaks installer output | Medium | High | High | Keep packaging migration behind dedicated branch/profile + smoke tests | Desktop Lead | First jpackage trial | Open |
| R-004 | Dependency upgrades introduce runtime regressions | Medium | Medium | Medium | Upgrade by slice and run targeted integration tests | Architect | Failed regression tests | Open |
| R-005 | Process docs created but not adopted | Medium | Medium | Medium | Add PR checklist requiring tracker/risk updates | Tech Lead | PRs missing tracker delta | Open |

## Escalation
- If any exposure remains **High** for >10 calendar days, escalate to Architect and Engineering Manager.

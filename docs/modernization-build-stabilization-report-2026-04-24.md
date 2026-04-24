# Habitv Modernization & Build-Stabilization Report (v2)

_Date: 2026-04-24 (UTC)_

## Evidence and inspection scope

### Commands executed
- `mvn -version && java -version`
- `mvn -f pom.xml test -DskipTests=false -DtrimStackTrace=false`
- `mvn -f pom.xml install -DskipTests`
- `mvn -f application/pom.xml validate -DskipTests`
- `mvn -f fwk/api/pom.xml -q test`
- `curl -I -L --max-time 15 http://dabiboo.free.fr/repository`
- `find application -maxdepth 2 -name pom.xml -print | sort`
- `find . -maxdepth 3 -type f ...` (checked `.github`, `CODEOWNERS`, dependabot/renovate, agent/rules files)
- `rg` scans for hardcoded paths, JavaFX/JDK assumptions, external process/network-dependent tests.

### Fact vs assumption policy
- **Fact**: observed directly from files/command outputs.
- **Assumption**: plausible inference where current blockers prevent full pipeline execution.

---

## 1) Executive summary

### Build reliability status: **RED**
The build is currently not cleanly reproducible end-to-end due to POM topology/version mismatches, missing aggregation at the root, legacy repository/distribution assumptions, and outdated packaging/toolchain coupling.

### Top 5 blockers
1. Parent POM resolution fails in nested modules due version mismatch (`4.1.0` vs `4.1.0-SNAPSHOT`) and wrong/default `relativePath`.
2. Root/fwk aggregators do not declare child modules, producing false-green root builds.
3. Legacy external artifact/update endpoint is HTTP and currently responds `403`.
4. JavaFX packaging is tied to JDK7 internals via `systemPath` and absolute `jdk.home` paths.
5. Framework artifact typo (`SNASPHOT`) causes coordinate fragility.

### Top 5 high-ROI modernization opportunities
1. Normalize Maven reactor topology + parent versioning (quickest path to deterministic build).
2. Introduce minimal CI workflow matrix with fail-fast gates.
3. Migrate off unsupported logging/dependency baseline (security + compatibility).
4. Decouple packaging from local JDK layout; modernize JavaFX packaging path.
5. Add lightweight governance/tracker docs and enforce incremental delivery.

---

## 2) Build blocker matrix

| ID | Symptom (exact failure mode) | Root cause | Reproduction | Scope impacted | Severity | Confidence | Recommended fix | Effort | Quick win |
|---|---|---|---|---|---|---|---|---|---|
| B1 | `Non-resolvable parent POM ... com.dabi.habitv:parent:pom:4.1.0 (absent)` | Child POM parent versions do not match root parent version; nested POMs rely on wrong implicit parent path | `mvn -f application/pom.xml validate -DskipTests`; `mvn -f fwk/api/pom.xml -q test` | All submodules (`application/*`, `fwk/*`) | Critical | High | Align parent versions and add explicit `<relativePath>` in children | S | Yes |
| B2 | Root build reports success without compiling modules | Root/fwk POMs are `pom` packaging with no `<modules>` entries | `mvn -f pom.xml test` | Build/release visibility and CI trust | Critical | High | Define full reactor module graph in root/fwk | S | Yes |
| B3 | Repository reachability/availability issue (`HTTP/1.1 403 Forbidden`) | HTTP legacy external host used for dependency/update endpoints | `curl -I -L --max-time 15 http://dabiboo.free.fr/repository` | Dependency resolution and updater path | High | Medium-High | Move to managed HTTPS artifact repo and mirror required binaries | M | No |
| B4 | Packaging modules assume JDK7 JavaFX internals (`jfxrt.jar`, `ant-javafx.jar`, `systemPath`) | Legacy JavaFX build model incompatible with modern JDK delivery | Static inspection of `application/habiTv-linux` and `application/habiTv-windows` poms | Desktop packaging teams | High | High | Replace with OpenJFX deps + modern packaging (`jpackage`/maintained plugin) | M/L | No |
| B5 | Artifact coordinate drift (`4.1.0-SNASPHOT`) | Manual typo and version governance gap | Static inspection of `fwk/framework/pom.xml` | Framework consumers/release automation | High | High | Correct typo and centralize version governance | S | Yes |

---

## 3) Modernization roadmap

### Phase 0 — Build stabilization (Immediate)
**Objectives**
- Achieve one deterministic command for code build verification.
- Remove hard blockers in model resolution and module graph.

**Concrete tasks**
1. Fix parent version/relativePath in all child POMs.
2. Add missing root/fwk `<modules>`.
3. Fix `SNASPHOT` typo.
4. Add Maven Enforcer baseline (JDK/Maven/plugin version checks).
5. Add initial CI workflow (`verify` on PR).

**Risks + mitigation**
- Risk: latent compile errors surface after reactor wiring.
  - Mitigation: phase module activation by profiles and keep packaging optional initially.

**Success metrics**
- `mvn -B -ntp verify` executes full reactor in CI.
- Zero parent POM resolution errors.

**Sequencing/parallelization**
- Sequence tasks 1→2→3.
- Tasks 4–5 in parallel after 1–2 land.

### Phase 1 — Toolchain/dependency modernization
**Objectives**
- Move to supported runtime/dependency baseline.

**Concrete tasks**
1. Introduce Maven Wrapper/toolchains.
2. Establish Java LTS target migration path (17 preferred baseline).
3. Upgrade critical libraries (logging/json/html parsing/test).
4. Add dependency scanning (SCA) and policy thresholds.
5. Replace HTTP repo usage with internal HTTPS source of truth.

**Risks + mitigation**
- Risk: API breakage from large dependency jumps.
  - Mitigation: split by module slice and pin versions per iteration.

**Success metrics**
- No critical CVEs in direct runtime deps.
- Non-packaging modules compile/test on Java 17.

### Phase 2 — Architecture/maintainability
**Objectives**
- Reduce coupling and clarify module contracts.

**Concrete tasks**
1. Document module dependency map and forbidden couplings.
2. Add plugin SPI contract tests.
3. Isolate OS/process execution behind abstractions.
4. Stabilize JAXB/codegen lifecycle.

**Risks + mitigation**
- Risk: hidden runtime coupling in UI↔core flows.
  - Mitigation: add integration contract tests before refactoring.

**Success metrics**
- Reduced cross-module leakage.
- Repeatable plugin compatibility tests.

### Phase 3 — Performance and developer experience
**Objectives**
- Improve iteration speed and release consistency.

**Concrete tasks**
1. Add lint/format/static analysis gates.
2. Split unit/integration/packaging pipelines.
3. Add reproducibility attestations + SBOM.
4. Improve onboarding docs and runbooks.

**Risks + mitigation**
- Risk: too many gates too quickly.
  - Mitigation: start non-blocking then ratchet.

**Success metrics**
- Faster mean time to green PR.
- Measurable reduction in flaky failures.

---

## 4) Technical debt register

| Debt item | Impact | Interest paid | Priority | Upgrade gate |
|---|---|---|---|---|
| Parent/version drift | Hard build failures | Every branch merge/build attempt | P0 | Must-fix before upgrade |
| Missing root reactor modules | False green builds | Hidden breakage and delayed detection | P0 | Must-fix before upgrade |
| HTTP external repo/update endpoints | Availability/security policy risk | Build/updater instability | P0 | Must-fix before upgrade |
| JavaFX JDK7 systemPath packaging | Modern JDK packaging blocked | Release process brittleness | P1 | Must-fix before upgrade |
| Legacy dependency baseline (log4j1/jackson2.0/jsoup1.6) | Security/compliance exposure | Repeated exceptions and patch pressure | P1 | Must-fix before upgrade |
| Sleep/network-dependent tests in unit path | Flaky pipeline | Reruns and trust erosion | P1 | Can defer briefly |
| Hardcoded local OS paths/config examples | Onboarding/runtime fragility | Support overhead | P2 | Can defer |
| No CI/workflow/rules templates | Process inconsistency | Manual review overhead | P1 | Can defer briefly |

---

## 5) CI/CD hardening recommendations

### Deterministic builds
- Use `mvnw` and pinned JDK toolchain.
- Pin plugin versions and enforce with Enforcer.
- Build from full reactor only (no parent-only green checks).
- Publish immutable artifacts and metadata (commit SHA, build date, JDK).

### Caching/lock/reproducibility
- Use Maven local repo cache keyed by `pom.xml` hash.
- Add reproducibility smoke check (`clean verify` on clean cache periodically).
- Use dependency convergence checks and explicit version constraints.

### Fail-fast quality gates
1. model/enforcer
2. compile
3. unit tests
4. integration/network tests
5. packaging
6. vulnerability + static analysis

### Cross-platform consistency
- Linux + Windows CI matrix minimum.
- Move OS-specific packaging into dedicated jobs/profiles.
- Remove absolute path dependencies from default flow.

---

## 6) Living development tracker (mandatory)

Created and initialized as part of this update:
- `docs/dev-tracker.md` (human-readable live tracker)
- `docs/dev-tracker.json` (machine-readable status)
- `docs/decision-log.md` (decision history)
- `docs/risk-register.md` (live risks)

Tracker governance implemented:
- Definition of Ready / Definition of Done.
- Escalation path.
- Changelog with timestamped updates.
- Backlog entries with owner/dependencies/risks/dates/progress.

---

## 7) GitHub, AI agent, and rules integration plan

### Facts observed
- No `.github/workflows` currently found.
- No PR template / issue templates found.
- No `CODEOWNERS`, dependabot/renovate config found.
- No project-local agent/rules files found besides top-level docs.

### Proposed minimal file set

| File | Purpose | Minimal initial content | Priority | Owner role | Validation |
|---|---|---|---|---|---|
| `.github/workflows/build.yml` | Deterministic PR build | Maven verify matrix (linux/windows), cache, fail-fast | Now | Build/Release Eng | PR run green + artifact logs |
| `.github/workflows/security.yml` | Dependency/security scan | OWASP dep check (or equivalent) weekly + PR | Next | Security Eng | Failing CVE threshold blocks |
| `.github/pull_request_template.md` | Review consistency | Checklist: tests, tracker update, risk/rollback | Now | Tech Lead | PRs include checklist |
| `.github/ISSUE_TEMPLATE/bug_report.yml` | Repro quality | env, command, expected/actual, logs | Next | Maintainer | Issue completeness metric |
| `.github/ISSUE_TEMPLATE/modernization_task.yml` | Roadmap execution | links to tracker item, DoD, risk | Now | Architect/PM | Tracker↔Issue traceability |
| `CODEOWNERS` | Ownership clarity | module ownership by path | Next | Eng Manager | Auto-review routing works |
| `.github/dependabot.yml` | Dependency freshness | weekly Maven updates, grouped minor patches | Next | Build Eng | Automated dependency PRs |
| `docs/agent-guardrails.md` | AI change safety | scope, prohibited actions, mandatory tracker updates | Now | Architect | PR includes tracker delta |
| `docs/review-checklist.md` | Governance | build reproducibility + security + docs checklist | Now | Tech Lead | Required reviewer ack |
| `docs/build-release-policy.md` | Release reliability | versioning, branch policy, rollback playbook | Next | Release Manager | Dry-run release signoff |

---

## Facts vs assumptions

### Facts
- Parent version mismatch exists (`4.1.0` vs `4.1.0-SNAPSHOT`) and causes non-resolvable parent failures.
- Root parent build can pass without building application/fwk modules.
- Legacy repository/update URLs still use HTTP endpoints.
- Packaging modules depend on JavaFX `systemPath` and JDK7 paths.
- Examples/tests include network calls and timing-based sleeps.
- GitHub workflows/templates and governance baseline files are missing.

### Assumptions
- After topology fixes, additional compile/test failures are likely to surface in legacy UI/packaging modules.
- Security scan will likely flag vulnerabilities in old baseline dependencies.

---

## 30/60/90-day implementation plan

### 0–30 days
- Repair Maven topology (parent versions, relativePath, root/fwk modules).
- Add baseline CI build workflow + PR template.
- Start tracker-driven execution and decision/risk logging.

### 31–60 days
- Add toolchain wrapper and Java 17 compatibility profile.
- Upgrade highest-risk dependencies and add vulnerability scanning.
- Split deterministic unit tests from integration/network tests.

### 61–90 days
- Modernize JavaFX packaging path and cross-platform release jobs.
- Add CODEOWNERS, issue templates, and release policy docs.
- Enable static analysis/coverage gates with ratcheting thresholds.

## Prioritized backlog (initial)
1. P0: Fix parent/version/relativePath inconsistencies.
2. P0: Add root/fwk module aggregation.
3. P0: Bootstrap CI verify workflow and PR checklist.
4. P1: Move external HTTP repo dependency to managed HTTPS source.
5. P1: Correct framework artifact typo + version governance.
6. P1: Establish living tracker cadence + decision/risk governance.
7. P1: Dependency security baseline modernization.
8. P2: JavaFX packaging modernization.
9. P2: Code quality gates (format/lint/static analysis).
10. P3: DX and onboarding polish.

## Initial living tracker availability
Use immediately:
- Human tracker: `docs/dev-tracker.md`
- Machine tracker: `docs/dev-tracker.json`
- Decision log: `docs/decision-log.md`
- Risk register: `docs/risk-register.md`

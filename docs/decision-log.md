# 🧭 Habitv Decision Log

[ADR](https://adr.github.io)-style architecture and process decisions
for the modernization restart. **Append-only**: new decisions supersede
older ones rather than rewriting them in place.

> 📝 **Identifier convention** — every entry uses a descriptive
> kebab-case slug (e.g. `restart-from-master`) rather than an opaque
> code. See the `descriptive-slug-ids` ADR below for the rationale and
> the legacy mapping.

---

## 📊 ADR dashboard

| ADR | Title | Status |
|---|---|:--:|
| `restart-from-master` | Restart modernization from `master` | ✅ Accepted |
| `keep-java8-baseline` | Keep Java 8 as baseline until the build is stable | 🔁 Superseded |
| `java21-openjfx-baseline` | Java 21 and OpenJFX 21.0.7 as the new baseline | ✅ Accepted |
| `small-prs-linear-history` | Small PRs with linear history | ✅ Accepted |
| `habitv-repo-static-host` | `habitv-repo` as the public static artifact repository | ✅ Accepted |
| `provider-cleanup-separate` | Provider cleanup is separate from build / governance bootstrap | ✅ Accepted |
| `develop-runnable-baseline` | Runnable console baseline on `develop` before broader modernization | ✅ Accepted |
| `doc-sync-and-rule-lifecycle` | Doc sync protocol & rule lifecycle (meta-rules) | ✅ Accepted |
| `descriptive-slug-ids` | Switch tracker / risk / ADR identifiers to descriptive slugs | ✅ Accepted |
| `legacy-dabiboo-svn-removal` | Remove active legacy DabiBoo/SVN wiring from build and runtime paths | ✅ Accepted |
| `ai-policy-source-of-truth` | Align AI workflow policy around AGENTS.md source of truth | ✅ Accepted |
| `plugin-versioning-policy` | When to bump a plugin `<version>` independently of the parent POM | 🟡 Proposed |
| `jaxb-activation-dedup-defer` | Plan JAXB/Activation dedup before POM changes; defer namespace migration | 🟡 Proposed |
| `provider-protected-content-tiered-policy` | Tiered protected content policy for provider and agent work | ✅ Accepted |

---

## 🏷️ Status legend

| Symbol | Meaning |
|:---:|---|
| ✅ **Accepted** | Decision in force; supersedes any prior contradicting ADR |
| 🟡 **Proposed** | Awaiting validation by deliverable; reversible |
| ⛔ **Rejected** | Considered and explicitly not adopted |
| 🔁 **Superseded** | Replaced by a later ADR — kept for history |

---

## ✅ `restart-from-master` — Restart modernization from `master`

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |
| **Tracker** | `gov-bootstrap` |
| **Legacy code** | ADR-0001 |

**Context** — Earlier modernization attempts diverged from the stable
`master` baseline and accumulated unrelated changes, making it hard
to reason about scope and rollback. A previous bootstrap branch
(`chore/bootstrap-restart-from-master`) had different filenames and
conventions from the agreed spec.

**Decision** — Restart modernization from the current `master` tip.
Recreate the bootstrap branch from `origin/master` and deliver only
governance / documentation / CI validate baseline in the first PR.
All later modernization PRs branch from the resulting governance
baseline (or `develop` once created).

**Consequences**
- 🔁 Short-term throwaway of previous bootstrap branch content.
- ✅ Clear, reviewable starting point.
- ✅ Future ADRs can build on a known baseline.

---

## 🔁 `keep-java8-baseline` — Keep Java 8 as baseline until the build is stable

| | |
|---|---|
| **Status** | 🔁 Superseded |
| **Date** | 2026-05-16 |
| **Trackers** | `maven-reactor`, `java8-baseline` |
| **Risks** | `javafx-jdk8`, `jaxb-mismatch` |
| **Legacy code** | ADR-0002 |

**Context** — The codebase declares Java 1.7 in the root
`maven-compiler-plugin` and Java 7 in some packaging POMs. JavaFX 2.x
and `javax.xml.bind` 2.0 assume the legacy JDK 8 stack. Migrating
Java baselines while the reactor itself is broken would conflate
multiple risks.

**Decision** — Keep Java 8 (Temurin 8) as the baseline for all CI and
agent guidance until the `maven-reactor` and `java8-baseline` items
are stable. Any later baseline change requires a dedicated migration
PR and a superseding ADR.

**Consequences**
- ✅ AI agents and contributors must reject Java 9+ language and API
  suggestions.
- ✅ CI matrix uses Temurin 8 only in this phase.
- ⚠️ JavaFX 2.x and `javax.xml.bind` 2.0 remain locked in until the
  superseding ADR.

---

## ✅ `small-prs-linear-history` — Small PRs with linear history

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |
| **Legacy code** | ADR-0003 |

**Context** — Recent history mixes unrelated changes in single
commits, making review and rollback expensive.

**Decision**
- One tracker item per PR.
- Conventional Commits with imperative, lowercase subjects.
- Linear history enforced via branch protection (no merge commits,
  squash merges preferred).
- Rebase merges allowed only when the owner is comfortable.

**Consequences**
- ✅ Slightly more overhead per change, materially better
  reviewability and bisectability.
- ✅ CI gates only on the `validate` workflow until the
  `java8-baseline` item lands.

---

## ✅ `habitv-repo-static-host` — `habitv-repo` as the public static artifact repository

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-18 |
| **Tracker** | `static-repo-publish` |
| **Risks** | `legacy-maven-repo`, `ftp-deploy`, `legacy-update-pull`, `pages-layout-mismatch` |
| **Legacy code** | ADR-0004 |

**Context** — Today the project relies on
`http://dabiboo.free.fr/repository` for both Maven artifact
resolution and runtime updates. The host is third-party, plain HTTP,
and unmaintained.

**Decision** — Use `habitv-repo` as the public static repository
served over HTTPS (GitHub Pages or an equivalent static host), with
no GitHub Packages dependency and no runtime token requirement.
Publication layout is fixed as:

- `repository/com/dabi/habitv/...` for Maven artifact paths
- `repository/plugins.txt` for plugin ID discovery
- static `index.html` directory listings with anchor links for
  `FindArtifactUtils` fallback discovery (no host autoindex required)

Local publication must pass
`python scripts/static-repo/validate_repository_layout.py <repository-root>`
before cutover.

**Consequences**
- ✅ HTTPS, version-controlled publication contract is now fixed.
- ✅ Runtime update defaults remain unchanged (`habitv.update.enabled=false`
  unless explicitly set by the operator).
- ✅ End-to-end publication cutover completed via
  `https://github.com/Mika3578/habitv-repo/pull/2` with live Pages
  validation for `/repository/`, `/repository/plugins.txt`, and
  `/repository/com/dabi/habitv/`.
- ⚠️ One dedicated opt-in runtime update smoke test remains recommended
  (keep default runtime updates disabled).

---

## ✅ `provider-cleanup-separate` — Provider cleanup is separate from build / governance bootstrap

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |
| **Tracker** | `provider-inventory` |
| **Legacy code** | ADR-0005 |

**Context** — Several provider plugins (Pluzz, legacy Canal+, beIN,
others) are likely obsolete. Removing them in the bootstrap PR would
blend documentation work with risky behavior changes and break the
"small, scoped" rule.

**Decision** — Inventory provider/plugin status under
`provider-inventory` without removing modules. Each obsolete/renamed
plugin gets its own dedicated PR with a deprecation note. The
bootstrap PR does not touch `plugins/*` or runtime code.

**Consequences**
- ✅ Plugins remain unchanged in this PR. Reviewers can focus on
  governance.
- ✅ Cleanup happens later in safe, named units.

---

## ✅ `develop-runnable-baseline` — Runnable console baseline on `develop` before broader modernization

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-17 |
| **Tracker** | `console-runnable` |
| **Legacy code** | ADR-0006 |

**Context** — `develop` is the active modernization line. PR #27
introduced a useful runnable console / yt-dlp path but also included
`application/core` source edits that fail scoped compile/package.
Concurrent PR #25 and PR #26 were targeted at `master`, which would
mix branch lines if merged directly.

**Decision** — Create a dedicated stabilization branch from `develop`
and supersede PR #27 with scoped linear commits that keep only the
buildable subset (consoleView fat-jar path, yt-dlp runtime/test
wiring, and required build POM updates). Exclude failing unrelated
source edits and keep `legacy-url-migration` / `static-repo-publish`,
JavaFX modernization, and provider cleanup out of scope.

**Consequences**
- ✅ `develop` gained a factual runnable console baseline with
  offline yt-dlp command coverage.
- ⚠️ Known legacy repository blockers remain explicit and are tracked
  separately.
- 🔁 PR #25 and PR #26 must be retargeted/rebased to `develop` (or
  recreated as scoped follow-ups) after this baseline lands.

---

## ✅ `doc-sync-and-rule-lifecycle` — Doc sync protocol & rule lifecycle (meta-rules)

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-18 |
| **Touches** | `AGENTS.md` §11, §12 |
| **Supersedes** | — (additive) |
| **Legacy code** | ADR-0007 |

**Context** — The repository now carries seven tracked documents
(`CHANGELOG.md`, `AGENTS.md`, `dev-tracker.md`, `dev-tracker.json`,
`dev-plan.md`, `risk-register.md`, `decision-log.md`). They each
encode a different facet of the modernization state and must agree
with reality at all times. Two gaps remained after the docs refresh:

1. **No explicit cadence** for keeping these documents in sync after
   each step. Agents and humans were free to update some but not
   others, creating drift.
2. **No governance** for the rulebook itself. Rules in `AGENTS.md`
   could be added, weakened, or quietly removed by any commit, with
   no traceable approval and no protection against an AI agent
   exempting itself from a constraint inside the very PR that
   benefits from the exemption.

**Decision** — Add two sections to `AGENTS.md`:

- **§11 Doc sync protocol** — a table mapping each Conventional
  Commits type to the documents that must be updated, a table of
  per-milestone updates (PR merge, item completion, phase end, new
  risk, new decision), and a pre-PR verification checklist. The
  protocol is binding for every commit that changes meaningful
  state.

- **§12 Rule lifecycle (meta-rules)** — a self-applying rulebook
  governing how rules in `AGENTS.md` can be created, modified, or
  deleted. Three classes of rule (🔴 hard, 🟠 process, 🟣 meta) with
  proportionate consequences on breakage. Every rule change requires
  an ADR; rule deletions that remove a safeguard must point at the
  risk-register entry that takes over. AI-specific conflict-of-
  interest safeguards forbid an agent from weakening a hard rule
  inside the same PR that benefits from the change. Section 12
  protects itself with a **7-day cooling-off period** before any of
  its own rules can be merged-changed (typos and formatting excepted).

**Consequences**

- ✅ Drift between the tracker, plan, risk register, decision log,
  and changelog is now formally detectable: §11.3 ships a verification
  command set.
- ✅ The rulebook is self-protecting against silent weakening: every
  rule change requires an ADR and, for §12 itself, a cooling-off
  period.
- ⚠️ Slightly higher overhead per "meaningful" commit: the agent
  must consider which documents the change touches. The §11.1 table
  is designed so the answer is unambiguous and can be looked up in
  seconds.
- 🔁 Any future ADR that wants to lower the meta-rule barrier (for
  example, to remove the cooling-off period) must itself go through
  the cooling-off period it tries to remove.

---

## ✅ `descriptive-slug-ids` — Switch tracker / risk / ADR identifiers to descriptive slugs

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-18 |
| **Touches** | `dev-tracker.md`, `dev-tracker.json`, `dev-plan.md`, `risk-register.md`, `decision-log.md`, `AGENTS.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `SECURITY.md` |
| **Supersedes** | — (refactor, no prior ADR contradicted) |

**Context** — The previous identifier scheme used opaque numeric
codes:

- `HBTV-000` … `HBTV-013` for work items
- `R-001` … `R-015` for risks
- `ADR-0001` … `ADR-0009` for architecture decisions

These codes carry no semantic meaning. Reading a PR description like
"closes HBTV-004" or "mitigates R-013" forces the reader to look up
the registry to know what is actually being changed. After the docs
refresh, the dashboards already display human-readable titles, but
the cross-references in commit messages, PR bodies, and code comments
still used the codes.

**Decision** — Use **descriptive kebab-case slugs** as the canonical
identifier for every tracker item, risk, and ADR.

Naming convention:
- Lowercase ASCII.
- 2–6 words joined by hyphens.
- Stable for the lifetime of the item; renames go through an ADR.
- No type prefix when the surrounding context is unambiguous (a slug
  in `dev-tracker.md` is a work item; in `risk-register.md` is a
  risk; in `decision-log.md` is an ADR).

The old numeric codes are preserved as a `legacy code` field in each
entry so PR descriptions, commits, and issue history that already
reference the old codes remain traceable. New work must use the slug.

**Slug mapping**

| Old | New (work item) |
|---|---|
| HBTV-000 | `gov-bootstrap` |
| HBTV-001 | `maven-reactor` |
| HBTV-002 | `java8-baseline` |
| HBTV-003 | `branch-protection` |
| HBTV-004 | `legacy-url-migration` |
| HBTV-005 | `static-repo-publish` |
| HBTV-006 | `provider-inventory` |
| HBTV-007 | `ytdlp-migration` |
| HBTV-008 | `javafx-modernization` |
| HBTV-010 | `plugin-tester-align` |
| HBTV-011 | `console-runnable` |
| HBTV-012 | `own-version-deps-align` |
| HBTV-013 | `youtube-apikey` |

| Old | New (risk) |
|---|---|
| R-001 | `legacy-maven-repo` |
| R-002 | `ftp-deploy` |
| R-003 | `javafx-jdk8` |
| R-004 | `jaxb-mismatch` |
| R-005 | `live-tests-flaky` |
| R-006 | `provider-endpoints-dead` |
| R-007 | `legacy-update-pull` |
| R-008 | `ytdlp-behavior-diff` |
| R-009 | `pages-layout-mismatch` |
| R-010 | `reactor-version-range` |
| R-011 | `jaxb-plugin-unpinned` |
| R-012 | `plugin-tester-mismatch` |
| R-013 | `youtube-key-hardcoded` |
| R-014 | `pages-autoindex-gap` |
| R-015 | `silent-stat-ping` |

| Old | New (ADR) |
|---|---|
| ADR-0001 | `restart-from-master` |
| ADR-0002 | `keep-java8-baseline` |
| ADR-0003 | `small-prs-linear-history` |
| ADR-0004 | `habitv-repo-static-host` |
| ADR-0005 | `provider-cleanup-separate` |
| ADR-0006 | `develop-runnable-baseline` |
| ADR-0007 | `doc-sync-and-rule-lifecycle` |
| ADR-0008 | `descriptive-slug-ids` |
| ADR-0009 | `legacy-dabiboo-svn-removal` |

**Consequences**

- ✅ A PR title like `feat(youtube): close youtube-apikey` is
  immediately readable; `feat(youtube): close HBTV-013` was not.
- ✅ Slugs survive renumbering and re-ordering; numeric codes had a
  spurious linear-history vibe.
- ✅ The §11.3 verification check is updated to compare slugs.
- ⚠️ Existing PRs (#25, #29, #36) and commit messages still reference
  the old codes. The `legacy code` field on each entry preserves the
  link; no rewriting of history.
- ⚠️ The renaming itself does **not** trigger the meta-rule
  cooling-off period defined in the `doc-sync-and-rule-lifecycle`
  ADR, because it does not change any rule **inside** `AGENTS.md` §12.
  It only changes the identifiers used **across** the docs.

**Validation**
```bash
# Slug parity between tracker md and json
python3 - <<'EOF'
import json, re
md = open('docs/dev-tracker.md').read()
js = json.load(open('docs/dev-tracker.json'))
md_slugs = set(re.findall(r'`([a-z][a-z0-9-]{4,})`', md))
js_slugs = {i['id'] for i in js['items']}
missing = js_slugs - md_slugs
assert not missing, f"Missing slugs in dev-tracker.md: {missing}"
print('OK:', len(js_slugs), 'items, all slugs cross-referenced')
EOF
```

---

## ✅ `legacy-dabiboo-svn-removal` — Remove active legacy DabiBoo/SVN wiring from build and runtime paths

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-17 |
| **Trackers** | `legacy-url-migration` |
| **Risks** | `legacy-maven-repo`, `ftp-deploy`, `legacy-update-pull` |
| **Legacy code** | ADR-0009 |

**Context** — Active code and Maven metadata still referenced legacy
endpoints (`dabiboo.free.fr`, `scm:svn` on Assembla, and `cpt.php`
telemetry). These endpoints are either unavailable or no longer
acceptable for modern secure/reproducible builds.

**Decision** — Replace active Maven repository base URL with
`https://mika3578.github.io/habitv-repo/repository`, replace/remove
active SVN/Assembla SCM metadata in POMs in favour of GitHub SCM
metadata inheritance, disable startup telemetry by default behind
`habitv.stat.enabled` / `habitv.stat.url`, and disable runtime plugin
updates by default behind `habitv.update.enabled` with optional
`habitv.update.url` (defaulting to the GitHub Pages base when enabled).

**Consequences**
- ✅ Build/runtime wiring no longer depends on legacy DabiBoo/free.fr/SVN
  endpoints.
- ⚠️ `FindArtifactUtils` still expects Apache-style HTML directory indexes;
  do not enable updates until HBTV-005 publishes verified static
  `index.html` or manifest files on GitHub Pages.
- ✅ Functional Maven/publication cutover remains a separate
  `static-repo-publish` item.

---

## 🟡 `plugin-versioning-policy` — When to bump a plugin `<version>` independently of the parent POM

| | |
|---|---|
| **Status** | 🟡 Proposed |
| **Date** | 2026-05-21 |
| **Tracker** | `own-version-deps-align` (related) |
| **Touches** | `AGENTS.md` §4, `CONTRIBUTING.md`, `.github/copilot-instructions.md`, `.github/pull_request_template.md`, `plugins/*/pom.xml` |

**Context** — Plugin POMs under `plugins/*/pom.xml` inherit
`4.1.0-SNAPSHOT` from the root POM, but four already override the
version: `beinsport`, `footyroom`, `francetv` at `4.1.1-SNAPSHOT`,
and `ffmpeg` at `4.1.2-SNAPSHOT`. The runtime updater
(`fwk/framework/.../FindArtifactUtils.java:259-267`) computes the
candidate prefix via `getVersionMaj`, which returns the first two
dot-separated segments of the configured core version (e.g. `4.1`
for `4.1.0-SNAPSHOT`), and filters available artifacts with
`entry.getVersion().startsWith(versionMaj)`. Per-plugin patch
versions on the same `4.1` line are therefore supported by design.
However, no written rule defined **when** a plugin should bump.
Recent functional changes to `youtube` (yt-dlp downloader migration,
PR #52) and `arte` (EMAC API discovery fix, PR #55) shipped without
a bump, so the runtime updater cannot advertise them to users.
Conversely, `francetv` was bumped before its substantial follow-up
(PR #64) that extended channel slugs, URL acceptance, and
`MAX_PAGES`.

**Decision** — A plugin's `<version>` MAY override the parent only
when at least one of the following trigger conditions holds.
Otherwise the plugin inherits the parent version.

Bump triggers (any of):
- Downloader or parser behavior change (URL handling, format
  detection, binary swap such as `youtube-dl` → `yt-dlp`).
- User-facing endpoint or channel slug change (e.g. swapping a
  dead provider URL).
- User-facing configuration change (API key resolution, defaults,
  credential layout).

Non-triggers (do not bump):
- Offline test fixtures.
- Build, CI, or IDE cleanup.
- Dependency management or reactor alignment.
- Internal renames invisible to a configured user.
- Documentation-only changes.

Suffix rule — keep `-SNAPSHOT` while `CHANGELOG.md` `[Unreleased]`
has not been cut. Dropping `-SNAPSHOT` is gated by the
`static-repo-publish` tracker item.

Internal dependency rule (carried over from `own-version-deps-align`)
— shared reactor dependencies (`api`, `framework`, `plugin-tester`)
in a bumped plugin MUST stay on `${project.parent.version}`, never
`${project.version}`. A `${project.version}` reference in a plugin
whose version differs from the parent will resolve to a non-existent
artifact at compile time.

**Consequences**
- ✅ The runtime updater can advertise a new version of a single
  plugin to users without forcing a full reactor release.
- ✅ Reviewers can challenge an unjustified bump (or the lack of one)
  against an enumerated trigger list, instead of arguing taste.
- ⚠️ Plugin authors must keep a one-line note in their PR body
  identifying which trigger applies; the policy is enforced at
  review, not by tooling.
- 🔁 The policy formalises the implicit pattern used by `beinsport`,
  `footyroom`, `francetv`, `ffmpeg`. Existing overrides are
  grandfathered; no retroactive renames.

---

## ✅ `ai-policy-source-of-truth` — Align AI workflow policy around AGENTS.md source of truth

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-23 |
| **Tracker** | `gov-bootstrap` (follow-up) |
| **Touches** | `AGENTS.md`, `CLAUDE.md`, `.cursor/rules/habitv-master.mdc`, `.cursor/rules/pr-style.mdc`, `.github/copilot-instructions.md`, `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/*`, `.github/workflows/build.yml`, `CONTRIBUTING.md`, `README.md`, `docs/ci.md`, `docs/dev-plan.md`, `docs/dev-tracker.md`, `docs/dev-tracker.json`, `docs/github-repository-settings.md`, `docs/provider-inventory.md`, `docs/pull-request-style-guide.md`, `docs/repository-governance.md`, `docs/repository-maintenance.md`, `docs/security-critical-cve-investigation.md`, `docs/security-dependency-audit.md` |
| **Legacy code** | ADR-0010 |

**Context** — Workflow guidance drifted across AI and contributor
instruction files, especially around branch prefixes and duplicate
work prevention. Inconsistent rules increase duplicate branches/PRs
and make enforcement unclear.

**Decision**
- `AGENTS.md` is the single source of truth for repository-wide AI
  agent workflow policy.
- Tool-specific rule files should stay short and reference
  `AGENTS.md` for branch naming, commit style, PR structure,
  validation, duplicate prevention, linear history, and doc sync.
- Allowed work-branch prefixes are restricted to `fix/`, `feat/`,
  `chore/`, `docs/`, `test/`, `ci/`, and `refactor/`.
- Deprecated prefixes are `feature/`, `build/`, `runtime/`, and
  `provider/`, with explicit replacement mapping in `AGENTS.md`.
- Agent-created PRs must target `Mika3578/habitv`. Agents do not open
  PRs directly against `ikfon10/habitv`; that upstream flow remains
  maintainer-controlled unless explicitly requested.
- Before creating a branch, run duplicate/collision checks:
  `git fetch --all --prune`, branch scope search, open PR scope search,
  and `git check-ref-format --branch`.

**Consequences**
- ✅ Branch naming and duplicate-prevention workflow are consistent for
  AI tools and human contributors.
- ✅ Tool-specific files remain concise and less likely to diverge.
- ⚠️ Follow-up edits that change workflow policy must continue using
  ADR-backed updates per the AGENTS rule lifecycle.

---

## 2026-05-23 - Remove HBTV-style tracker IDs from workflow naming

`AGENTS.md` remains the source of truth for AI agent workflow policy.

HBTV-style tracker IDs such as `hbtv-006` and `HBTV-015` are deprecated
for new branch names, PR titles, commit subjects, and workflow
documentation.

Future work should use descriptive Conventional Commit scopes and
GitHub-native tracking through issues, labels, projects, and milestones.

Existing historical tracker references may remain only when needed to
preserve context.

---

## 🟡 `jaxb-activation-dedup-defer` — Plan JAXB/Activation dedup before POM changes

| | |
|---|---|
| **Status** | 🟡 Proposed |
| **Date** | 2026-05-29 |
| **Risks** | `jaxb-mismatch` |
| **Touches** | `docs/jaxb-activation-dedup-plan.md`, `docs/shade-duplicates-audit.md`, `docs/risk-register.md` |

**Context** — PR #134 documented Maven Shade duplicate warnings. Overlapping
`jaxb-api` / `jakarta.xml.bind-api` and three Activation artifacts are
P1 runtime risks in shaded `consoleView` and `habiTv` JARs. Habitv still
targets Java 8; true Jakarta EE 9+ migration is out of scope for the restart
phase (`keep-java8-baseline`).

**Decision** — Require a docs-only plan
([`docs/jaxb-activation-dedup-plan.md`](jaxb-activation-dedup-plan.md))
before any POM exclusion or version change. Compare Strategy A (keep
`jaxb-api`, exclude transitive API from `jaxb-runtime`), Strategy B (drop
explicit `jaxb-api`), and Strategy C (defer until Java 21/25 migration).
Implement JAXB API dedup and Activation dedup in **separate** follow-up
PRs with the plan validation matrix. Do not upgrade `javax.mail` or exclude
`activation:1.1` without email/MIME tests.

**Consequences**
- ✅ Reduces risk of silent uber-JAR behavior changes.
- ⚠️ Shade warnings remain until `fix/shade-jaxb-api-dedup` merges.
- 🔁 Accept or supersede this ADR when an implementation PR records the
  chosen strategy in the decision log.

---

## ✅ `provider-protected-content-tiered-policy` — Tiered protected content policy for providers

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-31 |
| **Last revised** | 2026-05-31 (rev. 3) |
| **Risks** | `provider-protected-replay-residual` |
| **Touches** | `AGENTS.md` §15.24, §16.20, §18.2, §18.4, §19.9, §19.16; `docs/provider-policy.md`; `docs/maintainability-policy.md`; `docs/ytdlp-cli-compatibility.md`; `.cursor/rules/habitv-providers.mdc`; `.github/instructions/plugins.instructions.md`; `plugins/AGENTS.md` |
| **Supersedes** | implicit absolute “never bypass protected content” wording in provider/agent rules |

**Context** — TF1+ and other replay providers expose a mixed catalog: some
episodes are reachable through public discovery and yt-dlp, while others are
encryption-restricted. Maintainers need Stremio-equivalent catalog **and** optional
protected replay support without agents refusing by default.

**Decision** — Adopt a **tiered** protected-content policy (revision 3):

1. **Default:** public discovery, external downloaders, `protected`/`degraded`
   status, sanitized failures. Optional auth/protected paths may exist but stay
   inactive until user-local config is set.
2. **Maintainer opt-in:** when the maintainer **explicitly requests** work in
   the **current conversation** **or** directs a **scoped provider PR** (tracker
   + PR scope), agents may implement provider-scoped auth hooks and external
   tool delegation in that PR. User license material, account passwords, and
   sessions stay **outside the repo**. Prefer delegation (yt-dlp, ffmpeg,
   plugin helper scripts, user-local services) over Java reimplementation.
3. **Governance:** document residual risk in the PR; update ADR/risk register
   in the same batch. A separate governance-only PR is not required.
4. **Always forbidden in git:** license material, account passwords, sessions,
   tokens, shared credentials, committed browser profiles.

**Consequences**

- Default agent work remains safe and catalog-focused.
- Maintainer-directed TF1+/Stremio-style protected replay work is allowed.
- Residual legal, ToS, and maintenance risk stays under
  `provider-protected-replay-residual`.

---

## ✅ `java21-openjfx-baseline` — Java 21 and OpenJFX 21.0.7 as the new baseline

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-06-06 |
| **Tracker** | `javafx-modernization` |
| **Risks** | `javafx-jdk8` |
| **Supersedes** | `keep-java8-baseline` |

**Context** — The Java 8 baseline (enforced by `keep-java8-baseline`) was
originally kept to avoid conflating reactor stabilization with language
migration. By 2026-06-06 the reactor is stable, `habiTv-linux` and
`habiTv-windows` were blocked out of the reactor by the `${jdk.home}`
system-scope `jfxrt.jar` bootstrap, and JDK 11+ no longer bundles
JavaFX. Continuing on Java 8 blocked: JavaFX modernization, reactor
completeness for packaging modules, and adoption of modern JDK support.

**Decision**
- Set `maven.compiler.release=21` as the project-wide baseline in the
  root POM.
- Declare OpenJFX 21.0.7 as an explicit `org.openjfx` dependency;
  remove the `system`-scope `javafx:jfxrt` artifact and the
  `${jdk.home}` property entirely.
- Return `habiTv-linux` and `habiTv-windows` to the reactor.
- Remove the URLClassLoader / bootstrap hack that loaded `jfxrt.jar`
  from a hardcoded JDK path.
- Adopt `jpackage` (bundled with JDK 14+) as the end-user distribution
  direction; `javafx-maven-plugin 2.0` is retired.

**Consequences**
- ✅ Java 8 is no longer supported; CI matrix drops Temurin 8.
- ✅ OpenJFX is required on the class path; no longer bundled implicitly.
- ✅ `habiTv-linux` and `habiTv-windows` participate in `mvn package`
  from `develop`.
- ✅ URLClassLoader / jfxrt.jar hack is removed.
- ⚠️ Any downstream fork that required Java 8 must migrate.
- 🔁 `keep-java8-baseline` is superseded by this ADR.

---

## 📝 How to add a new ADR

1. Pick a descriptive kebab-case slug (e.g. `enable-spotbugs`).
2. Use this template:
   ```markdown
   ## 🟡 `your-slug` — <Title>

   | | |
   |---|---|
   | **Status** | 🟡 Proposed |
   | **Date** | YYYY-MM-DD |
   | **Tracker** | `tracker-slug` |
   | **Risks** | `risk-slug` |

   **Context** — …
   **Decision** — …
   **Consequences** — …
   ```
3. Update the dashboard table at the top of this file.
4. Reference the ADR slug in any PR that depends on it.

---

## 🗂️ Legacy code index

For incoming references in PR descriptions, commit messages, and
external issue trackers, this table maps the old codes to the
current slugs. Source of truth is the per-entry `Legacy code` field.

| Work item (old) | Slug |
|---|---|
| HBTV-000 | `gov-bootstrap` |
| HBTV-001 | `maven-reactor` |
| HBTV-002 | `java8-baseline` |
| HBTV-003 | `branch-protection` |
| HBTV-004 | `legacy-url-migration` |
| HBTV-005 | `static-repo-publish` |
| HBTV-006 | `provider-inventory` |
| HBTV-007 | `ytdlp-migration` |
| HBTV-008 | `javafx-modernization` |
| HBTV-010 | `plugin-tester-align` |
| HBTV-011 | `console-runnable` |
| HBTV-012 | `own-version-deps-align` |
| HBTV-013 | `youtube-apikey` |

| Risk (old) | Slug |
|---|---|
| R-001 | `legacy-maven-repo` |
| R-002 | `ftp-deploy` |
| R-003 | `javafx-jdk8` |
| R-004 | `jaxb-mismatch` |
| R-005 | `live-tests-flaky` |
| R-006 | `provider-endpoints-dead` |
| R-007 | `legacy-update-pull` |
| R-008 | `ytdlp-behavior-diff` |
| R-009 | `pages-layout-mismatch` |
| R-010 | `reactor-version-range` |
| R-011 | `jaxb-plugin-unpinned` |
| R-012 | `plugin-tester-mismatch` |
| R-013 | `youtube-key-hardcoded` |
| R-014 | `pages-autoindex-gap` |
| R-015 | `silent-stat-ping` |

| ADR (old) | Slug |
|---|---|
| ADR-0001 | `restart-from-master` |
| ADR-0002 | `keep-java8-baseline` |
| ADR-0003 | `small-prs-linear-history` |
| ADR-0004 | `habitv-repo-static-host` |
| ADR-0005 | `provider-cleanup-separate` |
| ADR-0006 | `develop-runnable-baseline` |
| ADR-0007 | `doc-sync-and-rule-lifecycle` |
| ADR-0008 | `descriptive-slug-ids` |
| ADR-0009 | `legacy-dabiboo-svn-removal` |
| ADR-0010 | `ai-policy-source-of-truth` |

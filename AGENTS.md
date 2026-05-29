# 🤖 AGENTS.md — Rules for AI coding agents

Instructions for **Codex, Cursor, Claude, Copilot** and any other AI
coding agent working on the Habitv repository. These instructions
complement, but do not replace, the human review process.

## Agent rules metadata

* **Version:** 1.4.0
* **Last updated:** 2026-05-28
* **Maintainer:** repository maintainer
* **Scope:** Habitv AI-assisted development workflow
* **Canonical source:** `AGENTS.md`
* **Related files:** `.cursor/rules/`, `.github/copilot-instructions.md`,
  `.github/instructions/`, `CLAUDE.md`, `GEMINI.md`, nested `AGENTS.md`,
  `docs/dev-workflow.md`, `docs/provider-policy.md`, `docs/release-policy.md`,
  `docs/dependency-policy.md`, `docs/security-policy.md`,
  `docs/agent-rules-changelog.md`, `docs/agent-rules-backlog.md`,
  `docs/modernization-backlog.md`, `docs/obsolescence-register.md`,
  `docs/maintainability-policy.md`, `docs/maintenance-dashboard.md`,
  `docs/adr/`, `docs/decision-log.md`, `docs/agent-rule-profiles.md`,
  `docs/archived-agent-rules.md`

When rules change, update **Version** and **Last updated**, and add an
entry to [`docs/agent-rules-changelog.md`](docs/agent-rules-changelog.md).

Versioning: **patch** — wording/examples; **minor** — new rule/section;
**major** — stricter workflow, changed approval model, validation policy,
or removed major rule.

**Source of truth policy** — `AGENTS.md` is the **canonical source of
truth** for repository-wide AI agent behavior. All other AI instruction
files must either:

- point to `AGENTS.md`;
- summarize `AGENTS.md` without contradicting it; or
- contain path-specific rules that **extend** `AGENTS.md`.

When they conflict with `AGENTS.md`, `AGENTS.md` wins.

**Files that must not drift from `AGENTS.md`:**

- `.cursor/rules/*.mdc`
- `.cursor/rules/*.md`
- `.github/copilot-instructions.md`
- `.github/instructions/*.instructions.md`
- `CLAUDE.md`
- `GEMINI.md`
- any Codex, Cursor, Copilot, Claude, or Gemini instruction file
- nested `AGENTS.md` files (they extend; they do not replace)

If a rule is changed in one place, the agent must inspect the related
instruction files and update them, or explicitly state why no update
is needed.

### Tool-specific instruction files

These files mirror or point to the same mandatory rules. When they
conflict with `AGENTS.md`, `AGENTS.md` wins.

| File | Role |
|------|------|
| [`.cursor/rules/habitv-master.mdc`](.cursor/rules/habitv-master.mdc) | Always-on Habitv modernization guardrails |
| [`.cursor/rules/git-safety.mdc`](.cursor/rules/git-safety.mdc) | Git workflow, workspace safety, approval gates |
| [`.cursor/rules/maven-validation.mdc`](.cursor/rules/maven-validation.mdc) | Pre-commit Maven build/test validation |
| [`.cursor/rules/pr-review.mdc`](.cursor/rules/pr-review.mdc) | PR policy, Copilot review handling |
| [`.cursor/rules/java8-compatibility.mdc`](.cursor/rules/java8-compatibility.mdc) | Java 8 and dependency constraints |
| [`.cursor/rules/pr-style.mdc`](.cursor/rules/pr-style.mdc) | PR and squash-merge metadata style |
| [`.github/copilot-instructions.md`](.github/copilot-instructions.md) | Repository-wide Copilot instructions |
| [`.github/instructions/maven-java.instructions.md`](.github/instructions/maven-java.instructions.md) | Path-specific Copilot rules for Java/POM |
| [`.github/instructions/github-pr.instructions.md`](.github/instructions/github-pr.instructions.md) | Path-specific Copilot rules for PR/GitHub workflow |
| [`.github/instructions/plugins.instructions.md`](.github/instructions/plugins.instructions.md) | Path-specific Copilot rules for plugins |
| [`.github/instructions/github-actions.instructions.md`](.github/instructions/github-actions.instructions.md) | Path-specific Copilot rules for GitHub Actions |
| [`.github/instructions/packaging.instructions.md`](.github/instructions/packaging.instructions.md) | Path-specific Copilot rules for packaging |
| [`CLAUDE.md`](CLAUDE.md) | Claude Code pointer to `AGENTS.md` |
| [`GEMINI.md`](GEMINI.md) | Gemini pointer to `AGENTS.md` |
| [`plugins/AGENTS.md`](plugins/AGENTS.md) | Provider/plugin path-specific extensions |
| [`.github/AGENTS.md`](.github/AGENTS.md) | CI, branch protection, PR, GitHub Actions |
| [`docs/AGENTS.md`](docs/AGENTS.md) | Documentation-only work |
| [`scripts/AGENTS.md`](scripts/AGENTS.md) | Maintenance scripts |
| [`packaging/AGENTS.md`](packaging/AGENTS.md) | Packaging and installer work (when present) |
| [`docs/dev-workflow.md`](docs/dev-workflow.md) | Developer and agent workflow guide |
| [`docs/dependency-policy.md`](docs/dependency-policy.md) | Dependency update policy |
| [`docs/security-policy.md`](docs/security-policy.md) | Security update policy for agents |
| [`docs/provider-policy.md`](docs/provider-policy.md) | Provider status, metadata, testing |
| [`docs/release-policy.md`](docs/release-policy.md) | Release and packaging readiness |
| [`docs/modernization-backlog.md`](docs/modernization-backlog.md) | Fast modernization backlog |
| [`docs/obsolescence-register.md`](docs/obsolescence-register.md) | Obsolete providers and endpoints |
| [`.cursor/rules/habitv-providers.mdc`](.cursor/rules/habitv-providers.mdc) | Provider/plugin Habitv rules |
| [`.cursor/rules/habitv-maintainability.mdc`](.cursor/rules/habitv-maintainability.mdc) | Maintainability guardrails |
| [`docs/maintainability-policy.md`](docs/maintainability-policy.md) | DoD, tests, config, user data |
| [`docs/maintenance-dashboard.md`](docs/maintenance-dashboard.md) | Lightweight maintenance dashboard |
| [`docs/adr/README.md`](docs/adr/README.md) | ADR template and workflow |
| [`.vscode/extensions.json`](.vscode/extensions.json) | Recommended VS Code/Cursor extensions |
| [`.vscode/settings.example.json`](.vscode/settings.example.json) | Example workspace settings (no machine paths) |
| [`.cursor/rules/dependencies.mdc`](.cursor/rules/dependencies.mdc) | Dependency and security update rules |
| [`.cursor/rules/workflows.mdc`](.cursor/rules/workflows.mdc) | GitHub Actions and CI rules |
| [`.cursor/rules/cursor-ide.mdc`](.cursor/rules/cursor-ide.mdc) | Cursor/VS Code project configuration |
| [`.cursor/rules/00-canonical-source.mdc`](.cursor/rules/00-canonical-source.mdc) | Canonical source pointer (always on) |
| [`.cursor/rules/90-rule-evolution.mdc`](.cursor/rules/90-rule-evolution.mdc) | Rule evolution and drift audit |
| [`.cursor/rules/README.md`](.cursor/rules/README.md) | Cursor rules index and module map |
| [`docs/agent-rules-changelog.md`](docs/agent-rules-changelog.md) | AI rules version history |
| [`docs/agent-rules-backlog.md`](docs/agent-rules-backlog.md) | Future rule improvements |
| [`.github/instructions/docs.instructions.md`](.github/instructions/docs.instructions.md) | Path-specific Copilot rules for docs |
| [`.cursor/rules/agent-productivity.mdc`](.cursor/rules/agent-productivity.mdc) | Anti-bloat, profiles, speed/deep mode |
| [`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md) | Rule profiles and validation matrix |
| [`docs/archived-agent-rules.md`](docs/archived-agent-rules.md) | Retired agent rules |

### AI agent workflow index

Use this map to find the canonical rule for each workflow area:

| Area | Primary sections |
|------|------------------|
| Repository context | §1 |
| Mandatory safety rules | §2, §14.6–14.8 |
| Git workflow | §3, §4, §14.5, §14.11–14.12 |
| Validation workflow | §6, §14.2–14.3 |
| Maven / Java 8 compatibility | §2, §6, §14.9 |
| PR review workflow | §5, §14.10 |
| Copilot comments workflow | §14.4 |
| Commit and push approval gates | §14.1, §14.13–14.14 |
| Prohibited actions | §2, §14.1, §14.8 |
| Instruction governance | §15 |
| Safety and validation hardening | §15.8–15.28 |
| Branch protection and PR readiness | §15.16–15.19, §15.25 |
| Fast safe modernization | §16 |
| Evolutionary rules governance | §17 |
| Habitv-specific modernization | §18 |
| Habitv maintainability guardrails | §19 |
| Productivity and anti-bloat | §20 |
| Documentation sync | §12 |
| Hard-rule ADR lifecycle | §13 |

> 📚 **Companion files** (read them before acting):
> - [`CONTRIBUTING.md`](CONTRIBUTING.md) — branch / commit / PR policy
> - [`docs/dev-plan.md`](docs/dev-plan.md) — phased modernization plan
> - [`docs/dev-tracker.md`](docs/dev-tracker.md) — active work items
> - [`docs/risk-register.md`](docs/risk-register.md) — current risks
> - [`docs/decision-log.md`](docs/decision-log.md) — accepted ADRs

---

## 🧭 1. Repository context

Habitv is a **Java 8 Maven multi-module** application that downloads
French TV catch-up content via pluggable provider plugins.

### Layout

```
.
├── pom.xml                # root parent POM (aggregates fwk/, application/, plugins/)
├── fwk/                   # api/, framework/
├── application/           # core/, consoleView/, trayView/, habiTv/
│   ├── habiTv-linux/      # ❌ out of reactor — JavaFX 2.x, hardcoded ${jdk.home}
│   └── habiTv-windows/    # ❌ out of reactor — JavaFX 2.x, hardcoded ${jdk.home}
├── plugins/               # 22 provider/downloader/export plugins + plugin-tester
└── docs/                  # tracker, plan, risks, decisions, audit baseline
```

### Current state at a glance

| Item | State |
|------|:-----:|
| Maven reactor walkable end-to-end | ✅ |
| `mvn validate` on Ubuntu + Windows (CI) | ✅ |
| `mvn compile` from root | ✅ |
| `mvn test` (offline) | 🟡 partial |
| `mvn package` (full app) | ⛔ blocked on JavaFX / `jdk.home` |
| Legacy `dabiboo.free.fr` / SVN / FTP removed | ⬜ |
| `youtube-dl` → `yt-dlp` complete migration | 🟡 wiring only |
| Provider plugin endpoints audited | ⬜ |

---

## 🚦 2. Hard rules (never break)

| 🔴 Forbidden without explicit tracker item + ADR | Why |
|---|---|
| Restructure Maven reactor topology | High blast radius; tracked under `maven-reactor` |
| Bump Java baseline beyond **Java 8** | JavaFX 2.x and `javax.xml.bind` 2.0 assume JDK 8 |
| Migrate JavaFX (`jfxrt`) to OpenJFX | Tracked under `javafx-modernization` |
| Regenerate JAXB classes or move to `jakarta.*` | Risk `jaxb-mismatch` |
| Replace `youtube-dl` plugin behavior with `yt-dlp` | Tracked under `ytdlp-migration` |
| Change runtime updater URLs or layout | Tracked under `static-repo-publish` |
| Migrate FTP/HTTP repositories | Tracked under `legacy-url-migration` |
| Remove or rename provider/plugin modules | Tracked under `provider-inventory` |
| Add OWASP / SBOM / static-analysis plugins | Out of restart phase |
| Commit secrets, tokens, local paths, IDE files | 🚨 never, period |
| Write non-English content (branches, code, docs) | English-only policy |

---

## 📝 3. Commit policy

Conventional Commits, English, imperative, lowercase, ≤ 72 chars.

```
<type>(<scope>): <subject>

<body wrapped at ~72 chars — motivation, not implementation detail>
```

**Types allowed**: `feat`, `fix`, `refactor`, `perf`, `docs`, `test`,
`chore`, `build`, `ci`, `style`, `revert`.

**One logical change per commit.** Split any subject containing "and".

---

## 🌿 4. Branch naming and duplicate prevention

Allowed work branch prefixes (only):
- `fix/`
- `feat/`
- `chore/`
- `docs/`
- `test/`
- `ci/`
- `refactor/`

Deprecated prefixes (do not create new branches with these):
- `feature/` → `feat/`
- `build/` → `ci/` for CI/build automation, or `chore/` for maintenance
- `runtime/` → `fix/`, `feat/`, or `refactor/` depending on scope
- `provider/` → `fix/provider-...`, `feat/provider-...`,
  `refactor/provider-...`, `docs/provider-...`, or `test/provider-...`

Branch format:
`<type>/<short-scope>`

Examples:
- `fix/provider-youtube-ytdlp`
- `feat/provider-francetv-metadata`
- `docs/provider-inventory-update`
- `test/provider-offline-fixtures`
- `ci/maven-pr-validation`
- `refactor/provider-canalplus-cleanup`

HBTV-style tracker IDs are deprecated.

Do not use tracker IDs such as `hbtv-006`, `HBTV-015`, `hbtv-***`, or
`HBTV***` in:
- branch names
- PR titles
- commit subjects
- active documentation headings
- AI workflow rules
- PR templates

Use descriptive Conventional Commit scopes instead.

Examples:
- `fix(provider-youtube): switch downloader to yt-dlp`
- `docs(agents): align AI rules policy`
- `test(providers): add offline fixtures`
- `ci(maven): harden PR validation`

Before creating any branch, run:
```bash
git fetch --all --prune
git branch -a --list "*<short-scope>*"
gh pr list --repo Mika3578/habitv --state open --search "<short-scope>"
git check-ref-format --branch "<branch-name>"
```

If an existing branch or open PR already covers the same scope, do not
create a duplicate. Reuse the existing branch/PR, or create a clearly
scoped follow-up branch.

Keep history linear on work branches (no merge commits). See Section 14.5
for the required rebase-only branch update flow.

---

## 🎯 5. PR policy

| Rule | Detail |
|------|--------|
| Related issue / scope | Optionally reference a real GitHub issue (e.g. `#123`) and include a clear descriptive scope |
| Template | Fill every section of `.github/pull_request_template.md` |
| Diff size | Keep small and focused; reject opportunistic refactors |
| History | Linear inside work branches; no merge commits |
| Doc sync | Update tracker, risk register, decision log on meaningful changes |
| Plugin version bump | A `plugins/*/pom.xml` `<version>` override must match a `plugin-versioning-policy` trigger (downloader/parser, user-facing endpoint, user-facing configuration). Name the trigger in the PR body. Internal reactor deps in a bumped plugin MUST use `${project.parent.version}`. |

AI-agent PR target policy:
- Agent-created PRs must always target `Mika3578/habitv`.
- Agents must never create, update, close, or comment on PRs in
  `ikfon10/habitv`.
- `ikfon10/habitv` may be used as an upstream/reference repository only.
- PRs from `Mika3578/habitv` to `ikfon10/habitv` are maintainer-controlled
  and must not be automated by Cursor, Claude, Codex, or Copilot unless
  explicitly requested as a separate task.

Safe GitHub CLI examples:

Correct:
```bash
git push -u origin docs/align-ai-rules-policy

gh pr create \
  --repo Mika3578/habitv \
  --base develop \
  --head docs/align-ai-rules-policy \
  --title "docs(agents): align AI rules policy" \
  --body-file /tmp/habitv-ai-rules-pr-body.md
```

Wrong:
```bash
gh pr create --repo ikfon10/habitv ...
```

PR title policy:
- PR titles must use Conventional Commits.
- Good: `docs(agents): align AI rules policy`
- Good: `fix(provider-youtube): switch downloader to yt-dlp`
- Good: `ci(maven): harden PR validation`
- Bad: `HBTV-006 fix youtube provider`
- Bad: `docs(HBTV-015): update agent rules`
- Bad: `fix/hbtv-006-youtube-ytdlp`

Required PR body sections:
1. `Related issue` (optional `#<issue-number>`)
2. `Scope` (short descriptive scope, e.g. `provider-youtube-ytdlp`)
3. `Summary`
4. `Changes`
5. `Validation`
6. `Risk / rollback`
7. `Notes`

Before opening a PR:
1. Verify no open PR already covers the same scope.
2. Verify the branch is based on the latest `develop` (rebase only;
   Section 14.5).
3. Complete Section 14 pre-commit and pre-push gates, including
   Copilot review handling (Section 14.4).
4. Run validation per Section 14.2 (tiered gate) or document why it was
   skipped for docs-only work.
5. Include exact validation results in the PR body.
6. Ask for explicit developer approval before `gh pr create`
   (Section 14.1).

If tracking is needed, use GitHub-native tracking:
- a real GitHub issue number (e.g. `#123`)
- labels
- project fields
- milestones

Do not invent local tracker IDs.

---

## 🧪 6. Validation policy

**Pre-commit gate** — Section 14.2 requires full build and test
validation before commit approval unless the developer explicitly relaxes
it or the change is documentation-only with a clear skip statement.

Validation baseline by PR type:

- Docs-only PR:
  - `git diff --check`
  - Maven is not required unless build files changed.
- Default code PR:
  - `mvn -B -ntp -DskipTests validate`
- Java/code-impacting PR:
  - `mvn -B -ntp -DskipTests validate`
  - Targeted compile/test commands relevant to the touched modules.
- Full package:
  - Run only when explicitly scoped and document JavaFX/JDK 8
    constraints.

Default validation command for non-docs changes:

```bash
mvn -B -ntp -DskipTests validate
```

Stronger commands are **not default-safe**. Use them only when a
tracker item explicitly asks for them, and document the **exact
command + output** in the PR body.

| Command | Status |
|---------|:------:|
| `mvn validate` | 🟢 always run |
| `mvn compile` | 🟢 safe from `develop` since `own-version-deps-align` |
| `mvn package` | 🟡 use `-pl` to exclude broken modules |
| `mvn test` | 🟠 many tests hit live network — quarantine |
| `mvn verify` | ⛔ not safe yet |

---

## 🛠️ 7. How to maintain trackers

`docs/dev-tracker.md` and `docs/dev-tracker.json` are **mirrors**.
Always update both in the same commit. Fields per item:

- `id` — descriptive kebab-case slug (e.g. `legacy-url-migration`)
- `legacyCode` — old opaque code preserved for compat (`HBTV-XXX`)
- `displayTitle` — human-readable label
- `icon` — emoji prefix
- `status` — `proposed` | `in-progress` | `done` | `blocked` | `deferred`
- `priority` — `P0` (critical) | `P1` (high) | `P2` (normal) | `P3` (low)
- `progressPercent` — integer 0–100
- `scope`, `acceptanceCriteria`, `validation`, `pr`, `notes`

For risks in `docs/risk-register.md` and decisions in
`docs/decision-log.md`, follow the same convention: a descriptive
slug as the primary id, a `Legacy code` field for the old `R-0XX`
or `ADR-00XX` reference. Append-only; supersede rather than rewrite.

---

## 🧯 8. How to handle failures

### Test failure
1. Read the failing assertion **and** the test before editing code.
2. Do not patch the test only to make it pass — understand the contract.
3. If the failure is environmental (network, missing tool, OS binary),
   document the limitation; never blindly retry.

### Build failure
1. Capture the **exact** Maven output (module, plugin, error).
2. If caused by blocked legacy `dabiboo.free.fr` / FTP, it is a known
   class — link the relevant risk and tracker item.
3. Never bypass with `--no-verify` or by skipping hooks.

### Cannot run a required command
You **must**:
1. State exactly which command could not be run, and why.
2. Not claim success for that command.
3. Record the limitation in the PR body's "Validation results" section
   and, if it changes the reproducibility picture, in
   `docs/audit-master-baseline.md`.
4. Propose the minimal next step to make the command runnable.

---

## 🔐 9. Security awareness

- ❌ Never introduce hardcoded API keys, passwords, or tokens.
- ❌ Never extend the existing hardcoded credentials (Gmail tests,
  YouTube key, freebox FTP sample) — they are tracked for removal.
- ❌ Never silently re-enable runtime calls to legacy hosts
  (`dabiboo.free.fr`, `ftpperso.free.fr`).
- ✅ Read [`SECURITY.md`](SECURITY.md) before touching any auth or
  network code.

---

## 🗣️ 10. Communication conventions

- Always reply in **English** in code, commits, comments, docs, and PR
  text — regardless of the user's prompt language.
- Be precise about what you ran vs. what you reasoned about. Cite
  exact command outputs, file paths, and line numbers.
- Disagree with the user when their suggestion would break a hard
  rule above; surface the conflict instead of complying silently.

---

## 📅 11. Update cadence

This file is reviewed:
- On every phase transition in `docs/dev-plan.md`.
- When a new hard rule is added (must come with an accepting ADR).
- When the supported tooling baseline changes (Java version, Maven
  version, OS targets).

Last refresh: see the latest commit touching this file.

---

## 🔄 12. Doc sync protocol (after every step)

**Rule** — every commit that changes meaningful state must keep the
documentation set in lockstep. A "meaningful state change" is any
commit that creates, advances, completes, mitigates, supersedes, or
contradicts an item that is already documented.

### 12.1 What to update, by commit type

| Type | `CHANGELOG.md` | `dev-tracker.{md,json}` | `risk-register.md` | `decision-log.md` |
|------|:---:|:---:|:---:|:---:|
| `feat` | ✅ Features | ✅ status / progress | 🟡 if mitigates | 🟡 if architectural |
| `fix` | ✅ Fixes | 🟡 progress | 🟡 if mitigates | ❌ |
| `refactor` | ✅ | ❌ | ❌ | 🟡 if shape change |
| `perf` | ✅ | ❌ | ❌ | ❌ |
| `docs` | 🟡 if user-facing | ✅ if scope/status | ✅ if risk listed | ✅ if ADR proposed |
| `test` | ✅ Tests | 🟡 progress | 🟡 `live-tests-flaky` quarantine | ❌ |
| `chore` | 🟡 if user-facing | ❌ | ❌ | ❌ |
| `build` | ✅ Build | 🟡 progress | 🟡 if removes blocker | 🟡 if topology |
| `ci` | ✅ Build | 🟡 progress | ❌ | ❌ |
| `style` | ❌ | ❌ | ❌ | ❌ |
| `revert` | ✅ | ✅ reopen | ✅ reopen | 🔁 supersede |

`✅` = required when the commit touches that area · `🟡` = required
**if** the commit's content matches the condition next to the icon ·
`❌` = leave alone.

### 12.2 What to update, by milestone

| Milestone | Required updates |
|---|---|
| **PR merged** | Bump `progressPercent` on each item the PR advanced. Recompute the `summary` block in `dev-tracker.json`. Refresh the dashboard tables in `dev-tracker.md`, `dev-plan.md`, `risk-register.md`. |
| **All criteria met for an item** | Flip `status` to `done`, set `progressPercent: 100`, recompute summaries, add a CHANGELOG entry summarizing the item. |
| **Phase completed** | Mark the phase done in `dev-plan.md`. Recompute the phase progress bar. Refresh AGENTS.md "Current state at a glance" table (Section 1). |
| **New risk identified** | Add row to `risk-register.md` dashboard table AND a dedicated section below it. Link from the relevant tracker item. |
| **Risk mitigated** | Move the risk to the 🟢 Mitigated bucket. Annotate the mitigating PR in its `Status update` line. |
| **New decision** | Append an ADR in `decision-log.md` and update its dashboard table. Reference the ADR id from any item it constrains. |

### 12.3 How to verify before a PR

The agent MUST run this checklist before requesting review:

```bash
# 1. The two tracker files agree on slug ids
python3 - <<'EOF'
import json, re
md = open('docs/dev-tracker.md').read()
js = json.load(open('docs/dev-tracker.json'))
md_slugs = set(re.findall(r'`([a-z][a-z0-9-]{4,})`', md))
js_slugs = {i['id'] for i in js['items']}
missing = js_slugs - md_slugs
assert not missing, f"Missing slugs in dev-tracker.md: {missing}"
assert len(js_slugs) == js['summary']['total']
print('OK:', len(js_slugs), 'items, all slugs cross-referenced')
EOF

# 2. The progress summary matches the items
python3 - <<'EOF'
import json
js = json.load(open('docs/dev-tracker.json'))
s = js['summary']
counts = {'done': 0, 'in-progress': 0, 'proposed': 0, 'blocked': 0, 'deferred': 0}
for i in js['items']:
    counts[i['status']] += 1
assert s['done'] == counts['done'], (s, counts)
assert s['inProgress'] == counts['in-progress'], (s, counts)
assert s['proposed'] == counts['proposed'], (s, counts)
print('OK: summary matches item statuses')
EOF

# 3. CHANGELOG has an Unreleased entry referencing this PR if user-facing
grep -F "$(git rev-parse --short HEAD)" CHANGELOG.md || true
```

A PR that flunks 12.1, 12.2, or 12.3 must be amended before merge.

---

## 🧬 13. Rule lifecycle — meta-rules

This section is the rulebook **about** the rulebook. It governs how
rules in `AGENTS.md` can be created, modified, or deleted, including
the rules in this very section.

### 13.1 Rule identification

Every rule is uniquely identified by its **section heading + index**:

- `2.5` — "Replace `youtube-dl` plugin behavior with `yt-dlp`"
  (a hard rule from Section 2's table)
- `3` — "Conventional Commits …" (a process rule from Section 3)
- `13.3` — "Modify a rule" (a meta-rule, this section)

Rule classes and their breakage consequence:

| Class | Found in | Breakage consequence |
|------|---------|----------------------|
| 🔴 **Hard rule** | Section 2 | PR reverted; incident logged |
| 🟠 **Process rule** | Sections 3–15 | PR amended; warning logged |
| 🟣 **Meta-rule** | Section 13 | PR blocked at review; cannot proceed without compliance |

### 13.2 Create a rule

To **add** a new rule:

1. Open or update an ADR in `docs/decision-log.md` with status
   `🟡 Proposed`. The ADR must carry:
   - **Context** — why the rule is needed.
   - **Decision** — the rule's exact wording.
   - **Consequences** — what it constrains and how it interacts with
     existing rules.
2. Add the rule text to `AGENTS.md` in the same PR.
3. If the rule is a 🔴 hard rule (Section 2), it must reference a
   risk from `risk-register.md`. If no relevant risk exists, add one
   in the same PR.
4. On merge, the ADR transitions from `🟡 Proposed` to
   `✅ Accepted`.

### 13.3 Modify a rule

To **change** an existing rule's wording, scope, or strictness:

1. Open a new ADR (do not edit the old one) that **supersedes** the
   prior ADR. Use the metadata line `Supersedes: <old-slug>`.
2. Edit the rule text in `AGENTS.md` in the same PR.
3. Update the ADR dashboard in `decision-log.md`: the older ADR
   becomes `🔁 Superseded`; the new one is `✅ Accepted` on merge.
4. If the change weakens a 🔴 hard rule, the ADR must explicitly
   identify the residual risk and link the risk slug that now
   carries it.

### 13.4 Delete a rule

To **remove** a rule entirely:

1. Open an ADR with status `🟡 Proposed`. The decision must:
   - Quote the removed rule verbatim.
   - Explain why it is no longer needed (changed reality, replaced
     by a different mechanism, etc.).
   - Identify any residual risk that must be accepted, and link the
     risk slug entry created for it.
2. Remove the rule text from `AGENTS.md` in the same PR.
3. If the deleted rule was a 🔴 hard rule, the PR requires explicit
   owner approval and cannot be self-approved by an AI agent.

### 13.5 Conflict-of-interest safeguards

These hold for AI agents in particular:

- 🚫 An agent **may not** delete or weaken a hard rule in the same
  PR that benefits from doing so. Split into two PRs: first the
  rulebook change, then the work it enables.
- 🚫 An agent **may not** silently amend a rule. Every change goes
  through 13.2 / 13.3 / 13.4 with an ADR.
- 🚫 An agent **may not** create a rule that exempts itself, a
  specific tool, a specific branch, or a specific user from the
  meta-rules.
- ✅ An agent **must** flag a conflict between two existing rules
  the moment it observes one, even if it does not have authority
  to resolve it.

### 13.6 Self-modification of meta-rules

The meta-rules in Section 13 themselves can be changed — but with
extra care:

- The ADR proposing a change to Section 13 must remain in
  `🟡 Proposed` for **at least 7 days** before it can be merged.
- The cooling-off period prevents an agent from instantly weakening
  its own constraints inside a single working session.
- This 7-day cooling-off **does not apply** to fixing typos,
  formatting, or clarifications that demonstrably do not change the
  rules' force.

### 13.7 Audit trail

Any change to `AGENTS.md` must leave traceable evidence:

- The commit message references the ADR slug introducing or
  superseding the change (e.g.
  `docs(agents): ... (doc-sync-and-rule-lifecycle)`).
- The ADR references the section/rule it touches (e.g.
  `Touches: AGENTS.md §12, §13`).
- The dashboard tables in `decision-log.md` and `dev-tracker.md`
  are refreshed in the same PR.

A change that breaks the audit trail is invalid and must be
reverted.

---

## 🛑 14. Manual validation gate before commit and push

These gates apply to **every** AI coding agent (Cursor, Claude, Codex,
Copilot, and equivalents). They are mandatory before commit, push, PR
creation, branch update, or any action that changes remote state.

Previous approval from another session is **never** valid.

### 14.1 Manual commit and push approval gate

AI agents **may**:

- inspect files;
- edit files;
- propose patches;
- run safe validation commands;
- prepare a commit plan;
- prepare a PR description.

AI agents **must not** run any of these commands without explicit
developer approval **in the current conversation**:

- `git commit`
- `git push`
- `git push --force`
- `git push --force-with-lease`
- `gh pr create`
- `gh pr merge`
- `gh pr review`
- `gh pr comment`
- requesting or re-requesting review
- adding PR labels
- editing PR title or body
- resolving GitHub review conversations
- deleting branches
- deleting files outside the requested scope
- destructive cleanup commands
- any equivalent command that publishes, merges, comments, or changes
  remote state

Do not chain approval-gated actions without separate approval for each
(Section 15.8). Forbidden without approval:

```bash
git add -A && git commit -m "..."
git commit -m "..." && git push
```

Before any commit, the agent must ask exactly:

> Approve commit?

Before any normal push, the agent must ask exactly:

> Approve push?

Before any rebased push or rewritten-history push, the agent must ask
exactly:

> Approve force-with-lease push?

### 14.2 Maven validation gate (tiered)

Before asking for commit approval, run the validation tier that matches the
change unless the developer explicitly relaxes this gate.

| Tier | When | Commands |
|------|------|----------|
| **Docs-only** | Markdown/rules only; no Java, POM, workflow, or runtime files | Maven **may be skipped** with a clear note |
| **Standard code** | Typical module or narrow code changes | `mvn -B -ntp validate` plus targeted module tests when relevant |
| **Risky / code-wide** | Multi-module, core framework, or broad behavior changes | `mvn -B -ntp test` (add `-pl <module> -am` when scoped) |
| **Packaging / release / JavaFX / full app** | Packaging, release prep, or full-app validation | `mvn -B -ntp -DskipTests clean package` **only** when the environment supports it (JavaFX/`jdk.home`) and scope requires it |

**Standard code** example:

```bash
mvn -B -ntp validate
mvn -B -ntp -pl <module> -am test   # when module-specific
```

**Risky / code-wide** example:

```bash
mvn -B -ntp validate
mvn -B -ntp test
```

**Packaging / full-app** example (when in scope and environment supports it):

```bash
mvn -B -ntp -DskipTests clean package
```

For documentation-only changes, the agent may skip Maven validation only
if it clearly states:

- this is documentation-only;
- no Java source, POM, workflow, or runtime file was changed;
- Maven validation was not run;
- the branch is not validated for runtime behavior.

If the required tier for a code-impacting change was skipped without
developer approval, mark the work as:

> Not ready to commit.

Section 6 documents CI-safe baselines; this subsection is the
**pre-commit** gate unless the developer explicitly relaxes it.

### 14.3 Real functional testing gate

Before asking for commit approval, the agent must invite the developer
to test the real application behavior affected by the change.

Required wording:

> Please test the real application behavior affected by this change
> before approving the commit.

If the change affects Habitv runtime behavior, UI behavior, plugin
discovery, downloads, packaging, update behavior, or external tools,
the agent must list the exact manual tests the developer should
perform.

### 14.4 Copilot review comments gate

Before a PR is considered ready, the agent must inspect all GitHub
Copilot review comments and all unresolved PR review conversations.

Each Copilot recommendation must be classified as one of:

- accepted and fixed;
- intentionally rejected with a clear reason;
- not applicable;
- outside current PR scope;
- needs developer decision.

The agent must implement accepted recommendations before marking the
work ready.

The agent must reply to each Copilot comment in English with a short
explanation of what was done or why it was not applied.

The agent must **not** mark a Copilot conversation as resolved before:

- the recommendation has been reviewed;
- the recommendation has been handled;
- the reply has been posted;
- any required code/doc change has been committed and pushed, if
  applicable.

The agent must never mark Copilot comments as resolved just to silence
the review.

If a Copilot recommendation is risky or outside the PR scope, the agent
must explain that in a reply and ask the developer whether to defer it.

If resolving review conversations requires a GitHub command or UI
action, the agent must ask for explicit developer approval before
doing it.

See also Section 15.7 (GitHub review resolution gate).

### 14.5 Branch update and linear history gate

AI agents must keep Habitv PR history linear.

When a PR branch needs to be updated with the latest `develop`, the
branch must be rebased on `origin/develop`.

Allowed branch update flow:

```bash
git fetch origin --prune
git rebase origin/develop
```

Disallowed branch update flow:

- `git merge origin/develop`
- plain `git pull`
- `git pull` without `--rebase`
- GitHub **Update branch** if it creates a merge commit
- any workflow that creates a merge commit just to update the PR branch

If rebase conflicts occur, the agent must:

- stop and list the conflicting files;
- explain the intended conflict resolution;
- resolve conflicts carefully;
- continue with `git rebase --continue` only after the resolution is
  clear;
- rerun the relevant build and test validation after the rebase.

If the branch was already pushed before the rebase, the rebased branch
may only be pushed with:

```bash
git push --force-with-lease
```

The agent must never run:

```bash
git push --force
```

Before any force-with-lease push, the agent must ask exactly:

> Approve force-with-lease push?

### 14.6 Workspace safety gate

Before editing files, committing, rebasing, pushing, or resolving PR
comments, the agent must inspect the current Git state:

```bash
git status --short
git branch --show-current
git remote -v
git log --oneline --decorate -n 10
```

The agent must not work directly on:

- `develop`
- `main`
- `master`

If the workspace contains uncommitted developer changes, the agent must
not overwrite, delete, stage, or commit them without explicit developer
approval.

The agent must distinguish clearly between:

- files changed by the agent;
- files already modified before the task started;
- generated files;
- unrelated local changes.

### 14.7 Scope control gate

The agent must keep the change limited to the requested task.

The agent must **not**:

- perform unrelated cleanup;
- reformat unrelated files;
- rename files unless required;
- update dependencies unless explicitly requested;
- change generated files unless the generation command is part of the
  task;
- mix documentation, refactoring, behavior changes, dependency
  changes, and formatting changes in the same commit unless explicitly
  approved.

Before asking for commit approval, the agent must list any out-of-scope
change and explain why it was necessary.

### 14.8 Destructive command gate

The agent must never run destructive Git or filesystem commands without
explicit developer approval.

Restricted commands include:

- `git reset --hard`
- `git clean -fd`
- `git checkout -- <file>`
- `git restore <file>`
- `git branch -D`
- deleting files or directories outside the requested scope
- force-pushing without `--force-with-lease`

If one of these commands is needed, the agent must explain:

- why it is needed;
- what data may be lost;
- the safer alternative if available;
- the exact command it wants to run.

Only then may the agent ask for explicit approval.

### 14.9 Dependency change gate

The agent must not add, remove, or upgrade dependencies unless the task
explicitly requires it.

If a dependency change is required, the agent must document:

- dependency name;
- old version;
- new version;
- reason for the change;
- compatibility impact with Java 8;
- validation performed.

For Habitv, Java 8 compatibility must be preserved unless a dedicated
migration task explicitly says otherwise.

### 14.10 PR target and repository gate

PRs must target:

- repository: `Mika3578/habitv`
- base branch: `develop`

The agent must never open PRs against forks such as `ikfon10/habitv`.

Before PR creation, the agent must show:

- source branch;
- target repository;
- target base branch;
- PR title;
- PR body;
- validation result.

The agent must ask for explicit approval before running `gh pr create`.

### 14.11 Branch naming gate

Branch names must use allowed conventional prefixes such as:

- `feat/`
- `fix/`
- `docs/`
- `test/`
- `refactor/`
- `chore/`
- `ci/`

The agent must not create random Claude/Cursor-style branch names.

See Section 4 for duplicate-prevention checks before creating a branch.

### 14.12 Commit and PR naming gate

Commit messages, PR titles, docs, and comments must be in English.

Use Conventional Commits (Section 3).

Do not use internal style IDs in branch names, commit messages, PR
titles, PR bodies, or docs. Forbidden patterns include `hbtv-006`,
`HBTV-015`, `hbtv-*`, and `HBTV*`.

Commit messages, PR titles, PR bodies, and documentation must **not**
include AI attribution (Section 15.10).

### 14.13 Required pre-commit checklist

Before asking **Approve commit?**, the agent must provide:

```markdown
## Pre-commit checklist

* Current branch:
* Base branch:
* Changed files:
* Functional impact:
* Out-of-scope changes:
* Copilot comments handled:
* Build commands run:
* Test commands run:
* Manual real-application testing requested:
* Sensitive-data check:
* Validation evidence:
* Known failing tests:
* Known risks:
* Repo health status:
* Review readiness status:
* Commit classification:
* Staging check:
* Tool versions recorded:
* Branch protection alignment:
* Status checks summary:
* Instruction drift check:
* Proposed commit message:

If useful out-of-scope work was found, also include **Follow-up
candidates** (Section 15.26).
```

The agent must not commit until the developer explicitly replies with
commit approval.

### 14.14 Required pre-push checklist

Before asking **Approve push?** or **Approve force-with-lease push?**,
the agent must provide:

```markdown
## Pre-push checklist

* Current branch:
* Remote target:
* Push command planned:
* Whether the branch was rebased:
* Whether --force-with-lease is required:
* Latest validation result:
* PR target repository must be Mika3578/habitv:
* PR target branch must be develop:
```

The agent must not push until the developer explicitly replies with
push approval.

---

## 📋 15. Agent instruction governance and session gates

These gates apply at task start, during AI-rule updates, and when
stopping before commit, push, PR creation, or unresolved review work.

### 15.1 Instruction loading check

At the start of **every** coding task, the agent must identify which
instruction files were found and read.

The agent must report:

```markdown
## Instruction files loaded

* AGENTS.md:
* .cursor/rules:
* .github/copilot-instructions.md:
* .github/instructions:
* CLAUDE.md:
* Other agent files:
```

If a relevant instruction file exists but was not read, the agent must
stop and read it before editing files.

Relevant files include any path-specific nested `AGENTS.md` under the
directories being edited (Section 15.3).

### 15.2 Nested AGENTS.md gate

Use nested `AGENTS.md` files only when a subdirectory needs specific
rules.

Recommended nested files:

| Path | Scope |
|------|-------|
| [`plugins/AGENTS.md`](plugins/AGENTS.md) | Provider/plugin work |
| [`.github/AGENTS.md`](.github/AGENTS.md) | CI, branch protection, PR, GitHub Actions |
| [`packaging/AGENTS.md`](packaging/AGENTS.md) | Packaging and installer work |
| [`docs/AGENTS.md`](docs/AGENTS.md) | Documentation-only work |
| [`scripts/AGENTS.md`](scripts/AGENTS.md) | Maintenance scripts |

Nested rules must **extend** the root `AGENTS.md`, not replace it.
When nested rules add path-specific constraints, they must reference
the root section they extend.

Recommended path-specific Copilot instruction files:

| Path | Scope |
|------|-------|
| [`.github/instructions/plugins.instructions.md`](.github/instructions/plugins.instructions.md) | Provider/plugin work |
| [`.github/instructions/maven-java.instructions.md`](.github/instructions/maven-java.instructions.md) | Java/POM changes |
| [`.github/instructions/github-actions.instructions.md`](.github/instructions/github-actions.instructions.md) | GitHub Actions workflows |
| [`.github/instructions/github-pr.instructions.md`](.github/instructions/github-pr.instructions.md) | PR and GitHub workflow |
| [`.github/instructions/packaging.instructions.md`](.github/instructions/packaging.instructions.md) | Packaging and installers |

Recommended path-specific content:

- **`plugins/`** — provider/plugin changes, offline fixtures, no
  live-network-only validation, Java 8 compatibility.
- **`.github/`** — CI workflow safety, no `secrets: inherit` unless
  explicitly justified, branch protection awareness.
- **`packaging/`** — no installer/binary scope creep, no platform
  packaging unless requested.
- **`docs/`** — docs-only validation, no runtime claims without source.
- **`scripts/`** — no destructive scripts, cross-platform
  PowerShell/Bash parity when applicable.

### 15.3 Rule drift prevention gate

The repository must avoid contradictory AI rules.

Before finishing an AI-rule update, the agent must check for conflicting
instructions about:

- commit approval;
- push approval;
- PR creation;
- PR merge;
- rebase vs merge;
- Maven validation;
- Java 8 compatibility;
- Copilot comment handling;
- branch naming;
- PR target repository;
- destructive commands;
- chained commands that bypass approval;
- secret/credential handling;
- AI attribution in commits or PRs;
- generated-file and binary-file rules;
- branch protection alignment;
- status check verification;
- CODEOWNERS review;
- explicit staging rules;
- PR comment actions;
- commit content classification.

If conflicts are found, resolve them by making `AGENTS.md` the source
of truth and updating or trimming conflicting tool-specific files.

Document the drift check result in the pre-commit checklist.

### 15.4 Repo health gate

Before asking for commit approval, the agent must run or document the
relevant repository health checks.

**For code changes:** run the tier from Section 14.2 (docs-only skip,
standard `validate` + targeted tests, risky `test`, packaging `clean package`
when in scope).

Example **standard code** tier:

```bash
mvn -B -ntp validate
mvn -B -ntp -pl <module> -am test   # when module-specific
```

Example **risky / code-wide** tier:

```bash
mvn -B -ntp validate
mvn -B -ntp test
```

**For documentation-only AI-rule changes:**

- verify Markdown formatting manually;
- verify there are no contradictory duplicate rules;
- verify instruction files point to the same workflow;
- verify no Java source, POM, dependency, workflow, or runtime file
  changed.

The final status must be exactly one of:

- **Ready for developer manual testing**
- **Ready for commit approval**
- **Ready for push approval**
- **Ready for PR review**
- **Not ready to commit**
- **Not ready to merge**
- **Needs developer decision**

Use **Not ready to merge** when required status checks are pending or
failing (Section 15.18), or when required code-owner review is missing
(Section 15.19).

Include the chosen status in the pre-commit checklist. See Section 15.16
for PR review readiness criteria.

### 15.5 No intermediate commit gate

The agent must not create incremental "work in progress" commits.

The agent must keep working locally until:

- the requested change is complete;
- validation is run or clearly marked as skipped;
- known risks are documented;
- the manual testing request is written;
- the developer approves the commit.

The agent must not push after every commit. Push only when the work is
coherent, reviewed, and explicitly approved.

### 15.6 Session handoff gate

When stopping before commit, push, PR creation, PR merge, or unresolved
review work, the agent must provide a handoff:

```markdown
## Handoff

* Current branch:
* Latest local commit:
* Files changed:
* Validation run:
* Validation not run:
* Manual tests still needed:
* Copilot comments still open:
* PR status:
* Next recommended action:
* Exact command not yet approved:
```

### 15.7 GitHub review resolution gate

A GitHub PR review conversation may only be resolved when:

- the recommendation was implemented; **or**
- a clear English reply explains why it was rejected, not applicable,
  or deferred;
- the developer approved resolving it if the action changes GitHub
  state.

Never resolve conversations silently.

This applies to Copilot review comments (Section 14.4) and all other
GitHub PR review threads.

### 15.8 Chained command safety gate

AI agents must not hide or bypass approval requirements through chained
commands.

The agent must never run a command chain that includes any approval-gated
action unless **each** gated action was explicitly approved first.

Approval-gated actions include:

- `git commit`
- `git push`
- `git push --force-with-lease`
- `git push --force`
- `gh pr create`
- `gh pr merge`
- `gh pr comment`
- `gh pr review`
- resolving GitHub review conversations
- deleting branches
- deleting files outside the requested scope
- destructive cleanup commands

Forbidden examples without explicit approval:

```bash
git add -A && git commit -m "..."
git commit -m "..." && git push
git fetch origin && git rebase origin/develop && git push --force-with-lease
gh pr comment ... && gh pr merge ...
```

Any script that performs commit, push, PR creation, PR merge, or review
resolution indirectly is also forbidden without approval.

Each gated action requires a separate checklist and separate developer
approval.

### 15.9 Secret and credential safety gate

The agent must never commit secrets, credentials, private tokens,
cookies, browser sessions, API keys, Maven credentials, GitHub tokens,
local paths containing credentials, or private machine-specific
configuration.

Before asking for commit approval, the agent must inspect the diff for
sensitive data.

Sensitive files and patterns include:

- `.env`
- `settings.xml`
- credentials files
- browser cookie exports
- session files
- access tokens and refresh tokens
- private keys
- local absolute paths that reveal private setup
- generated logs containing tokens, cookies, headers, or credentials

If sensitive data is found, the agent must stop and ask the developer
how to remove or redact it.

### 15.10 AI attribution gate

Commit messages, PR titles, PR bodies, and documentation must **not**
include AI attribution.

Forbidden examples:

- `Generated by Claude`, `Generated by Cursor`, `Generated by Copilot`,
  `Generated by ChatGPT`, `AI-generated`
- `Co-authored-by: Claude`, `Co-authored-by: Cursor`,
  `Co-authored-by: Copilot`, `Co-authored-by: ChatGPT`

The author and committer identity must remain the developer's normal Git
identity.

### 15.11 Known failing tests gate

The agent must not ask for commit or push approval if there are known
failing tests unless the final status is clearly marked:

> Not ready to commit.

If tests fail, the agent must:

- list the failing command;
- summarize the failure;
- identify whether the failure is related to the current change;
- propose the next fix;
- avoid committing until the developer explicitly approves committing
  despite the failure.

### 15.12 Generated files gate

The agent must not edit generated files manually unless the task
explicitly requires it.

If generated files changed, the agent must document:

- which generation command produced them;
- why the generated output is expected;
- whether source files were changed consistently;
- how the generated output was validated.

For Habitv, generated JAXB/XML-related files must not be changed
casually. Any generated-source change requires a dedicated explanation
and validation.

### 15.13 Binary and large file gate

The agent must not add binaries, archives, installers, downloaded tools,
videos, screenshots, or large generated artifacts unless explicitly
requested.

Forbidden by default:

- `.zip`, `.exe`, `.dll`
- `.jar` generated artifacts
- `target/` outputs
- downloaded external tools
- video/audio files
- local logs
- IDE caches

If a binary or large file is required, the agent must ask the developer
before staging it.

### 15.14 Path-specific rule gate

Use path-specific instructions for areas that need stricter rules. See
Section 15.2 for the recommended nested `AGENTS.md` and
`.github/instructions/*.instructions.md` files.

Each path-specific file must extend `AGENTS.md` and must not contradict
it. When editing under a scoped path, read the matching path-specific
file before making changes.

### 15.15 Validation evidence gate

Before handoff, commit approval, push approval, or PR readiness, the
agent must provide evidence for the touched surface.

Required evidence:

- exact commands run;
- pass/fail result;
- affected modules;
- files inspected;
- tests intentionally skipped;
- reason skipped tests were acceptable;
- manual test request for real application behavior.

The agent must **not** write vague validation such as:

- "tests pass"
- "build ok"
- "should work"
- "validated locally"

The agent must provide exact command lines and concrete results.

### 15.16 Review readiness gate

A PR is not review-ready until:

- full required validation is complete or clearly marked as not complete;
- Copilot comments are handled or explicitly listed as pending;
- unresolved GitHub conversations are listed;
- known risks are listed;
- branch is rebased on latest `origin/develop`;
- no merge commit was introduced;
- no sensitive data is present (Section 15.9);
- no unrelated files are included;
- commit history is coherent and not WIP;
- no AI attribution is present (Section 15.10);
- no unapproved binaries or generated files are included (Sections 15.12,
  15.13, 15.23);
- branch protection expectations are met or documented (Section 15.17);
- required status checks are verified or listed as pending/failing
  (Section 15.18);
- CODEOWNERS coverage is reviewed when applicable (Section 15.19).

The final status must be exactly one of:

- **Ready for developer manual testing**
- **Ready for commit approval**
- **Ready for push approval**
- **Ready for PR review**
- **Not ready to commit**
- **Not ready to merge**
- **Needs developer decision**

Use **Not ready to merge** when required status checks are pending or
failing (Section 15.18), or when required code-owner review is missing
(Section 15.19).

### 15.17 Branch protection alignment gate

AI agent rules must mirror the repository branch protection expectations
documented in `docs/repository-governance.md` and enforced on `develop`.

Before declaring a PR ready, the agent must verify or document:

- required status checks are passing or still pending;
- unresolved review conversations are listed;
- branch history is linear;
- the PR branch is up to date with `origin/develop` by rebase, not merge;
- no direct push was made to `develop`;
- no force push was made to protected branches;
- no merge commit was introduced.

The agent must never suggest bypassing branch protection, disabling
checks, reducing required reviews, or merging with failing checks unless
the developer explicitly asks for repository administration guidance.

### 15.18 Required status check gate

If GitHub status checks exist for the PR, the agent must inspect and
summarize them before PR readiness.

The agent must report:

- check name;
- status;
- failing job if any;
- whether the failure is related to the current change;
- next recommended fix.

The agent must not say "CI is green" unless it has verified the current
PR checks.

If checks are **pending**, the final status must be:

> Not ready to merge

If checks are **failing**, the final status must be:

> Not ready to merge

### 15.19 CODEOWNERS and reviewer gate

If [`.github/CODEOWNERS`](.github/CODEOWNERS) exists, the agent must
inspect it when files are changed.

Before PR readiness, the agent must list:

- changed paths covered by CODEOWNERS;
- required reviewers if identifiable;
- whether review is still needed.

The agent must not mark the PR as ready to merge when required
code-owner review is missing.

### 15.20 Explicit staging gate

The agent must never stage everything blindly.

Forbidden by default:

```bash
git add -A
git add .
git add --all
```

Allowed staging pattern:

- stage only intentional files by explicit path;
- show staged diff before commit approval;
- confirm no unrelated files are staged.

Before asking **Approve commit?**, the agent must run:

```bash
git diff --stat
git diff --check
git diff --cached --stat
git diff --cached --check
```

If nothing is staged yet, the agent must say so and show the exact files
it plans to stage.

### 15.21 Scratch space gate

Temporary agent work must go into a git-ignored scratch directory.

Recommended directory: `agent_space/`

Rules:

- use `agent_space/` for temporary scripts, investigation notes, raw
  outputs, prompt drafts, and experiments;
- do not commit files from `agent_space/`;
- do not store secrets in `agent_space/`;
- delete or leave scratch files untracked according to developer
  preference;
- never stage scratch files unless explicitly requested.

If `agent_space/` is missing from `.gitignore`, propose adding it in a
follow-up documentation or tooling commit.

### 15.22 Tool version and environment gate

Before changing build, dependency, generated, or packaging files, the
agent must record the relevant tool versions.

For Habitv, report when relevant:

```bash
java -version
mvn -v
git --version
gh --version   # if GitHub CLI is used
```

Also report: operating system, active branch, Maven module touched.

If generated files or lock-like files are changed, the agent must
document which tool version generated them.

The agent must avoid regenerating files with a different tool version
when that would create unrelated diff noise.

### 15.23 Artifact exclusion gate

The agent must not stage or commit build outputs, local IDE files,
downloaded tools, installers, archives, logs, or generated binaries
unless explicitly requested.

Forbidden by default:

- `target/`, `*.class`
- `*.jar` produced by local build
- `.zip`, `.exe`, `.dll`, `*.log`
- downloaded ffmpeg, yt-dlp, aria2, curl, rtmpdump binaries
- IDE caches
- temporary screenshots, local reports, coverage outputs

If such files appear in `git status`, the agent must leave them
unstaged and mention them separately.

### 15.24 Network and live-service gate

For Habitv provider/plugin work, the agent must distinguish between:

- offline unit tests;
- live/network tests;
- manual real-application tests.

The agent must not rely only on live provider behavior as proof of
correctness.

When changing plugins or replay providers, the agent should prefer:

- offline fixtures;
- deterministic parser tests;
- focused module tests;
- manual live verification only as an additional check.

If live tests are skipped because of network instability, geoblocking,
authentication, DRM, or provider rate limits, the agent must say so
clearly.

### 15.25 PR comment action gate

Posting GitHub comments changes remote state.

The agent must ask for explicit approval before:

- replying to a PR comment;
- replying to Copilot;
- marking a conversation resolved;
- requesting review;
- re-requesting review;
- adding labels;
- editing PR title or body.

Before any PR comment action, the agent must show the exact English
comment it plans to post.

Review reply template when handled:

```text
Handled in commit <commit-sha or pending>. Summary: <short explanation>. Validation: <command/result or not applicable>.
```

When rejected:

```text
Not applied. Reason: <clear reason>. Risk/scope: <why it should not be changed in this PR>. Developer decision needed: <yes/no>.
```

See also Section 15.7 for resolution criteria.

### 15.26 Issue and follow-up gate

If the agent finds useful work outside the current PR scope, it must not
implement it silently.

Instead, list it under:

```markdown
## Follow-up candidates

* Title:
* Reason:
* Risk:
* Suggested branch:
* Suggested commit type:
```

The agent must not create GitHub issues unless explicitly approved.

### 15.27 No silent auto-fix gate

The agent must not silently fix unrelated warnings, formatting, imports,
lint issues, or IDE suggestions.

If an auto-fix tool changes unrelated files, the agent must revert those
unrelated changes or ask the developer.

For Habitv, avoid mixing in one PR:

- provider behavior changes;
- Java migration changes;
- dependency changes;
- packaging changes;
- CI changes;
- documentation-only changes.

Each category should normally be a separate PR.

### 15.28 Commit content gate

Each commit must be coherent and reviewable.

Before commit approval, the agent must classify the commit as exactly one:

- docs-only;
- test-only;
- code behavior;
- refactor-only;
- build/CI;
- packaging;
- dependency update;
- generated files;
- mixed with explicit approval.

If the commit is **mixed**, the agent must explain why it cannot be split
safely.

---

## ⚡ 16. Fast safe modernization workflow

These rules help agents move faster **without** reducing safety. Manual
approval gates (Section 14) and Java 8 compatibility (Section 2) remain
mandatory unless a dedicated migration task says otherwise.

Detailed companion docs: [`docs/dev-workflow.md`](docs/dev-workflow.md),
[`docs/dependency-policy.md`](docs/dependency-policy.md),
[`docs/security-policy.md`](docs/security-policy.md).

### 16.1 Fast progress gate

Optimize for small, reviewable, fast-to-merge PRs.

Default rules:

- one topic per branch;
- one coherent commit unless the developer asks for a split;
- no mixed PR combining code behavior, docs, dependency updates, CI,
  packaging, and refactoring;
- prefer incremental modernization over big-bang rewrites;
- keep every PR buildable on Java 8 unless it is a dedicated Java
  migration PR.

Recommended PR size:

| Size | Files changed | Rule |
|------|---------------|------|
| small | under 10 | preferred default |
| medium | 10–25 | acceptable with clear scope |
| large | over 25 | requires explicit developer approval before continuing |

If a task becomes larger than expected, stop and propose a split plan.

### 16.2 Speed-first task classification

Before editing, classify the task as exactly one:

- quick fix;
- provider/plugin fix;
- dependency/security update;
- CI/workflow update;
- IDE/tooling update;
- docs-only;
- refactor-only;
- packaging;
- migration preparation;
- investigation only.

Then apply the matching **rule profile** (Section 20.2) and validation
plan from Sections 6, 14.2, and 16.11.

### 16.3 Dependency update policy

Dependency updates must be handled in focused PRs. See
[`docs/dependency-policy.md`](docs/dependency-policy.md).

Allowed categories: security patch; patch update; minor update; major
update; plugin/tooling update; GitHub Actions update; Maven plugin update.

Rules:

- do not batch unrelated dependency updates unless explicitly requested;
- do not mix dependency updates with feature work;
- preserve Java 8 compatibility unless a dedicated migration task says
  otherwise;
- document old version, new version, reason, risk, and validation;
- check release notes or changelog when the update is non-trivial;
- run full Maven validation after any dependency change.

Before commit approval on dependency PRs, provide:

```markdown
## Dependency update report

* Dependency:
* Old version:
* New version:
* Update type:
* Reason:
* Java 8 compatibility:
* Release notes/changelog checked:
* Security impact:
* Validation commands:
* Known risks:
```

### 16.4 Security update policy

Security fixes have priority over cosmetic cleanup. See
[`docs/security-policy.md`](docs/security-policy.md) and [`SECURITY.md`](SECURITY.md).

When a security alert, Dependabot PR, CodeQL alert, dependency review
issue, or OWASP warning exists, the agent must:

- identify whether it affects runtime, test-only, plugin-only,
  build-only, or transitive scope;
- avoid suppressing alerts without explanation;
- prefer upgrading to a safe compatible version;
- document if no Java 8-compatible fix exists;
- avoid disabling security checks just to make CI green.

The agent must **not**: ignore security alerts; silence CodeQL without
justification; disable Dependabot; remove dependency review; weaken branch
protection; add broad workflow permissions.

### 16.5 Dependabot rule

Inspect [`.github/dependabot.yml`](.github/dependabot.yml) before
dependency work.

Current configuration targets `develop`, limits open PRs, groups Maven
minor/patch and GitHub Actions updates, and ignores major JAXB/mail bumps
that would violate Java 8 / no-jakarta rules.

Rules:

- group low-risk patch updates only when useful;
- keep security updates separate when possible;
- never enable auto-merge for dependency updates unless the developer
  explicitly asks;
- if Dependabot config needs changes, use a dedicated CI/tooling PR.

### 16.6 Dependency Review workflow rule

For dependency-changing PRs, require dependency review when available.
Current workflow: [`.github/workflows/dependency-review.yml`](.github/workflows/dependency-review.yml)
(fails on high severity).

The workflow should fail or warn when a new vulnerable dependency is
introduced or a dependency diff is suspicious.

If Dependency Review is not available for the repository plan, document
that limitation instead of pretending it is enforced.

### 16.7 CodeQL rule

CodeQL should remain enabled for Java and GitHub Actions when possible.
See `docs/required-checks-roadmap.md` for rollout status.

Rules:

- do not remove CodeQL workflows without explicit approval;
- do not suppress CodeQL alerts without a clear reason;
- for Java/Maven, ensure CodeQL build mode matches project reality;
- if CodeQL fails because the Maven project requires Java 8, document
  JDK setup and fix the workflow instead of disabling analysis;
- use least required permissions.

### 16.8 GitHub Actions security rule

All GitHub Actions workflows must use least-privilege permissions.

Default:

```yaml
permissions:
  contents: read
```

Only add write permissions when the job really needs them.

Rules:

- avoid `pull_request_target` unless explicitly justified;
- pin actions to trusted major versions at minimum;
- prefer official actions for checkout, setup-java, cache, CodeQL,
  dependency-review;
- do not expose secrets to PRs from forks;
- do not echo secrets, tokens, cookies, or headers;
- do not add workflow steps that execute untrusted PR text as shell
  commands.

Before changing workflows, provide:

```markdown
## Workflow safety checklist

* Workflow changed:
* Trigger changed:
* Permissions before:
* Permissions after:
* Secrets used:
* Third-party actions added:
* Java version used:
* Maven command used:
* Security risk:
```

### 16.9 CI speed rule

CI should be fast but meaningful. Current primary workflow:
[`.github/workflows/ci-maven.yml`](.github/workflows/ci-maven.yml).

Recommended split when adding workflows:

| Workflow | Purpose |
|----------|---------|
| validate | Maven validate / compile / tests on Java 8 |
| security | CodeQL, dependency review, OWASP if stable |
| docs | Markdown/docs checks if useful |
| package | packaging only when packaging changes or manual trigger |

Rules:

- avoid heavy packaging on every small PR unless needed;
- use path filters where useful;
- cache Maven dependencies safely;
- never cache corrupted security databases if they cause recurring failures;
- keep a full validation path available before merge.

### 16.10 Java compatibility roadmap rule

Habitv currently preserves Java 8 compatibility.

Rules:

- default source/target remains Java 8 unless a dedicated migration PR
  changes it;
- modern Java migration work must be split into preparation PRs;
- do not introduce APIs unavailable in Java 8;
- do not upgrade Maven plugins to versions requiring a newer JDK unless
  the PR is explicitly a migration step;
- document compatibility impact for every build plugin or dependency
  update.

Migration roadmap categories: Java 8 stabilization; JavaFX packaging
clarification; Java 11 preparation; Java 17 preparation; Java 21/25 future
migration.

Do not claim Java 11/17/21/25 support until CI proves it. Use wording:
**current support**, **planned migration**, **experimental**, or **not
yet validated**.

### 16.11 Maven quality gate

For Maven changes, prefer explicit validation per Section 14.2 tiers.

**Standard code** (typical module or POM change):

```bash
mvn -B -ntp validate
mvn -B -ntp -pl <module> -am test   # when module-specific
```

**Risky / code-wide** changes:

```bash
mvn -B -ntp validate
mvn -B -ntp test
```

**Packaging / release / full-app** (only when in scope and JavaFX/`jdk.home`
environment supports it):

```bash
mvn -B -ntp -DskipTests clean package
```

For dependency analysis:

```bash
mvn -B -ntp dependency:tree
mvn -B -ntp versions:display-dependency-updates
mvn -B -ntp versions:display-plugin-updates
```

Do not commit POM changes without explaining why the dependency or
plugin change is needed.

### 16.12 Maven Enforcer rule

If Maven Enforcer is present, inspect and improve it carefully in a
dedicated build-quality PR.

If missing, propose Enforcer only in that dedicated PR — not mixed with
feature work.

Potential rules: require Java 8; require Maven minimum version; ban
duplicate dependencies; require plugin versions; dependency convergence
only if it does not create excessive noise.

### 16.13 Cursor IDE rules

Cursor configuration must help agents work faster and consistently.

Project-level:

- store repository rules in `.cursor/rules/`;
- keep `AGENTS.md` as canonical source of truth;
- Cursor rules must not contradict `AGENTS.md`;
- split rules by topic (git, Maven, Java 8, PR, dependencies, workflows,
  plugins).

User-level Cursor settings are for personal preferences only. Never rely
on user settings for repository-critical behavior.

The agent must inspect repository rules first; never assume local Cursor
settings are correct.

### 16.14 Cursor recommended extensions gate

Maintain [`.vscode/extensions.json`](.vscode/extensions.json) with
recommended extensions. Do not force installation.

Suggested categories: Java, Maven, Java test/debug, GitHub PRs, GitHub
Actions, Copilot (if used), YAML, EditorConfig, Markdown.

### 16.15 Cursor workspace settings gate

Do not commit machine-specific Java paths.

Forbidden in committed workspace settings: personal absolute paths; local
username paths; machine-specific `JAVA_HOME`; tokens; private tool paths.

Allowed: [`.vscode/settings.example.json`](.vscode/settings.example.json)
with documented examples; safe editor settings; format-on-save only if it
does not create massive unrelated diffs.

Prefer documenting local Java 8 setup in [`docs/dev-workflow.md`](docs/dev-workflow.md).
Keep machine-specific settings local and gitignored.

### 16.16 Cursor task commands

Add [`.vscode/tasks.json`](.vscode/tasks.json) only in a dedicated tooling
PR.

Recommended tasks: Maven validate; clean package (skip tests); test;
targeted module test; git status; git diff — **never** tasks that commit,
push, force-push, merge, or resolve PR comments.

### 16.17 Agent startup checklist

At the start of each task, report:

```markdown
## Agent startup checklist

* Instruction files loaded:
* Current branch:
* Base branch:
* Task classification:
* Expected files to touch:
* Validation plan:
* Risk level:
* Needs web/release-note lookup: yes/no
* Needs developer decision: yes/no
```

See also Section 15.1 (instruction loading).

### 16.18 Decision speed rule

Move fast with safe defaults when scope is clear, change is low risk,
validation can prove correctness, and no remote state change is needed.

The agent **must ask** before: commit; push; force-with-lease push; PR
creation; PR merge; resolving comments; dependency major updates; Java
version migration; destructive commands; security suppression; workflow
permission broadening.

### 16.19 PR queue prioritization rule

When multiple tasks exist, prioritize:

1. build is broken
2. security alert or vulnerable dependency
3. CI/workflow failure blocking merges
4. PR comments blocking review
5. dependency patch/minor updates
6. provider/plugin fixes
7. tests and fixtures
8. packaging
9. docs cleanup
10. refactor
11. future Java migration preparation

### 16.20 Provider/plugin update rule

For replay provider work:

- prefer deterministic parser tests with offline fixtures;
- avoid relying only on live network tests;
- document authentication, cookies, DRM, geoblocking, or browser session
  requirements;
- do not bypass DRM;
- do not commit cookies, tokens, or browser profiles;
- use yt-dlp behavior as integration inspiration only when legally and
  technically appropriate;
- keep each provider change isolated.

See also Section 15.24 and [`plugins/AGENTS.md`](plugins/AGENTS.md).

### 16.21 Documentation freshness rule

When documentation mentions versions, Java support, workflow behavior,
packaging, or external tools, verify the statement matches current
repository state.

Do not document future support as already available.

### 16.22 Release notes and changelog gate

Before non-trivial updates, inspect release notes or changelogs when
available (Maven plugins, GitHub Actions, JavaFX/OpenJFX, yt-dlp,
security tools, dependency major/minor updates, packaging tools).

If release notes were not checked, say so explicitly.

### 16.23 Auto-formatting gate

Do not enable broad auto-formatting that changes many legacy files.
Formatting must be explicit, scoped, and preferably a dedicated formatting
PR separate from behavior changes.

### 16.24 Branch naming for fast work

Use predictable branch names: `fix/`, `feat/`, `docs/`, `test/`, `ci/`,
`chore/`, `refactor/` + short topic.

Avoid: `claude/*`, `cursor/*`, random generated names, ticket/style IDs,
vague names like `update-stuff`. See Section 4.

### 16.25 Final task report

Every completed task must end with:

```markdown
## Final report

* Task classification:
* Branch:
* Files changed:
* Summary:
* Validation:
* Manual tests requested:
* Security impact:
* Dependency impact:
* Java 8 compatibility:
* Copilot/GitHub comments:
* Follow-up candidates:
* Proposed commit:
* Next action requiring approval:
```

---

## 🔄 17. Evolutionary rules governance

AI rules must evolve with project needs, repository maturity, tooling,
security requirements, dependency updates, and developer feedback. Rules
must be easy to update, version, audit, simplify, and retire.

Companion docs: [`docs/agent-rules-changelog.md`](docs/agent-rules-changelog.md),
[`docs/agent-rules-backlog.md`](docs/agent-rules-backlog.md).

Section 13 governs **hard-rule** ADR changes. This section governs **AI
workflow rule** evolution.

### 17.1 Evolutionary rules governance

`AGENTS.md` remains the canonical source of truth.

All other AI instruction files must reference, summarize, or extend
`AGENTS.md` — never contradict it.

AI rules evolve through **normal pull requests** dedicated to rule changes
or clearly scoped docs PRs. Do not silently change rules during unrelated
feature, fix, dependency, or provider work.

Rule changes must be explicit, reviewable, and recorded in the changelog.

### 17.2 Rule lifecycle states

Classify important rules as one of:

| State | Meaning |
|-------|---------|
| **mandatory** | must follow unless developer explicitly overrides |
| **recommended** | default best practice; adapt with justification |
| **experimental** | being tested; review after a few PRs |
| **deprecated** | temporary; remove soon |
| **removed** | inactive; history in changelog only |

Do not keep obsolete **mandatory** rules indefinitely.

### 17.3 Rule review cadence

Review rules:

- after **5 merged PRs** touching agent workflow or CI;
- after any **repeated agent mistake**;
- after any **failed or polluted PR** caused by AI behavior;
- after dependency/security workflow changes;
- after Java compatibility milestones;
- before large migrations;
- before enabling new automation.

Suggest a rule update when the same problem happens **twice**.

### 17.4 Rule improvement trigger

Propose a rule update when:

- commit/push happened too early;
- merge commits appeared on a PR branch;
- Copilot comments resolved without handling;
- validation skipped without clear status;
- PR mixed unrelated changes;
- dependency updates bundled badly;
- CI failed from missing workflow rule;
- security alert ignored or suppressed badly;
- Java 8 compatibility broken accidentally;
- generated files or binaries committed;
- branch/PR naming conventions violated.

Use:

```markdown
## Suggested rule improvement

* Problem observed:
* Proposed rule:
* Files to update:
* Risk if not added:
* Suggested commit:
```

Add to [`docs/agent-rules-backlog.md`](docs/agent-rules-backlog.md) if not
implemented immediately.

### 17.5 Rule simplification gate

When updating rules, check for: duplication; contradiction; obsolete text;
too many examples; vague unverifiable rules; rules that slow simple tasks.

If too long, split into:

- mandatory short rule in `AGENTS.md`;
- detailed explanation in `docs/dev-workflow.md`;
- path-specific detail in nested `AGENTS.md` or
  `.github/instructions/*.instructions.md`.

### 17.6 Rule drift audit

Before finishing any AI-rule update, audit:

- `AGENTS.md`
- `.cursor/rules/*`
- `.github/copilot-instructions.md`
- `.github/instructions/*.instructions.md`
- `CLAUDE.md`, `GEMINI.md`
- `docs/dev-workflow.md`
- `docs/agent-rules-changelog.md`

Report:

```markdown
## Rule drift audit

* Files checked:
* Conflicts found:
* Conflicts resolved:
* Files intentionally left unchanged:
* Reason:
```

### 17.7 Cursor rule evolution

Cursor rules must be modular. See [`.cursor/rules/README.md`](.cursor/rules/README.md).

Each `.mdc` file must include: purpose; when it applies; mandatory rules;
examples only if useful; reference to `AGENTS.md`.

Cursor rules must **not** contain unique critical policy missing from
`AGENTS.md`.

Recommended numbered modules (current names in parentheses):

| Module | Topic |
|--------|-------|
| `00-canonical-source.mdc` | canonical pointer |
| `10-git-safety.mdc` | (`git-safety.mdc`) |
| `20-validation.mdc` | (`maven-validation.mdc`) |
| `30-java8-maven.mdc` | (`java8-compatibility.mdc`) |
| `40-pr-review.mdc` | (`pr-review.mdc`) |
| `50-dependencies-security.mdc` | (`dependencies.mdc`) |
| `60-workflows-ci.mdc` | (`workflows.mdc`) |
| `70-provider-plugins.mdc` | (`plugins/AGENTS.md`) |
| `90-rule-evolution.mdc` | evolution and audit |

### 17.8 GitHub Copilot instruction evolution

Repository-wide: `.github/copilot-instructions.md`.

Path-specific: `.github/instructions/*.instructions.md` — create only when
they reduce noise or improve accuracy.

Each path-specific file must state: files it applies to; how it extends
`AGENTS.md`; validation expected; what not to change.

### 17.9 Rule retirement policy

Remove or downgrade rules when:

- the problem no longer occurs;
- CI or branch protection now enforces it;
- it duplicates a stronger rule;
- it slows work without preventing real issues;
- it was temporary for a completed migration.

Document retirement in `docs/agent-rules-changelog.md`.

### 17.10 Automation maturity levels

| Level | Name | Description |
|-------|------|-------------|
| 0 | Manual only | agent proposes; developer approves all remote actions |
| 1 | Assisted | agent validates locally; developer approves commit/push/PR |
| 2 | Guarded automation | selected automation only when CI + protection reliable |
| 3 | Trusted automation | low-risk repetitive tasks only; explicit decision |

**Current Habitv default: Level 1 — Assisted.**

Do not move to a higher level without a dedicated PR and developer
approval.

### 17.11 Rule scoring

Before adding a new **mandatory** rule, score it:

```markdown
## Rule score

* Prevents real recurring problem: yes/no
* Easy to verify: yes/no
* Low maintenance cost: yes/no
* Does not slow simple tasks too much: yes/no
* Belongs in AGENTS.md instead of docs: yes/no
```

Add as mandatory only when most answers are **yes**. Otherwise use
recommended, experimental, or backlog.

### 17.12 Experimental rules

Experimental rules must document:

- start date; reason; success criteria; review date; owner; rollback
  condition.

Example template in [`docs/agent-rules-backlog.md`](docs/agent-rules-backlog.md).

### 17.13 Progress-aware rules

Identify the current project phase before major work (full list: Section
18.1). See [`docs/dev-plan.md`](docs/dev-plan.md).

### 17.14 Agent rules update report

Every AI-rule update must end with:

```markdown
## Agent rules update report

* Rule version before:
* Rule version after:
* Files updated:
* Rules added:
* Rules changed:
* Rules removed:
* Rules deprecated:
* Drift audit:
* Backlog updated:
* Changelog updated:
* Validation:
* Next review trigger:
```

---

## 📺 18. Habitv-specific modernization rules

Habitv-specific rules for providers, dependencies, security tooling, CI,
IDE setup, packaging, and Java compatibility. Companion docs:
[`docs/provider-policy.md`](docs/provider-policy.md),
[`docs/release-policy.md`](docs/release-policy.md),
[`docs/modernization-backlog.md`](docs/modernization-backlog.md),
[`docs/obsolescence-register.md`](docs/obsolescence-register.md).

General gates in Sections 14–17 still apply.

### 18.1 Habitv project phase gate

Before making changes, identify the current work **phase** (exactly one):

- stabilization;
- provider/plugin restoration;
- metadata enrichment;
- dependency/security update;
- CI hardening;
- IDE/tooling setup;
- packaging;
- release preparation;
- Java migration preparation;
- Java migration execution.

Each task must declare: **phase**; **risk level**; **expected files**;
**validation plan**; whether **Java 8 compatibility** is affected.

### 18.2 Provider status matrix rule

Replay providers/plugins must have a clear status in
[`docs/provider-inventory.md`](docs/provider-inventory.md) and/or
[`docs/obsolescence-register.md`](docs/obsolescence-register.md).

| Status | Meaning |
|--------|---------|
| **working** | validated with tests and real behavior when possible |
| **degraded** | partial function; known metadata or endpoint gaps |
| **protected** | auth, cookies, DRM, geoblocking, or anti-bot |
| **obsolete** | dead or replaced endpoint/platform |
| **removed** | intentionally removed from active UI/plugin list |
| **unknown** | not recently validated |

When changing a provider, update or propose updating status documentation.

### 18.3 Provider data quality contract

Preserve or improve metadata extraction when feasible. Desired fields:
channel; program title; episode title; broadcast date; duration; summary;
thumbnail; season/episode numbers when available; replay URL; download URL
or delegated downloader command; availability/end date when available.

If metadata is unavailable, document whether it is: not exposed; auth-protected;
JavaScript/API-only; or out of scope for the PR.

Do **not** claim metadata support without validation.

### 18.4 Provider testing rule

Prefer deterministic offline tests: fixtures; parser tests; URL extraction;
command-building; targeted `mvn -pl plugins/<module> -am test`.

Live/network checks are supplementary — not the only proof. If skipped, state
why (network, geoblocking, auth, DRM, rate limits, downtime).

Do not bypass DRM. Do not commit cookies, browser profiles, sessions, tokens,
or auth headers. See also Section 15.24.

### 18.5 External tools policy

External tools include yt-dlp, ffmpeg, aria2, rclone, curl, rtmpdump.

Rules:

- do not silently replace or upgrade tools;
- dedicated PRs for tool updates;
- document source URL, version, reason, compatibility, checksum when possible;
- do not commit downloaded binaries unless explicitly requested;
- do not mix tool updates with provider behavior changes;
- validate command-building with tests when possible.

See [`docs/external-tools-recommendations.md`](docs/external-tools-recommendations.md).

Before commit approval on tool updates, provide **External tool update
report** (template in [`docs/provider-policy.md`](docs/provider-policy.md)).

### 18.6 Java 8 compatibility rule

Remain Java 8-compatible by default. Do not introduce Java 9+ APIs or
language features, Maven plugins requiring newer JDK (unless dedicated
migration PR), or dependencies dropping Java 8 without approval.

Split migration work: Java 8 stabilization → 11/17/21/25 preparation →
runtime migration. Do not claim newer Java support until CI validates it.

See [`docs/java-runtime-policy.md`](docs/java-runtime-policy.md) and
`.cursor/rules/java8-compatibility.mdc`.

### 18.7 JavaFX compatibility rule

Distinguish:

- historical JDK 8 with bundled JavaFX (`jfxrt`);
- modern Java 8 distributions that **may** bundle JavaFX (selected Zulu,
  Liberica);
- newer Java where JavaFX is typically separate (OpenJFX / packaging).

Do **not** write that JavaFX is always bundled with Java. Document validated
local setup and future migration plan separately when changing JavaFX docs.

### 18.8 Dependency/security modernization lane

Focused PRs only. Priority order:

1. vulnerable dependencies;
2. broken CI/security workflows;
3. outdated GitHub Actions;
4. Maven plugin updates;
5. low-risk patches;
6. minors;
7. majors (dedicated review).

Do not mix with feature work. Check release notes. Preserve Java 8. Run full
Maven validation. Use **Dependency update report** (Section 16.3).

See [`docs/dependency-policy.md`](docs/dependency-policy.md).

### 18.9 SBOM rule

SBOM generation (e.g. CycloneDX Maven plugin) belongs in a **dedicated
security/tooling PR** — not unrelated work.

**Prerequisites before enablement or CI changes:**

- a dedicated tracker item;
- a dedicated security/tooling PR;
- an ADR when the change affects repository policy or CI behavior;
- no enablement in unrelated PRs.

Section 2 still forbids adding OWASP/SBOM/static-analysis plugins without
an explicit tracker item and ADR.

Document where SBOM is generated. Do not commit generated SBOM files unless
required. Prefer CI artifact upload.

### 18.10 OWASP Dependency-Check rule

If OWASP Dependency-Check is used:

**Prerequisites before enablement or CI changes:**

- a dedicated tracker item;
- a dedicated security/tooling PR;
- an ADR when the change affects repository policy or CI behavior;
- no enablement in unrelated PRs.

Section 2 still forbids adding OWASP/SBOM/static-analysis plugins without
an explicit tracker item and ADR.

Operational rules:

- do not disable it to green CI;
- do not cache corrupted vulnerability databases;
- use stable cache strategy;
- NVD API key only via GitHub secrets — never hard-coded;
- document suppressions with reason and expiry/review date.

Suppression template in [`docs/security-policy.md`](docs/security-policy.md).

### 18.11 CodeQL rule

Do not remove CodeQL without approval. Do not suppress without explanation.
Minimal permissions. Match Java/Maven build to Habitv reality. Fix Java 8 setup
instead of disabling. See Section 16.7 and `docs/security-policy.md`.

### 18.12 OpenSSF Scorecard rule

Propose Scorecard in a **dedicated security PR** only. Document permissions,
schedule, Security tab usage, expected noise, and follow-ups.

### 18.13 GitHub Actions workflow tiers

| Tier | Purpose |
|------|---------|
| PR fast validation | compile/tests on Java 8 |
| targeted module | path-filtered module tests |
| security | CodeQL, dependency review, OWASP, SBOM |
| packaging | packaging changes or manual trigger |
| scheduled maintenance | dependency/security scans |

Avoid heavy packaging on every PR. Stable CI names for branch protection.
Least privilege. No `pull_request_target` unless justified. No secrets to fork
PRs. See Section 16.9 and `.cursor/rules/workflows.mdc`.

### 18.14 IDE and Cursor project setup rule

Repository-critical IDE setup lives in the repo:

- `.vscode/extensions.json`, `.vscode/settings.example.json`;
- `docs/dev-workflow.md`;
- `.cursor/rules/README.md`.

Recommend extensions; do not force. No machine-specific Java paths. Document
Java 8 setup, Maven commands, Java project reload, and known Cursor Java
pitfalls. See Section 16.13–16.16.

### 18.15 Runtime diagnostics rule

For runtime changes: avoid full stack traces for known provider endpoint
failures; log short root-cause messages for expected failures; keep debug
detail where useful; do not hide unexpected exceptions silently.

### 18.16 Obsolescence register rule

Maintain [`docs/obsolescence-register.md`](docs/obsolescence-register.md):
obsolete provider; old endpoint; replacement; status; removal plan; risk; PR.

Do not delete legacy providers silently — mark, replace, deprecate, or remove
in focused PRs.

### 18.17 Release readiness rule

Before release/packaging work, verify full build, tests, provider status,
dependency/security status, external tool versions, Java/JavaFX assumptions,
packaging scripts, changelog, and known limitations.

Provide **Release readiness report** (template in
[`docs/release-policy.md`](docs/release-policy.md)).

### 18.18 Fast modernization backlog

Track future work in [`docs/modernization-backlog.md`](docs/modernization-backlog.md)
instead of bloating `AGENTS.md`. Categories: build blockers; security;
dependencies; CI; providers; metadata; external tools; packaging; Java
migration; documentation; cleanup/removal.

### 18.19 Habitv task final report

Every completed Habitv task must end with:

```markdown
## Final report

* Phase:
* Task classification:
* Branch:
* Files changed:
* Provider impact:
* Dependency impact:
* Security impact:
* Java 8 compatibility:
* JavaFX impact:
* External tool impact:
* Validation:
* Manual tests requested:
* Known limitations:
* Follow-up candidates:
* Proposed commit:
* Next action requiring approval:
```

Section 16.25 is the generic minimum; Section 18.19 adds Habitv-specific
fields; Section 19.25 adds maintainability fields.

---

## 🛠️ 19. Habitv maintainability guardrails

Second-level rules for long-term maintainability, reproducible builds,
dependency safety, configuration compatibility, test reliability, and
decision tracking. Companion: [`docs/maintainability-policy.md`](docs/maintainability-policy.md).

Sections 14–18 still apply.

### 19.1 Decision record rule

Document architectural, workflow, dependency, Java compatibility,
packaging, or provider strategy decisions.

- **Existing ADRs:** [`docs/decision-log.md`](docs/decision-log.md) (append-only).
- **New ADR files:** [`docs/adr/`](docs/adr/) with template in
  [`docs/adr/README.md`](docs/adr/README.md).

Do not hide durable decisions only in PR comments. Supersede old ADRs;
do not contradict silently.

### 19.2 Definition of Done rule

Declare **Definition of Done** before implementation:

- code change complete;
- relevant tests added or updated;
- Maven validation run;
- manual real-app test requested;
- Java 8 compatibility preserved;
- no unrelated files changed;
- no secrets or binaries staged;
- Copilot/GitHub comments handled when applicable;
- final report written;
- developer approval requested before commit/push.

If DoD cannot be met: **Not ready to commit**.

### 19.3 Reproducible build rule

Improve reproducibility gradually:

- no timestamps, local paths, random ordering, or machine-specific outputs
  in generated artifacts;
- avoid committing machine-varying generated files;
- document tool versions when build output changes;
- prefer deterministic packaging when in scope;
- no reproducible-build tooling in unrelated PRs.

For Maven/build PRs, consider: `project.build.outputTimestamp`; pinned
plugin versions; Maven Toolchains; pinned CI JDK; checksum/provenance.

### 19.4 Maven Toolchains rule

For Java compatibility work, prefer Maven Toolchains or documented JDK
setup — not developer machine defaults.

- no hard-coded local JDK paths in committed files;
- document Java 8 separately from 11/17/21/25 experiments;
- toolchains only in dedicated build/tooling PRs;
- CI and local docs must agree;
- do not claim multi-JDK support without CI validation.

### 19.5 Renovate and Dependabot strategy rule

Dependency automation must reduce noise, not create it.

**Current:** [`.github/dependabot.yml`](.github/dependabot.yml) — Maven +
GitHub Actions, targets `develop`, limited open PRs.

Allowed: Dependabot only; Renovate only; both with separated responsibility.
Renovate requires dedicated PR; no automerge by default. Never auto-merge
dependencies without explicit developer approval.

See [`docs/dependency-policy.md`](docs/dependency-policy.md).

### 19.6 Dependency provenance rule

Document provenance for dependencies and external tools:

name; ecosystem; source repo/site; license; old/new version; Java 8
compatibility; security reason; release notes checked; migration notes;
validation performed.

No random mirrors or untrusted binaries. Template in
[`docs/maintainability-policy.md`](docs/maintainability-policy.md).

### 19.7 EditorConfig and formatting rule

Style is repository-defined, not IDE-specific.

If [`.editorconfig`](.editorconfig) exists: follow it; do not contradict
in Cursor/VS Code settings. If missing: propose in dedicated tooling PR
only.

No broad legacy reformatting mixed with behavior changes (Section 16.23).

### 19.8 Test reliability and flaky test rule

Classify test failures: related; unrelated existing; flaky; environment/
network; skipped by design; needs investigation.

Do not ignore failures. For flaky tests: document command, frequency,
cause, commit blocker status, fix/quarantine plan. Do not disable tests
without developer approval.

### 19.9 Network test rule

Provider tests must not depend only on live network (see Section 18.4).

Live tests clearly labeled; CI must not fail randomly on provider downtime,
geoblocking, DRM, auth, or rate limits. Prefer offline fixtures; live
checks optional, manual, or scheduled.

### 19.10 XML configuration compatibility rule

Protect legacy XML config compatibility when changing config classes,
schema, JAXB models, or defaults:

- document backward compatibility impact;
- preserve existing user configs when possible;
- add migration logic and old-config tests if needed;
- do not remove fields or change defaults silently.

If uncertain: **Needs developer decision**.

### 19.11 User data safety rule

Do not risk user data loss when changing download paths, config storage,
plugin updates, or cleanup:

- do not delete user files or overwrite downloads;
- do not change default directories silently;
- document migration; request manual real-app testing.

### 19.12 External downloader command safety rule

For yt-dlp, ffmpeg, aria2, curl, rclone commands:

- log safely without secrets;
- never print cookies, tokens, auth headers, or session paths;
- quote Windows paths; preserve cross-platform behavior;
- test command construction where possible;
- document manual testing.

### 19.13 Windows-first validation rule

Primary development is on Windows. For launch, packaging, paths, scripts,
process execution, or external tools:

- validate Windows explicitly;
- quote paths with spaces;
- test with local Java 8 when possible;
- keep PowerShell/Bash consistent;
- do not assume Linux-only paths.

### 19.14 Cross-platform script parity rule

When both `.sh` and `.ps1` exist: update both or explain why not; align
arguments and output; validate syntax; document platform differences.
See [`scripts/AGENTS.md`](scripts/AGENTS.md).

### 19.15 Manual test evidence rule

Before commit approval on behavior changes, request or record manual
test evidence. Template in [`docs/maintainability-policy.md`](docs/maintainability-policy.md).

If manual testing not done: **Ready for developer manual testing** — not
**Ready for commit approval**.

### 19.16 PR risk label rule

Classify risk: **low** (docs/narrow fix); **medium** (provider, Maven, CI,
dep patch/minor); **high** (dep major, Java migration, packaging, config
migration, external tools); **critical** (security, release, destructive
migration, DRM/auth/cookies).

Validation scales with risk (Section 19.2, 14.2).

### 19.17 Rollback plan rule

For medium/high/critical changes, provide rollback plan: files to revert;
PR strategy; user impact; data compatibility; post-release safety.

### 19.18 Feature flag and config default rule

Conservative defaults: risky behavior off unless requested; document flags;
backward compatible; test enabled/disabled; no silent user-visible changes.

### 19.19 Logging policy rule

Concise warnings for expected provider failures; debug detail for
unexpected errors; never log secrets unnecessarily. See Section 18.15.

### 19.20 Performance and timeout rule

For downloaders, scans, startup, tool preflight: avoid overly short
timeouts; document rationale; account for slow Windows startup; avoid
blocking UI threads.

### 19.21 UI behavior rule

JavaFX/UI: Java 8/JavaFX compatibility; no modern JavaFX APIs; manual UI
testing; do not mix UI with provider/backend unless necessary.

### 19.22 Error message quality rule

User-facing errors: what failed; likely cause; retry usefulness;
obsolete/protected status; user action; log location. No raw stack traces
for expected failures.

### 19.23 Worktree rule

For parallel PR work, prefer Git worktrees over dirty branch switching.

One branch per worktree; no shared uncommitted files; document path in
handoff; delete obsolete worktrees only with approval.

### 19.24 Maintenance dashboard rule

Maintain [`docs/maintenance-dashboard.md`](docs/maintenance-dashboard.md).
Update in dedicated docs/planning PRs or when directly relevant.

### 19.25 Maintainability report extension

Extend the final report (Section 18.19) with:

```markdown
## Maintainability report

* Rule impact:
* Reproducibility impact:
* Config compatibility:
* User data safety:
* Test reliability:
* Rollback plan:
* Manual evidence needed:
* Maintenance dashboard update needed:
```

---

## ⚡ 20. Productivity and anti-bloat guardrails

Keep AI rules **useful, fast, and maintainable**. Detail:
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md).

Sections 14–19 still apply; this section controls **how much** applies per task.

### 20.1 Minimum viable ruleset rule

`AGENTS.md` = mandatory safety, workflow, validation commands, Java/Maven,
Git/PR gates, and links — not long examples or edge cases. See file list in
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#minimum-viable-ruleset).

### 20.2 Rule profile system

Pick one profile at task start (extends Section 16.2). Profiles, validation,
risk, allowed files, and report sections:
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#profile-matrix).

Do not apply release or migration rules to docs-only tasks.

### 20.3 Lightweight path for docs-only changes

No Maven unless build/runtime docs change; verify Markdown and scope; drift
audit if AI rules changed (Section 17.6). May reach **Ready for commit
approval** after review. Details:
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#docs-only-lightweight-path).

### 20.4 Heavy gate only when needed

Heavy validation for code, build, providers, packaging, tools, Java/JavaFX,
config/XML/JAXB, and security — not unrelated docs. Scope list in
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#heavy-gate-scope).

### 20.5 Rule conflict resolver

Precedence order in
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#conflict-precedence).
If unresolved: **Needs developer decision**.

### 20.6 Rule budget

Before adding a mandatory rule: replace, merge, or simplify an existing one.
Avoid duplicates and unverifiable rules. Prefer one short rule + linked doc +
backlog item. Checklist in
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#rule-budget).

### 20.7 Rule archive policy

Retire inactive rules to changelog, backlog, or
[`docs/archived-agent-rules.md`](docs/archived-agent-rules.md) — not active
mandatory sections.

### 20.8 Agent speed mode

Low-risk docs-only, typo, narrow test fix, isolated config documentation.
Still: `git status`, changed-files summary, final report, **Approve commit?**,
approval gates. Skips heavy Maven/release/dep/provider/packaging checks.
Details: [`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#speed-mode-vs-deep-mode).

### 20.9 Deep mode

Provider/dependency/Maven/CI/packaging/migration/release/downloaders/config
work. Requires full validation plan, risk, rollback, manual testing, and
impact reports. Details: same link as 20.8.

### 20.10 Stop condition rule

Do not add mandatory rules that duplicate CI, document one-time issues, or
slow common tasks. When unsure, use
[`docs/agent-rules-backlog.md`](docs/agent-rules-backlog.md). Criteria in
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#stop-conditions).

### 20.11 Rule cleanup PR cadence

Every ~10 merged PRs, propose `docs/cleanup-agent-rules` with
`docs(ai-rules): clean up obsolete agent guidance`. See
[`docs/agent-rule-profiles.md`](docs/agent-rule-profiles.md#rule-cleanup-cadence).

### 20.12 Enforcement over expansion

Prefer enforcing rules via branch protection, CI, CODEOWNERS, Dependabot,
Dependency Review, CodeQL, Maven validation, small PRs, and provider tests —
not endless mandatory text. Track gaps in
[`docs/agent-rules-backlog.md`](docs/agent-rules-backlog.md#enforcement-over-expansion-section-2012).

# 🤖 AGENTS.md — Rules for AI coding agents

Instructions for **Codex, Cursor, Claude, Copilot** and any other AI
coding agent working on the Habitv repository. These instructions
complement, but do not replace, the human review process.

**Source of truth policy** — `AGENTS.md` is the single source of truth
for repository-wide AI agent workflow policy (branch naming, duplicate
prevention, commits, PR structure, validation, linear history, and
documentation sync).

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
`<type>/<tracker-or-pr-id>-<short-scope>`

Examples:
- `fix/hbtv-006-youtube-ytdlp`
- `fix/pr-91-canalplus-cstar`
- `chore/hbtv-012-remove-obsolete-providers`
- `test/hbtv-006-provider-offline-fixtures`
- `ci/hbtv-020-maven-pr-validation`
- `docs/hbtv-006-provider-inventory`

Before creating any branch, run:
```bash
git fetch --all --prune
git branch -a --list "*<scope>*"
gh pr list --state open --search "<scope>"
git check-ref-format --branch "<branch-name>"
```

If an existing branch or open PR already covers the same scope, do not
create a duplicate. Reuse the existing branch/PR, or create a clearly
scoped follow-up branch.

Keep history linear on work branches (no merge commits).

---

## 🎯 5. PR policy

| Rule | Detail |
|------|--------|
| Tracker reference | Reference one work-item slug (e.g. `legacy-url-migration`) in the PR body |
| Template | Fill every section of `.github/pull_request_template.md` |
| Diff size | Keep small and focused; reject opportunistic refactors |
| History | Linear inside work branches; no merge commits |
| Doc sync | Update tracker, risk register, decision log on meaningful changes |
| Plugin version bump | A `plugins/*/pom.xml` `<version>` override must match a `plugin-versioning-policy` trigger (downloader/parser, user-facing endpoint, user-facing configuration). Name the trigger in the PR body. Internal reactor deps in a bumped plugin MUST use `${project.parent.version}`. |

AI-agent PR target policy:
- Agent-created PRs must always target `Mika3578/habitv`.
- Agents must never open PRs directly against `ikfon10/habitv`.
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

Required PR body sections:
1. `Summary`
2. `Changes`
3. `Validation`
4. `Risk / rollback`
5. `Notes`

Before opening a PR:
1. Verify no open PR already covers the same scope.
2. Verify the branch is based on the latest `develop`.
3. Run:
   - `git status --short`
   - `mvn -B -ntp -DskipTests validate`
4. Include exact validation results in the PR body.

---

## 🧪 6. Validation policy

Default validation command for this phase:

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
| 🟠 **Process rule** | Sections 3–12 | PR amended; warning logged |
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

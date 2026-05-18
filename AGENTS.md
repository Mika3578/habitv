# 🤖 AGENTS.md — Rules for AI coding agents

Instructions for **Codex, Cursor, Claude, Copilot** and any other AI
coding agent working on the Habitv repository. These instructions
complement, but do not replace, the human review process.

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
| Restructure Maven reactor topology | High blast radius; tracked under HBTV-001 |
| Bump Java baseline beyond **Java 8** | JavaFX 2.x and `javax.xml.bind` 2.0 assume JDK 8 |
| Migrate JavaFX (`jfxrt`) to OpenJFX | Tracked under HBTV-008 |
| Regenerate JAXB classes or move to `jakarta.*` | Risk R-004 |
| Replace `youtube-dl` plugin behavior with `yt-dlp` | Tracked under HBTV-007 |
| Change runtime updater URLs or layout | Tracked under HBTV-005 |
| Migrate FTP/HTTP repositories | Tracked under HBTV-004 |
| Remove or rename provider/plugin modules | Tracked under HBTV-006 |
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

## 🎯 4. PR policy

| Rule | Detail |
|------|--------|
| Tracker reference | Reference one `HBTV-XXX` in the PR body |
| Template | Fill every section of `.github/pull_request_template.md` |
| Diff size | Keep small and focused; reject opportunistic refactors |
| History | Linear inside feature branches; no merge commits |
| Doc sync | Update tracker, risk register, decision log on meaningful changes |

---

## 🧪 5. Validation policy

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
| `mvn compile` | 🟢 safe from `develop` since HBTV-012 |
| `mvn package` | 🟡 use `-pl` to exclude broken modules |
| `mvn test` | 🟠 many tests hit live network — quarantine |
| `mvn verify` | ⛔ not safe yet |

---

## 🛠️ 6. How to maintain trackers

`docs/dev-tracker.md` and `docs/dev-tracker.json` are **mirrors**.
Always update both in the same commit. Fields per item:

- `displayTitle` — human-readable label
- `icon` — emoji prefix
- `id` — `HBTV-XXX` identifier
- `status` — `proposed` | `in-progress` | `done` | `blocked` | `deferred`
- `priority` — `P0` (critical) | `P1` (high) | `P2` (normal) | `P3` (low)
- `progress` — integer 0–100
- `scope`, `acceptanceCriteria`, `validation`, `pr`, `notes`

For risks (`R-0XX`) in `docs/risk-register.md` and decisions
(`ADR-00XX`) in `docs/decision-log.md`, follow the existing
templates. Append-only; supersede rather than rewrite.

---

## 🧯 7. How to handle failures

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

## 🔐 8. Security awareness

- ❌ Never introduce hardcoded API keys, passwords, or tokens.
- ❌ Never extend the existing hardcoded credentials (Gmail tests,
  YouTube key, freebox FTP sample) — they are tracked for removal.
- ❌ Never silently re-enable runtime calls to legacy hosts
  (`dabiboo.free.fr`, `ftpperso.free.fr`).
- ✅ Read [`SECURITY.md`](SECURITY.md) before touching any auth or
  network code.

---

## 🗣️ 9. Communication conventions

- Always reply in **English** in code, commits, comments, docs, and PR
  text — regardless of the user's prompt language.
- Be precise about what you ran vs. what you reasoned about. Cite
  exact command outputs, file paths, and line numbers.
- Disagree with the user when their suggestion would break a hard
  rule above; surface the conflict instead of complying silently.

---

## 📅 10. Update cadence

This file is reviewed:
- On every phase transition in `docs/dev-plan.md`.
- When a new hard rule is added (must come with an accepting ADR).
- When the supported tooling baseline changes (Java version, Maven
  version, OS targets).

Last refresh: see the latest commit touching this file.

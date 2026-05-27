# 🤝 Contributing to Habitv

Thanks for your interest! Habitv is in a **controlled modernization
restart**. To keep changes reviewable and reversible during this phase,
contributions follow a stricter-than-usual workflow.

> 📖 **Required reading before opening a PR**
> - [`AGENTS.md`](AGENTS.md) — full rules for human and AI contributors
> - [`docs/dev-plan.md`](docs/dev-plan.md) — phased modernization plan
> - [`docs/dev-tracker.md`](docs/dev-tracker.md) — active work items
> - [`docs/decision-log.md`](docs/decision-log.md) — accepted architecture decisions
> - [`docs/automatic-category-download.md`](docs/automatic-category-download.md) — category watch / index behavior
> - [`docs/ci.md`](docs/ci.md) — CI required vs diagnostic checks

---

## 🌳 Branching

| Branch | Role | Direct push |
|--------|------|-------------|
| `master` | Stable baseline | 🔒 Protected |
| `develop` | Integration line for modernization | 🔒 Protected |
| `fix/*`, `feat/*`, `chore/*`, `docs/*`, `test/*`, `ci/*`, `refactor/*` | Short-lived branches | ✅ Allowed |
| `legacy` | Historical reference | 🔒 Frozen |

All modernization branches must target **`develop`**.
The restart bootstrap PR targets `master`. Everything else targets `develop`.

Before creating a new branch, follow the duplicate-prevention checks
defined in `AGENTS.md` (`git fetch --all --prune`, matching branch/PR
search, and `git check-ref-format --branch`).

**AI agents** must not use tool-specific or session-generated branch
names (`claude/**`, `anthropic/**`, `ai/**`, `cursor/**`, `codex/**`,
`wip/**`, and similar). Use only `feat/`, `fix/`, `docs/`, `chore/`,
`test/`, `refactor/`, or `ci/` plus a short kebab-case scope — see
`AGENTS.md` §4 for valid/invalid examples and recovery when already on
an invalid branch.

For AI-agent workflow, PR target policy is defined in `AGENTS.md`:
agent-created PRs target `Mika3578/habitv` (not `ikfon10/habitv`).

---

## 📝 Commit style

[Conventional Commits](https://www.conventionalcommits.org/), in English,
imperative, lowercase, no trailing period, ≤ 72 characters subject.

```
<type>(<scope>): <subject>

<body wrapped at ~72 chars explaining motivation / behavior change>
```

Allowed types: `feat`, `fix`, `refactor`, `perf`, `docs`, `test`, `chore`,
`build`, `ci`, `style`, `revert`.

✅ **Good**

```
feat(youtube): externalize data api key
fix(arte): handle 404 on main archive
docs(tracker): record plugin-tester-align outcome
```

❌ **Avoid**

```
update code           ← what, where, why?
Fixed bug.            ← capitalized, past tense, vague
WIP                   ← do not commit WIP
```

---

## 🏷️ Plugin versioning

Each `plugins/*/pom.xml` inherits `<version>` from the root POM by
default. A plugin **MAY** override the version and publish on its own
patch line when at least one user-visible trigger applies. See the
`plugin-versioning-policy` ADR in [`docs/decision-log.md`](docs/decision-log.md)
for the full rationale.

**Bump the plugin's `<version>` when:**

- The downloader or parser behavior changes (URL handling, format
  detection, binary swap such as `youtube-dl` → `yt-dlp`).
- A user-facing endpoint or channel slug changes (e.g. swapping a
  dead provider URL).
- A user-facing configuration change (API key resolution, defaults,
  credential layout).

**Do NOT bump for:**

- Offline test fixtures.
- Build, CI, or IDE cleanup.
- Dependency management or reactor alignment.
- Internal renames invisible to a configured user.
- Documentation-only changes.

**Conventions:**

- Keep the `-SNAPSHOT` suffix while `CHANGELOG.md` `[Unreleased]` has
  not been cut. Dropping `-SNAPSHOT` is gated by `static-repo-publish`.
- Internal reactor dependencies (`api`, `framework`, `plugin-tester`)
  inside a bumped plugin MUST use `${project.parent.version}`, never
  `${project.version}` (cf. `own-version-deps-align`). A bare
  `${project.version}` in a plugin whose version differs from the
  parent resolves to a non-existent artifact at compile time.
- The PR body must name which trigger applies when bumping.

---

## 🎯 One scope per PR

| Rule | Detail |
|------|--------|
| Related issue | Optional: reference a real GitHub issue (e.g. `#123`) when one exists |
| Scope | Use one clear descriptive scope in branch name and PR body (e.g. `provider-youtube-ytdlp`) |
| Scope discipline | One logical change. No opportunistic refactors or formatting passes |
| Linear history | No merge commits inside work branches; squash or rebase only |
| Doc sync | Update `docs/dev-tracker.{md,json}`, `docs/risk-register.md`, `docs/decision-log.md` when behavior or scope changes |
| English only | Branches, commits, code comments, docs, PR text |

Do not use local tracker IDs (e.g. `hbtv-*` / `HBTV-*`) for new branch
names, PR titles, or commit subjects. Use descriptive scopes and
GitHub-native tracking (issues, labels, projects, milestones).

---

## ✅ Validation matrix

The current safe validation level is **`validate`**.
Stronger goals are only safe for explicitly scoped modules.
For docs-only PRs, run `git diff --check`; Maven commands are optional
unless build files changed.

| Goal | Status in current phase | When to use |
|------|------------------------|-------------|
| `mvn -B -ntp -DskipTests validate` | 🟢 safe, default | Always |
| `mvn -B -ntp -DskipTests compile` | 🟢 safe on `develop` since `own-version-deps-align` | Always |
| `mvn -B -ntp -DskipTests package` | 🟡 scoped only | `-pl '!application/trayView,!application/habiTv'` |
| `mvn -B -ntp test` | 🟠 network-dependent tests flap | Scoped per-module; document results |
| `mvn -B -ntp verify` | ⛔ not safe yet | Dedicated test-lifecycle tracker work |

Always quote the **exact command output** in your PR body.

---

## 🚫 Out of scope until explicitly tracked

Do not, without a dedicated tracker item and an accepted ADR:

- Restructure the Maven reactor or aggregator topology.
- Migrate the Java baseline beyond Java 8.
- Migrate JavaFX (`jfxrt.jar` on Java 8 runtime) to shipped OpenJFX runtime
  packaging (compile bridge on JDK 11+ is not sufficient — see
  `docs/java-runtime-policy.md`).
- Regenerate JAXB-bound classes or move to `jakarta.*`.
- Replace `youtube-dl` with `yt-dlp` (see `ytdlp-migration`).
- Change runtime updater URLs or layout (see `static-repo-publish`).
- Migrate FTP/HTTP repositories (see `legacy-url-migration`).
- Remove or rename provider/plugin modules (see `provider-inventory`).
- Add OWASP, SBOM, or static-analysis plugins.

---

## 🔐 Security & credentials

- 🚨 **Never** commit secrets, tokens, local paths, build outputs, IDE
  files, or generated artifacts.
- The repository historically contained hardcoded credentials
  (Gmail POP3/IMAP test, YouTube Data API key, freebox FTP sample).
  These are tracked under `youtube-key-hardcoded` in
  `docs/risk-register.md` and are being remediated;
  **do not add new ones**.
- See [`SECURITY.md`](SECURITY.md) for the disclosure process.

---

## 📦 License

Habitv currently has **no explicit license file**. Until a license is
added by the maintainer, treat the source as proprietary by default
and do not redistribute. Contributors agree that their patches may be
relicensed under whichever license the maintainer chooses when this
gap is closed.

---

## 🆘 Getting help

- 🐛 **Bug?** → Issue template `bug.md`.
- 🛠️ **Modernization scope?** → Issue template `modernization.md`.
- ❓ **Unclear procedure?** → Open a discussion or ask in the PR you are drafting.

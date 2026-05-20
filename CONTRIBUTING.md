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
| `feature/*`, `fix/*`, `chore/*`, `build/*`, `ci/*`, `docs/*`, `test/*` | Short-lived branches | ✅ Allowed |
| `legacy` | Historical reference | 🔒 Frozen |

All modernization branches must target **`develop`**.
The restart bootstrap PR targets `master`. Everything else targets `develop`.

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

## 🎯 One tracker item per PR

| Rule | Detail |
|------|--------|
| Tracker ID | Reference one work-item slug (e.g. `legacy-url-migration`) from `docs/dev-tracker.md` in the PR body |
| Scope discipline | One logical change. No opportunistic refactors or formatting passes |
| Linear history | No merge commits inside feature branches; squash or rebase only |
| Doc sync | Update `docs/dev-tracker.{md,json}`, `docs/risk-register.md`, `docs/decision-log.md` when behavior or scope changes |
| English only | Branches, commits, code comments, docs, PR text |

If your change does not fit any tracker item, open one first via the
`modernization` issue template before coding.

---

## ✅ Validation matrix

The current safe validation level is **`validate`**.
Stronger goals are only safe for explicitly scoped modules.

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
- Migrate JavaFX (JDK-bundled `jfxrt`) to OpenJFX.
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

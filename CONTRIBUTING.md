# Contributing to HabiTV

HabiTV is in a controlled modernization. Keep changes small and reviewable.

Agents: follow [`AGENTS.md`](AGENTS.md). Humans: this file.

## Branches

| Branch | Role |
|--------|------|
| `develop` | Integration line (protected) |
| `master` | Stable baseline (protected) |
| `fix/*`, `feat/*`, `docs/*`, `test/*`, `ci/*`, `chore/*`, `refactor/*` | Work branches |

Format: `<type>/<short-scope>` (English kebab-case). No AI/tool prefixes
(`cursor/`, `claude/`, `ai/`, `codex/`).

Public git text stays short and generic. Module or topic as scope is
fine (lowercase kebab-case). Detail stays in the diff. Canonical
wording: [`AGENTS.md`](AGENTS.md) **Public git text**.

PR titles use Conventional Commits.

Target **`Mika3578/habitv`** → **`develop`**. Linear history on work
branches (rebase, no merge commits).

```
<type>(<scope>): <subject>
```

Required scope, imperative subject, ≤ 72 characters.

Allowed types: `feat`, `fix`, `refactor`, `perf`, `docs`, `test`,
`chore`, `ci`, `build`, `style`, `revert`.

## Validation

Java baseline and build details: [`docs/development.md`](docs/development.md).
Current compiler/CI: Java 8. Target: Java 21, then Java 25.

| Change | Minimum |
|--------|---------|
| Docs only | `git diff --check` |
| Code / POM / workflow | `mvn -B -ntp -DskipTests validate` plus targeted module tests when relevant |

Record the command and result in the PR body (not a full log). Live
provider tests are opt-in (`-Plive-provider-tests`), not default CI.

## Parent / application versioning

The reactor parent version in the root `pom.xml` drives `habiTv`,
`trayView`, `core`, `framework`, and other modules that inherit it.

Conventional Commit type on the PR title selects the SemVer bump
(`feat` → minor, `fix` / `refactor` / `perf` → patch, breaking →
major). CI **Validate Version Bump Consistency** enforces it.

Apply a missing bump without rewriting plugin overrides:

```bash
bash scripts/apply-parent-version-bump.sh --type feat
bash scripts/apply-parent-version-bump.sh --type fix --check
```

`chore`, `docs`, `test`, `ci`, and similar types do not bump the parent.

## Plugin versioning

Plugins inherit the parent version unless a **user-visible** change
requires an override:

- downloader/parser behavior
- user-facing endpoint or channel slug
- user-facing configuration

Do not bump for fixtures, CI, docs, or internal-only refactors.
Inside a bumped plugin, depend on `api` / `framework` /
`plugin-tester` with `${project.parent.version}`.

When the parent version changes, keep the plugin override as-is unless
that plugin itself needs a user-visible bump.

## Out of scope unless explicitly requested

Reactor topology changes, Java baseline bump, JavaFX migration, JAXB
Jakarta move, provider rewrites, runtime updater URL changes.

Never commit secrets, tokens, credentials, or generated binaries.

Code comments: English, concise; explain non-obvious *why* (see
[`AGENTS.md`](AGENTS.md) **Engineering Baseline**).

## License

No `LICENSE` file is present. Treat sources as proprietary until the
maintainer adds one.

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
(`cursor/`, `claude/`, `ai/`).

Branch names, PR titles, PR comments, and commit subjects stay short
and generic. No product or brand names. Detail stays in the diff.
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

Paste exact command output in the PR body. Live provider tests are
opt-in (`-Plive-provider-tests`), not default CI.

## Plugin versioning

Plugins inherit the parent version unless a **user-visible** change
requires an override:

- downloader/parser behavior
- user-facing endpoint or channel slug
- user-facing configuration

Do not bump for fixtures, CI, docs, or internal-only refactors.
Inside a bumped plugin, depend on `api` / `framework` /
`plugin-tester` with `${project.parent.version}`.

## Out of scope unless explicitly requested

Reactor topology changes, Java baseline bump, JavaFX migration, JAXB
Jakarta move, provider rewrites, runtime updater URL changes.

Never commit secrets, tokens, credentials, or generated binaries.

## License

No `LICENSE` file is present. Treat sources as proprietary until the
maintainer adds one.

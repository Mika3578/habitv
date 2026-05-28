# Habitv developer and agent workflow

Canonical AI agent policy: [`AGENTS.md`](../AGENTS.md) (especially
Sections 14–16).

This guide summarizes practical workflow for humans and agents working
on Habitv modernization.

## Principles

- **Fast but safe** — small, focused PRs; one topic per branch.
- **Java 8 default** — unless a dedicated migration PR says otherwise.
- **Manual approval** — agents never commit, push, or open PRs without
  explicit developer approval in the current conversation.
- **Incremental modernization** — prefer focused changes over rewrites.

## Task startup

At task start, agents report the **Agent startup checklist** (Section
16.17) and **Instruction files loaded** (Section 15.1).

Pick a **rule profile** before editing (Sections 16.2 and **20.2**). Use
**speed mode** for docs-only and other low-risk work (Section 20.8); use
**deep mode** for provider, dependency, packaging, and migration work
(Section 20.9).

Classify work before editing (Section 16.2 and **18.1** for Habitv phases):
quick fix, provider/plugin, dependency/security, CI, IDE/tooling, docs-only,
refactor, packaging, migration preparation, or investigation only.

## Branch and PR workflow

1. Create a work branch from latest `origin/develop` (never work on
   `develop` directly).
2. Use conventional branch prefixes: `fix/`, `feat/`, `docs/`, `test/`,
   `ci/`, `chore/`, `refactor/`.
3. Keep PRs small when possible (under 10 files preferred).
4. Rebase on `origin/develop` to update; never merge `develop` into the
   PR branch.
5. Target PRs at `Mika3578/habitv` → `develop`.

## Validation

Validation tiers (`AGENTS.md` Section 14.2):

**Docs-only:** `git diff --check`; Maven may be skipped with a clear note.

**Standard code changes:**

```bash
mvn -B -ntp validate
mvn -B -ntp -pl <module> -am test   # when module-specific
```

**Risky / code-wide changes:**

```bash
mvn -B -ntp validate
mvn -B -ntp test
```

**Packaging / release / full-app** (when in scope and JavaFX/`jdk.home`
environment supports it):

```bash
mvn -B -ntp -DskipTests clean package
```

Provide exact command output — not vague "build ok" claims (Section 15.15).

## Local Java 8 setup

Habitv targets Java 8. For GUI/packaging work, use a **JavaFX-capable
JDK 8** (e.g. Liberica Full JDK 8).

Do **not** commit machine-specific paths in workspace settings. Use
[`.vscode/settings.example.json`](../.vscode/settings.example.json) as a
reference; keep actual `JAVA_HOME` and paths in local untracked settings.

## Cursor / VS Code

Recommended extensions: [`.vscode/extensions.json`](../.vscode/extensions.json).

Repository rules live in `.cursor/rules/` and `AGENTS.md`. User-level
Cursor settings are personal preferences only.

Use `agent_space/` for temporary agent scratch work. If `agent_space/` is
missing from `.gitignore`, propose adding it in a dedicated tooling PR
(per `AGENTS.md` Section 15.21).

## CI overview

| Workflow | File | Role |
|----------|------|------|
| Maven CI | `.github/workflows/ci-maven.yml` | Java 8 validate/test/package |
| Dependency Review | `.github/workflows/dependency-review.yml` | High-severity dependency gate |
| Build | `.github/workflows/build.yml` | Additional build diagnostics |

See [`docs/ci.md`](ci.md) and [`docs/repository-governance.md`](repository-governance.md)
for required checks and branch protection.

## Related policies

- [`docs/provider-policy.md`](provider-policy.md) — providers and external tools
- [`docs/release-policy.md`](release-policy.md) — release readiness
- [`docs/modernization-backlog.md`](modernization-backlog.md) — fast modernization queue
- [`docs/maintenance-dashboard.md`](maintenance-dashboard.md) — planning dashboard
- [`docs/maintainability-policy.md`](maintainability-policy.md) — DoD, tests, config
- [`docs/agent-rule-profiles.md`](agent-rule-profiles.md) — profiles, speed/deep mode
- [`docs/archived-agent-rules.md`](archived-agent-rules.md) — retired rules
- [`docs/adr/README.md`](adr/README.md) — ADR workflow (see also `decision-log.md`)
- [`docs/dependency-policy.md`](dependency-policy.md) — dependency updates
- [`docs/security-policy.md`](security-policy.md) — security fixes
- [`docs/java-runtime-policy.md`](java-runtime-policy.md) — Java 8 and JavaFX
- [`docs/agent-rules-changelog.md`](agent-rules-changelog.md) — rule versions
- [`docs/agent-rules-backlog.md`](agent-rules-backlog.md) — future rule work
- [`CONTRIBUTING.md`](../CONTRIBUTING.md) — commit and PR policy

## Final report

Agents end tasks with the **Final report** (Sections 16.25, **18.19**, and
**19.25** for maintainability fields) and ask **Approve commit?** before
any commit. Apply Section 20 profiles for validation scope.

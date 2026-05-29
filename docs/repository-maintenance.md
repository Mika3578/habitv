# Repository maintenance

Day-to-day maintenance for the Habitv repository: branching, merge hygiene,
documentation sync, and automation expectations.

## Branch model

| Branch | Role |
|--------|------|
| `develop` | Integration line for modernization — **branch from here** |
| `master` | Stable baseline snapshot |
| `fix/*`, `feat/*`, `chore/*`, `docs/*`, `test/*`, `ci/*`, `refactor/*` | Short-lived topic branches |

Modernization PRs target **`develop`** with linear history (no merge commits on
work branches). Squash merge to `develop` with a cleaned title/body (see
below). Follow duplicate-prevention checks before creating any new branch as
defined in `AGENTS.md`.

## Contributor workflow

1. Sync to latest `develop`.
2. Pick or add a descriptive scope item in [`dev-tracker.md`](dev-tracker.md).
3. Keep the PR scoped to **one** logical change / scope.
4. Use [Conventional Commits](https://www.conventionalcommits.org/) in English.
5. Run validation:
   - docs-only PR: `git diff --check`
   - default code PR: `mvn -B -ntp -DskipTests validate`
6. Paste exact command output in the PR body.
7. Update `dev-tracker.md` + `dev-tracker.json` (and risk/decision docs when
   applicable) in the same PR when state changes.

See [`CONTRIBUTING.md`](../CONTRIBUTING.md) and [`AGENTS.md`](../AGENTS.md).

## PR metadata policy

- PR titles and bodies in **English**.
- Reference one descriptive scope slug (e.g. `provider-inventory`).
- Squash merge title: Conventional Commits + PR number.
- Squash body: final outcome and validation — not intermediate commit bullets.
- Remove accidental `Co-authored-by` trailers unless intentional.

See [`pull-request-style-guide.md`](pull-request-style-guide.md).

## Repository automation

| Automation | Behavior |
|------------|----------|
| Dependabot | Weekly Maven + Actions update PRs; no major bumps; no auto-merge |
| Dependency Review | Blocks new high/critical dependency vulnerabilities on PRs |
| CodeQL | Code scanning (separate from Maven CI) |
| Stale triage | Labels inactive issues/PRs; does not auto-close |
| PR labeler | Path-based labels; not a required check |

Live provider tests must not become required checks until replaced or reliably
quarantined — see [`ci.md`](ci.md).

### CodeQL-only compiler warnings (Lombok `Permit`)

The Habitv reactor does not use Lombok. If CodeQL logs mention
`lombok.permit.Permit` or `--enable-final-field-mutation` during the manual
Maven verify step, treat that as an **accepted, CodeQL-tracer warning**—not a
project dependency or compiler-plugin defect. Maven CI on Java 8 does not
emit it. Do not add Maven `-q`, log filtering, or JVM flags in application POMs
to silence it. See the audit notes in [`ci.md`](ci.md#accepted-codeql-build-warnings-lombok--final-field-mutation).

## Documentation sync

When process, scope, or status changes, keep aligned in the **same commit**:

| Change type | Update |
|-------------|--------|
| Work item progress | `dev-tracker.md` + `dev-tracker.json` |
| Risk added/mitigated | `risk-register.md` |
| Architecture decision | `decision-log.md` (ADR) |
| User-facing release notes | `CHANGELOG.md` (when applicable) |

Machine/human tracker parity check (before PR):

```bash
python3 -c "
import json, re
md = open('docs/dev-tracker.md', encoding='utf-8').read()
js = json.load(open('docs/dev-tracker.json', encoding='utf-8'))
md_slugs = set(re.findall(r'\`([a-z][a-z0-9-]{4,})\`', md))
js_slugs = {i['id'] for i in js['items']}
missing = js_slugs - md_slugs
assert not missing, missing
print('OK:', len(js_slugs), 'items')
"
```

## Related documentation

- [`repository-governance.md`](repository-governance.md) — branch protection and rulesets
- [`ci.md`](ci.md) — required vs diagnostic CI jobs
- [`dev-plan.md`](dev-plan.md) — phased roadmap
- [`README.md`](../README.md) — project entry point

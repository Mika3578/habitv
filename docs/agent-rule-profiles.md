# Agent rule profiles

Canonical summary: [`AGENTS.md`](../AGENTS.md) Section 20.

Agents must pick **one profile** at task start (extends Section 16.2).
Do not apply heavy release or migration rules to docs-only work.

## Minimum viable ruleset

`AGENTS.md` holds only mandatory safety, workflow, validation commands,
Java/Maven compatibility, Git/PR approval gates, and links.

Long explanations, examples, and templates belong in:

- [`docs/dev-workflow.md`](dev-workflow.md)
- [`docs/agent-rules-changelog.md`](agent-rules-changelog.md)
- [`docs/agent-rules-backlog.md`](agent-rules-backlog.md)
- path-specific `AGENTS.md` files
- [`.github/instructions/`](../.github/instructions/)

## Profile matrix

| Profile | Risk | Maven | Deep sections | Final report extras |
|---------|:----:|:-----:|---------------|---------------------|
| docs-only | low | skip* | — | minimal |
| quick fix | low–medium | validate | — | standard |
| provider/plugin | medium–high | validate + module tests | §18, §19 | Habitv + maintainability |
| dependency/security | medium–high | full | §16.3–16.4, §19.5–19.6 | dependency + security |
| CI/workflow | medium | validate | §15, workflows | CI impact |
| IDE/tooling | low–medium | skip unless POM touched | §16.16 | tooling notes |
| packaging | high | package when in scope | §18.12, §19.3 | release + rollback |
| Java migration | critical | dedicated plan | §2, §18.7, §19.4 | migration report |
| release preparation | high–critical | full + manual | §18.13, §19 | release readiness |
| investigation only | low | skip | — | findings only |

\* Skip Maven unless docs change build/runtime instructions or AI rules
that affect validation policy (run drift audit instead).

Profile names: docs-only; quick fix; provider/plugin; dependency/security;
CI/workflow; IDE/tooling; packaging; Java migration; release preparation;
investigation only.

## Docs-only lightweight path

For docs-only work:

- no Maven unless docs affect build/runtime instructions;
- verify Markdown manually;
- verify no code, POM, workflow, dependency, or runtime file changed;
- run rule drift audit if AI rules changed (Section 17.6);
- status may be **Ready for commit approval** after manual review.

Do not run full Maven for pure text-only docs unless requested.

## Heavy gate scope

Heavy validation required for:

- Java source changes;
- POM/dependency changes;
- workflow changes;
- provider/plugin behavior changes;
- packaging changes;
- external tool changes;
- Java/JavaFX compatibility changes;
- config/XML/JAXB changes;
- security-sensitive changes.

Do not apply heavy validation to unrelated documentation or planning.

## Speed mode vs deep mode

**Speed mode** (Section 20.8): docs-only, narrow test fix, typo, isolated
config documentation; no remote state, dependency, or Java behavior change.

Still required: `git status`, changed-files summary, final report,
**Approve commit?**; no commit/push without approval.

Skipped in speed mode: full Maven build, release checklist, dependency/
security checklist, provider live validation, packaging checks.

**Deep mode** (Section 20.9): provider behavior, dependency/security,
Maven/POM, workflow/CI, packaging, Java migration, release prep, external
downloaders, XML/config compatibility.

Required: full validation plan, risk assessment, rollback plan (medium+),
manual real-app testing request, security/dependency impact, Java 8 report.

## Allowed files (scope creep guard)

| Profile | Typical allowed paths |
|---------|----------------------|
| docs-only | `docs/**`, `*.md`, tracker JSON mirrors |
| quick fix | single module or narrow path per task |
| provider/plugin | `plugins/<name>/**`, related tests |
| dependency/security | `pom.xml`, `**/pom.xml`, lockfiles if any |
| CI/workflow | `.github/**` |
| IDE/tooling | `.vscode/**`, `.cursor/**`, `docs/**` |
| packaging | `packaging/**`, `scripts/stage-package.*` |
| investigation only | read-only; no commits unless approved |

Cross-profile changes require explicit developer approval.

## Conflict precedence

When rules conflict (Section 20.5):

1. explicit developer instruction (current conversation)
2. safety/security rule
3. manual approval gate
4. branch protection / repository policy
5. root `AGENTS.md`
6. nested `AGENTS.md`
7. `.cursor/rules`
8. `.github/copilot-instructions.md`
9. `.github/instructions/*.instructions.md`
10. docs guidance
11. older comments or historical notes

If still unresolved: **Needs developer decision**.

## Rule budget

Before adding a mandatory rule, check whether it replaces, merges, or
simplifies an existing one.

**Avoid:** duplicate wording; multiple rules saying the same thing; long
examples for obvious rules; unverifiable rules; one-incident rules unlikely
to recur.

**Prefer:** one short mandatory rule; one linked detailed doc; one checklist
item; one backlog item if not urgent.

## Stop conditions

Stop adding mandatory rules when:

- the rule duplicates an existing gate;
- CI or branch protection already enforces it;
- it documents a one-time issue only;
- it would slow common tasks more than it prevents real mistakes;
- it belongs in backlog, not active instructions.

When unsure: add to [`docs/agent-rules-backlog.md`](agent-rules-backlog.md).

## Rule cleanup cadence

Every ~10 merged PRs, propose branch `docs/cleanup-agent-rules` with commit
`docs(ai-rules): clean up obsolete agent guidance` (Section 20.11): remove
duplicates; downgrade over-strict rules; archive obsolete rules; align
instruction files; keep `AGENTS.md` short.

Archive retired rules in [`archived-agent-rules.md`](archived-agent-rules.md).

## Enforcement over expansion

Prefer enforcing rules through branch protection, required CI checks,
CODEOWNERS, Dependabot, Dependency Review, CodeQL, Maven validation, small
PR workflow, provider tests, and workspace docs — not endless mandatory text.

Track gaps in [`docs/agent-rules-backlog.md`](agent-rules-backlog.md).

# 🧭 Habitv Decision Log

[ADR](https://adr.github.io)-style architecture and process decisions
for the modernization restart. **Append-only**: new decisions supersede
older ones rather than rewriting them in place.

---

## 📊 ADR dashboard

| ID | Title | Status |
|---|---|:--:|
| `ADR-0001` | Restart modernization from `master` | ✅ Accepted |
| `ADR-0002` | Keep Java 8 as baseline until the build is stable | ✅ Accepted |
| `ADR-0003` | Small PRs with linear history | ✅ Accepted |
| `ADR-0004` | `habitv-repo` as the future static artifact repository | 🟡 Proposed |
| `ADR-0005` | Provider cleanup is separate from build / governance bootstrap | ✅ Accepted |
| `ADR-0006` | Runnable console baseline on `develop` before broader modernization | ✅ Accepted |

---

## 🏷️ Status legend

| Symbol | Meaning |
|:---:|---|
| ✅ **Accepted** | Decision in force; supersedes any prior contradicting ADR |
| 🟡 **Proposed** | Awaiting validation by deliverable; reversible |
| ⛔ **Rejected** | Considered and explicitly not adopted |
| 🔁 **Superseded** | Replaced by a later ADR — kept for history |

---

## ✅ ADR-0001 — Restart modernization from `master`

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |
| **Tracker** | HBTV-000 |

**Context** — Earlier modernization attempts diverged from the stable
`master` baseline and accumulated unrelated changes, making it hard
to reason about scope and rollback. A previous bootstrap branch
(`chore/bootstrap-restart-from-master`) had different filenames and
conventions from the agreed spec.

**Decision** — Restart modernization from the current `master` tip.
Recreate the bootstrap branch from `origin/master` and deliver only
governance / documentation / CI validate baseline in the first PR.
All later modernization PRs branch from the resulting governance
baseline (or `develop` once created).

**Consequences**
- 🔁 Short-term throwaway of previous bootstrap branch content.
- ✅ Clear, reviewable starting point.
- ✅ Future ADRs can build on a known baseline.

---

## ✅ ADR-0002 — Keep Java 8 as baseline until the build is stable

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |
| **Trackers** | HBTV-001, HBTV-002 |
| **Risks** | R-003, R-004 |

**Context** — The codebase declares Java 1.7 in the root
`maven-compiler-plugin` and Java 7 in some packaging POMs. JavaFX 2.x
and `javax.xml.bind` 2.0 assume the legacy JDK 8 stack. Migrating
Java baselines while the reactor itself is broken would conflate
multiple risks.

**Decision** — Keep Java 8 (Temurin 8) as the baseline for all CI and
agent guidance until HBTV-001 (reactor) and HBTV-002 (compile + test
baseline) are stable. Any later baseline change requires a dedicated
migration PR and a superseding ADR.

**Consequences**
- ✅ AI agents and contributors must reject Java 9+ language and API
  suggestions.
- ✅ CI matrix uses Temurin 8 only in this phase.
- ⚠️ JavaFX 2.x and `javax.xml.bind` 2.0 remain locked in until the
  superseding ADR.

---

## ✅ ADR-0003 — Small PRs with linear history

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |

**Context** — Recent history mixes unrelated changes in single
commits, making review and rollback expensive.

**Decision**
- One tracker item per PR.
- Conventional Commits with imperative, lowercase subjects.
- Linear history enforced via branch protection (no merge commits,
  squash merges preferred).
- Rebase merges allowed only when the owner is comfortable.

**Consequences**
- ✅ Slightly more overhead per change, materially better
  reviewability and bisectability.
- ✅ CI gates only on the `validate` workflow until HBTV-002 lands.

---

## 🟡 ADR-0004 — `habitv-repo` as the future public static artifact repository

| | |
|---|---|
| **Status** | 🟡 Proposed |
| **Date** | 2026-05-16 |
| **Tracker** | HBTV-005 |
| **Risks** | R-001, R-002, R-007, R-009 |

**Context** — Today the project relies on
`http://dabiboo.free.fr/repository` for both Maven artifact
resolution and runtime updates. The host is third-party, plain HTTP,
and unmaintained.

**Decision** — Stand up a `habitv-repo` static repository (target:
GitHub Pages or equivalent HTTPS static host) to publish Maven
artifacts, plugin drops, and update metadata. Layout is defined in
HBTV-005 to remain compatible with `FindArtifactUtils` and
`UpdateManager` semantics.

**Consequences**
- ✅ HTTPS, version-controlled publication.
- 🟡 Confirmation pending: a future ADR will accept or supersede this
  once layout and publication workflow are validated end-to-end.

---

## ✅ ADR-0005 — Provider cleanup is separate from build / governance bootstrap

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-16 |
| **Tracker** | HBTV-006 |

**Context** — Several provider plugins (Pluzz, legacy Canal+, beIN,
others) are likely obsolete. Removing them in the bootstrap PR would
blend documentation work with risky behavior changes and break the
"small, scoped" rule.

**Decision** — Inventory provider/plugin status in HBTV-006 without
removing modules. Each obsolete/renamed plugin gets its own dedicated
PR with a deprecation note. The bootstrap PR does not touch
`plugins/*` or runtime code.

**Consequences**
- ✅ Plugins remain unchanged in this PR. Reviewers can focus on
  governance.
- ✅ Cleanup happens later in safe, named units.

---

## ✅ ADR-0006 — Runnable console baseline on `develop` before broader modernization

| | |
|---|---|
| **Status** | ✅ Accepted |
| **Date** | 2026-05-17 |
| **Tracker** | HBTV-011 |

**Context** — `develop` is the active modernization line. PR #27
introduced a useful runnable console / yt-dlp path but also included
`application/core` source edits that fail scoped compile/package.
Concurrent PR #25 and PR #26 were targeted at `master`, which would
mix branch lines if merged directly.

**Decision** — Create a dedicated stabilization branch from `develop`
and supersede PR #27 with scoped linear commits that keep only the
buildable subset (consoleView fat-jar path, yt-dlp runtime/test
wiring, and required build POM updates). Exclude failing unrelated
source edits and keep HBTV-004 / HBTV-005, JavaFX modernization, and
provider cleanup out of scope.

**Consequences**
- ✅ `develop` gained a factual runnable console baseline with
  offline yt-dlp command coverage.
- ⚠️ Known legacy repository blockers remain explicit and are tracked
  separately.
- 🔁 PR #25 and PR #26 must be retargeted/rebased to `develop` (or
  recreated as scoped follow-ups) after this baseline lands.

---

## 📝 How to add a new ADR

1. Pick the next `ADR-00XX` id.
2. Use this template:
   ```markdown
   ## 🟡 ADR-00XX — <Title>

   | | |
   |---|---|
   | **Status** | 🟡 Proposed |
   | **Date** | YYYY-MM-DD |
   | **Tracker** | HBTV-XXX |
   | **Risks** | R-0XX |

   **Context** — …
   **Decision** — …
   **Consequences** — …
   ```
3. Update the dashboard table at the top of this file.
4. Reference the ADR id in any PR that depends on it.

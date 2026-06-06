---
applyTo: "**/*.java,**/pom.xml,fwk/**,application/**,plugins/**"
---

# Habitv Maven and Java instructions (Copilot)

Canonical workflow policy: `AGENTS.md`. Do not contradict it.

## Java 21

- Target Java 21 only (`maven.compiler.release=21`). No Java 22+ language features or APIs.
- OpenJFX 21.0.7 is the required JavaFX dependency; do not reintroduce `system`-scope `javafx:jfxrt` or `${jdk.home}` references.
- Preserve the existing Maven multi-module layout.
- Do not propose reactor restructures without a referenced tracker item.

## Pre-commit validation

Before commit approval, use the tier from `AGENTS.md` Section 14.2. Do
**not** require root `clean package` for every code change.

**Standard code:**

```
mvn -B -ntp validate
mvn -B -ntp -pl <module> -am test
```

**Risky / code-wide:**

```
mvn -B -ntp validate
mvn -B -ntp test
```

**Packaging / full-app** (when in scope and environment supports JavaFX):

```
mvn -B -ntp -DskipTests clean package
```

If the required tier was skipped for a code-impacting change without
developer approval, mark:

> Not ready to commit.

CI-safe baseline (`mvn -B -ntp -DskipTests validate`) is not sufficient
alone unless the developer explicitly relaxes `AGENTS.md` Section 14.2.

## Dependencies

Do not add, remove, or upgrade dependencies unless the task explicitly
requires it. Document Java 21 compatibility impact when a change is
required.

## Avoid suggesting

- `javax.xml.bind` → `jakarta.xml.bind`
- `youtube-dl` → `yt-dlp` (tracked separately)
- JavaFX upgrades (tracked separately)
- Network-dependent tests in the default lifecycle
- OWASP, SBOM, or static-analysis plugins in this phase
- Reformatting or renaming outside the change scope

## Real application testing

Before commit approval, prompt the developer to test affected runtime
behavior. List exact manual tests when UI, plugins, downloads, packaging,
or external tools are affected.

## Approval gates

Never commit or push automatically. Never chain approval-gated commands
(Section 15.8). See `AGENTS.md` Section 14 and `.cursor/rules/git-safety.mdc`.

## Generated files

Do not edit generated JAXB/XML files manually unless explicitly required
(Section 15.12). Document generation command and validation when they
change.

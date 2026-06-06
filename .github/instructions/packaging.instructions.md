---
applyTo: "**/packaging/**,application/habiTv*/**,scripts/stage-package.*"
---

# Habitv packaging instructions (Copilot)

Extends `AGENTS.md` and `packaging/AGENTS.md` (when present). Do not
contradict them.

## Scope

Packaging, installers, and platform bundling work.

## Rules

- No installer or binary scope creep unless explicitly requested.
- No platform packaging (Windows/Linux installers) unless the task
  requires it.
- Do not add `.exe`, `.zip`, `.dll`, or other binaries by default.
- JavaFX packaging uses OpenJFX 21.0.7 on Java 21; do not reference `jfxrt.jar` or `${jdk.home}`.
- Use `jpackage` (bundled with JDK 14+) as the distribution direction; `javafx-maven-plugin 2.0` is retired.
- Document Java 21 and OpenJFX assumptions in validation evidence.

## Validation

When packaging scripts or POMs change:

- document exact commands run and pass/fail results;
- list affected modules;
- request manual testing of the packaged artifact when runtime behavior
  changes.

If full build cannot run, mark **Not ready to commit**.

## Safety

No secrets, no downloaded tools committed, no `target/` outputs staged.
Ask developer before staging any binary or large file.

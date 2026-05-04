# JAXB generation in `application/core`

## Scope
This document describes how JAXB classes for the `application/core` module are generated and consumed.

## Source schemas
The JAXB model is generated from XSD files under:

- `application/core/xsd/config.xsd`
- `application/core/xsd/configuration.xsd`
- `application/core/xsd/grab-config.xsd`

## Maven generation contract
Generation is configured in `application/core/pom.xml` with `com.sun.tools.xjc.maven2:maven-jaxb-plugin` pinned to `1.1.1`.

Three executions are defined:

- `config` -> `com.dabi.habitv.config.entities`
- `configuration` -> `com.dabi.habitv.configuration.entities`
- `grabconfig` -> `com.dabi.habitv.grabconfig.entities`

Generated sources are written to:

- `${project.build.directory}/generated-sources/jaxb`

This keeps generated artifacts out of source control and avoids stale, root-level generated files causing compile drift.

## Accessor naming expectations
Current JAXB generation in this project exposes nullable Boolean accessors as `getXxx()`, not `isXxx()`.

Production code in `application/core/src` must follow the generated getter API for nullable booleans.

## Committed vs regenerated sources
Generated JAXB classes are **not committed**.

- They are generated during Maven builds.
- Build and IDE setups should rely on Maven lifecycle generation instead of editing generated Java files.

## Validation commands
Use these commands from repository root:

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp -DskipTests compile
mvn -B -ntp install
```

## Java 8 / Java 17 notes
- The current build lane remains Java 8-compatible and keeps `javax.xml.bind` usage intact.
- This JAXB stabilization is intended to unblock `application/core` so Java 17 compatibility work can rebase cleanly afterward.
- No `jakarta.xml.bind` migration is included in this scope.

## Runtime provider notes
- `javax.xml.bind:jaxb-api` provides JAXB interfaces but not the implementation provider class.
- Runtime/test paths that call `JAXBContext.newInstance(...)` require a provider containing `com.sun.xml.bind.v2.ContextFactory`.
- `application/core` and `fwk/framework` now declare:
  - `javax.xml.bind:jaxb-api:2.3.1` (javax API line, Java 8-compatible)
  - `com.sun.xml.bind:jaxb-impl:2.3.3` (runtime scope)
  - `javax.activation:activation:1.1.1` (runtime scope)
- This is the minimal Java 8-compatible `javax` JAXB RI wiring needed for deterministic local and CI test/runtime context creation.

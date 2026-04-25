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
Boolean XSD elements are generated with `isXxx()` accessors by JAXB in this module (for example `isUpdateOnStartup`, `isDownload`, `isDeleted`).

Production code in `application/core/src` must use the generated accessor contract and should not assume `getXxx()` for nullable booleans.

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

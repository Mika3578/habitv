# Runtime update repository

## Scope split: build-time vs runtime

Habitv uses two independent artifact sources:

- Build-time Maven repository resolution (used by Maven during `mvn` commands).
- Runtime updater repository resolution (used by Habitv startup update checks).

This document only describes the runtime updater repository.

## Runtime updater base URL

Runtime update checks now use:

`https://raw.githubusercontent.com/Mika3578/habitv-repo/main`

The runtime updater reads:

- `plugins.txt` from `<baseUrl>/plugins.txt`
- plugin/tool artifacts under a Maven-like static path rooted at `<baseUrl>/com/dabi/habitv/...`

## Expected static Maven layout

The updater builds artifact URLs from:

- groupId path: dots replaced by slashes (`com.dabi.habitv` -> `com/dabi/habitv`)
- artifactId path segment
- version directory
- final artifact file

Expected repository layout example:

`com/dabi/habitv/<artifactId>/<version>/<artifactId>-<version>.<ext>`

Concrete URL shape:

`https://raw.githubusercontent.com/Mika3578/habitv-repo/main/com/dabi/habitv/<artifactId>/<version>/<file>`

## Updater URL resolution behavior

Current updater logic does not directly request Maven metadata files. It parses HTML anchor lists from artifact/version directory URLs to discover available versions/files.

That means the runtime endpoint must allow readable directory responses for paths like:

- `<baseUrl>/com/dabi/habitv/<artifactId>/`
- `<baseUrl>/com/dabi/habitv/<artifactId>/<version>/`

When pointed at `raw.githubusercontent.com`, directory paths can return HTTP 400 because raw hosting is file-oriented, not a directory index service. In that case, URL migration is correct but startup updates still require repository publication/serving follow-up so the updater can enumerate versions/files.

## Metadata expectations

`plugins.txt` is required at repository root:

- path: `<baseUrl>/plugins.txt`
- format: one artifactId per line
- line separator expectation in current updater code: CRLF (`\r\n`)

Each artifactId in `plugins.txt` must have corresponding Maven-like directories/files under `com/dabi/habitv/...`.

## Local startup update test

1. Build with Java 8:
   - `mvn -B -ntp clean -DskipTests compile`
2. Start Habitv locally (any launcher path that initializes plugin update flow).
3. Check logs for:
   - `Checking plugin updates...`
   - URL requests to `raw.githubusercontent.com/Mika3578/habitv-repo/main`
4. Verify no runtime request still points to `dabiboo.free.fr`.

If update retrieval fails because artifacts or `plugins.txt` are not yet published to `habitv-repo`, treat that as expected follow-up publication work, not as a runtime URL regression.

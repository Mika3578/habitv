# Static repository publication (`habitv-repo`)

Habitv publishes Maven artifacts and external tool binaries to a local staging
directory that is committed in the separate [`habitv-repo`](https://github.com/Mika3578/habitv-repo)
repository and served publicly via GitHub Pages. This replaces the legacy
`http://dabiboo.free.fr/repository` host (removed from active POMs on
`develop`).

| Role | Location |
|------|----------|
| Application sources | `habitv/` (this repository) |
| Local Maven staging (build only) | `${habitv.static.repo.path}` (see below) |
| Public runtime download URL | `https://mika3578.github.io/habitv-repo/repository/` |

**Strict separation:** Maven uses `file:///${habitv.static.repo.path}` only at
build/deploy time. Habitv at runtime never reads that path; it downloads from the
HTTPS URL in `FrameworkConf.UPDATE_URL`.

## Maven publication workflow (required)

The `-Pstatic-repo-deploy` profile is the single publication command. It deploys
Maven artifacts and runs the final `build/static-repo-publisher` reactor module,
which regenerates repository extras and validates the result. No second manual
script is required after Maven succeeds.

```powershell
$env:HABITV_REPO_DIR = "D:\path\to\habitv-repo"

mvn -B -ntp -Pstatic-repo-deploy deploy `
  "-Dhabitv.static.repo.path=$env:HABITV_REPO_DIR/repository" `
  "-pl=!application/habiTv"
```

`application/habiTv` is excluded with `-pl=!application/habiTv` because of the
existing JDK/`utils4j` compile blocker, which is unrelated to static repository
deployment.

What this single Maven command publishes:

1. **Maven artifacts** — plugin and framework JARs/POMs under
   `${habitv.static.repo.path}/com/dabi/habitv/`.
2. **External tools** — `tools/<tool>/<version>/<tool>.zip` (Windows/PowerShell
   via `publish-repository-extras.ps1`).
3. **`plugins.txt`** — plugin id list for runtime discovery.
4. **`habitv-update-manifest.properties`** — manifest entries for runtime discovery
   (runtime prefers this over `maven-metadata.xml`):
   - **plugin** entries point to the latest selected plugin JAR (timestamped Maven
     SNAPSHOT builds when present);
   - **tool** entries point to published tool ZIP files under `tools/`;
   - the manifest can contain different packaging types (`jar`, `zip`, and others
     as published).
5. **`index.html`** — directory listings for static-host fallback discovery.
6. **Validation** — layout and manifest freshness checks; Maven fails if the
   manifest is stale or inconsistent.

Publication-time requirements on Windows:

- PowerShell (for tool zip publication and full repository extras)
- Python 3 (for layout and manifest freshness validation invoked by Maven)

The runtime updater resolves `habitv-update-manifest.properties` **before**
falling back to `maven-metadata.xml`. If the manifest is not regenerated during
deploy, startup downloads stale timestamped SNAPSHOT artifacts even when newer
JARs were deployed.

### Convenience wrapper (optional)

`scripts/static-repo/publish-static-repository.ps1` is a thin alias around the
Maven command above. It is not the authoritative workflow.

```powershell
$env:HABITV_REPO_DIR = "D:\path\to\habitv-repo"
.\scripts\static-repo\publish-static-repository.ps1 -SkipTests
```

### Legacy `static-repo-publish` profile (deprecated)

The older `-Pstatic-repo-publish` profile remains only as a legacy compatibility
fallback for older runbooks. **New publications must use `-Pstatic-repo-deploy`.**

Do not treat the legacy profile as equivalent to `-Pstatic-repo-deploy`. It is a
best-effort fallback only and is **not** the validated workflow for deploy
ordering, manifest freshness validation, or `${habitv.static.repo.path}` routing.
Use it only when an exceptional compatibility case requires it:

```powershell
mvn -B -ntp -DskipTests clean deploy -Pstatic-repo-publish `
  '-DaltDeploymentRepository=habitv-local::default::file:///${habitv.static.repo.path}' `
  "-pl=!application/habiTv"
```

## Static repository contract

Runtime consumption must work from a public static host with no credentials and no
host-provided autoindex. The generated tree must include:

- `repository/plugins.txt`
- `repository/com/dabi/habitv/...` (Maven path layout)
- `index.html` files with anchor links in directories used by
  `FindArtifactUtils` fallback discovery:
  - `repository/`, `repository/com/`, `repository/com/dabi/`,
    `repository/com/dabi/habitv/`
  - each `repository/com/dabi/habitv/<artifactId>/`
  - each `repository/com/dabi/habitv/<artifactId>/<version>/`

No GitHub Packages endpoint is used, and runtime update checks must never require a
token.

Inspect the resolved staging path:

```powershell
mvn help:evaluate "-Dexpression=habitv.static.repo.path" -q "-DforceStdout"
```

Override the staging path:

```powershell
$env:HABITV_REPO_DIR = "D:\path\to\habitv-repo"
mvn -B -ntp -Pstatic-repo-deploy deploy `
  "-Dhabitv.static.repo.path=$env:HABITV_REPO_DIR/repository" `
  "-pl=!application/habiTv"
```

Nothing is written under the `habitv` source tree; output goes only to
`${habitv.static.repo.path}` (default `${user.home}/dev/habitv-repo/repository`).
The `repository/` directory is listed in `.gitignore` as a safety net.

## Default local staging path

Root `pom.xml` defines:

```xml
<habitv.static.repo.path>${user.home}/dev/habitv-repo/repository</habitv.static.repo.path>
```

| OS | Example |
|----|---------|
| Windows | `C:/Users/<user>/dev/habitv-repo/repository` |
| Linux | `/home/<user>/dev/habitv-repo/repository` |
| macOS | `/Users/<user>/dev/habitv-repo/repository` |

## Workspace layout

```text
${user.home}/dev/
  habitv/                      # application (clone)
  habitv-repo/                 # static artifacts (clone)
    repository/
      com/dabi/habitv/...      # Maven layout (from deploy)
      tools/...                # external tools (from publisher)
      plugins.txt
      habitv-update-manifest.properties
      index.html
```

## Internal helper scripts

Maven invokes these automatically from `build/static-repo-publisher` during
`-Pstatic-repo-deploy deploy`. Run them manually only when debugging:

```powershell
.\scripts\static-repo\publish-repository-extras.ps1 -RepositoryPath "$env:HABITV_REPO_DIR/repository"
```

```bash
./scripts/static-repo/publish-repository-extras.sh
```

Tool sources: `scripts/static-repo/tool-sources.properties` (includes `yt-dlp`;
publish `tools/yt-dlp/latest/yt-dlp.zip` to `habitv-repo` in a follow-up PR).

Skipped by default: `rtmpdump` (no reliable upstream Windows binary), `adobeHDS`
(runtime expects `AdobeHDS.exe`; plugin ships `AdobeHDS.php` only).

## Publish to GitHub Pages

After the publication workflow completes, commit the sibling `habitv-repo`
checkout. Commit **only** `repository/`; do not commit `.trunk/` or local caches.

```powershell
cd $env:HABITV_REPO_DIR
git status --short --branch
git diff --stat
git add repository
git commit -m "repo: update SNAPSHOT versions and refresh metadata"
git push
```

Wait for GitHub Pages propagation (typically 30–60 seconds), then verify:

- https://mika3578.github.io/habitv-repo/repository/plugins.txt
- https://mika3578.github.io/habitv-repo/repository/habitv-update-manifest.properties

Run the isolated startup auto-update test (see project runbooks) and confirm the
manifest points at the latest timestamped SNAPSHOT build ids.

GitHub Pages serves `repository/` at
`https://mika3578.github.io/habitv-repo/repository/`.

## Runtime update discovery

By default, startup checks for plugin/tool updates use the configured
repository base. You can override the base with `-Dhabitv.update.url=...` or
disable startup updates with `-Dhabitv.update.enabled=false`.

1. **Plugins** — `plugins.txt` first, fallback to plugin entries in
   `habitv-update-manifest.properties`.
2. **Artifact resolution (runtime priority)**:
   - manifest entry with explicit final JAR path (preferred);
   - `maven-metadata.xml` snapshotVersion (fallback when no manifest entry);
   - directory listing fallback for non-SNAPSHOT artifacts.
3. **External tools** — manifest `tool` entries, then `tools/<name>/<version>/<file>`.

If GitHub Pages is unreachable, Habitv keeps local plugins and tools.

### Snapshot artifacts

Maven deploy publishes timestamped SNAPSHOT JARs (for example
`arte-4.1.0-20260518.163022-1.jar`). Because the published static repository
currently ships only `-SNAPSHOT` builds for every plugin, `autoriseSnapshot`
defaults to `true` in `configuration.xml`. Do not rely on non-timestamped
`artifactId-<version>-SNAPSHOT.jar` names on GitHub Pages.

To pin to release-only behaviour (skip SNAPSHOT entries from the manifest),
set `<autoriseSnapshot>false</autoriseSnapshot>` in `configuration.xml`, or
pass `-Dhabitv.update.autoriseSnapshot=false`:

```bash
java -Dhabitv.update.autoriseSnapshot=false -jar habitv.jar
```

Supported values for `habitv.update.autoriseSnapshot` are `true` and `false`
only. Invalid values are ignored and leave the XML setting in effect. The
updater logs when the XML value is overridden.

## Manual validation commands

Maven runs these during deploy. Use the commands below only for ad hoc checks:

```bash
python scripts/static-repo/validate_repository_layout.py "<path-to-repository>"
python scripts/static-repo/validate_manifest_freshness.py "<path-to-repository>"
```

Windows example:

```powershell
python .\scripts\static-repo\validate_repository_layout.py `
  "$env:HABITV_REPO_DIR/repository"
python .\scripts\static-repo\validate_manifest_freshness.py `
  "$env:HABITV_REPO_DIR/repository"
```

## Validation URLs

- https://mika3578.github.io/habitv-repo/repository/
- https://mika3578.github.io/habitv-repo/repository/plugins.txt
- https://mika3578.github.io/habitv-repo/repository/com/dabi/habitv/
- https://mika3578.github.io/habitv-repo/repository/tools/

## Clone setup

```bash
mkdir -p "$HOME/dev" && cd "$HOME/dev"
git clone https://github.com/Mika3578/habitv.git
git clone https://github.com/Mika3578/habitv-repo.git
```

```powershell
mkdir "$env:USERPROFILE\dev"; cd "$env:USERPROFILE\dev"
git clone https://github.com/Mika3578/habitv.git
git clone https://github.com/Mika3578/habitv-repo.git
```

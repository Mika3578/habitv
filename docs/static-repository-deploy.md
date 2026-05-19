# Static repository publication (`habitv-repo`)

Habitv publishes Maven artifacts and external tool binaries to a local staging
directory that is committed in the `habitv-repo` Git repository and served
publicly via GitHub Pages.

| Role | Location |
|------|----------|
| Application sources | `habitv/` (this repository) |
| Local Maven staging (build only) | `${habitv.static.repo.path}` (see below) |
| Public runtime download URL | `https://mika3578.github.io/habitv-repo/repository/` |

**Strict separation:** Maven uses `file:///${habitv.static.repo.path}` only at
build/deploy time. Habitv at runtime never reads that path; it downloads from the
HTTPS URL in `FrameworkConf.UPDATE_URL`.

## One-command workflow (preferred)

The `static-repo-publish` profile adds a final reactor module,
`build/static-repo-publisher`, and activates the local file
`distributionManagement` used for the static repository deploy.

Windows PowerShell:

```powershell
mvn -B -ntp -DskipTests clean deploy -Pstatic-repo-publish `
  '-DaltDeploymentRepository=habitv-local::default::file:///${habitv.static.repo.path}' `
  "-pl=!application/habiTv"
```

Linux/macOS (PowerShell when available; otherwise metadata and `index.html` only):

```bash
mvn -B -ntp -DskipTests clean deploy -Pstatic-repo-publish \
  '-DaltDeploymentRepository=habitv-local::default::file:///${habitv.static.repo.path}' \
  '-pl=!application/habiTv'
```

What this single command does:

1. **Maven deploy** — publishes plugin and framework JARs under
   `${habitv.static.repo.path}/com/dabi/habitv/`.
2. **`static-repo-publisher`** (final module) — generates:
   - `plugins.txt`
   - `habitv-update-manifest.properties`
   - `index.html` directory listings
   - `tools/<tool>/<version>/<tool>.zip` (Windows/PowerShell only)

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

`application/habiTv` remains excluded (`-pl=!application/habiTv`) because of the
existing JDK/`utils4j` compile blocker, which is unrelated to static repository
deployment.

Inspect the resolved staging path:

```powershell
mvn help:evaluate "-Dexpression=habitv.static.repo.path" -q "-DforceStdout"
```

Override the staging path:

```powershell
mvn -B -ntp -DskipTests clean deploy -Pstatic-repo-publish `
  "-Dhabitv.static.repo.path=$env:USERPROFILE/dev/habitv-repo/repository" `
  '-DaltDeploymentRepository=habitv-local::default::file:///${habitv.static.repo.path}' `
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

## Manual fallback scripts

If you deploy without the profile, run the publisher script after `mvn deploy`:

```powershell
.\scripts\static-repo\publish-repository-extras.ps1 -RepositoryPath (mvn help:evaluate "-Dexpression=habitv.static.repo.path" -q "-DforceStdout")
```

```bash
./scripts/static-repo/publish-repository-extras.sh
```

Tool sources: `scripts/static-repo/tool-sources.properties` (includes `yt-dlp`;
publish `tools/yt-dlp/latest/yt-dlp.zip` to `habitv-repo` in a follow-up PR).

Skipped by default: `rtmpdump` (no reliable upstream Windows binary), `adobeHDS`
(runtime expects `AdobeHDS.exe`; plugin ships `AdobeHDS.php` only).

## Publish to GitHub Pages

After the Maven command completes, commit the sibling `habitv-repo` checkout:

```powershell
cd $env:USERPROFILE\dev\habitv-repo
git status --short
git add repository
git commit -m "repo: publish habitv artifacts and tools"
git push
```

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

## Layout validation command

After publishing metadata/index files, validate the generated repository layout:

```bash
python scripts/static-repo/validate_repository_layout.py "<path-to-repository>"
```

Windows example:

```powershell
python .\scripts\static-repo\validate_repository_layout.py `
  "$env:USERPROFILE/dev/habitv-repo/repository"
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

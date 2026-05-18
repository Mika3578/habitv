# Static repository publication (`habitv-repo`)

Habitv publishes Maven artifacts and external tool binaries to a local staging
directory that is committed in the `habitv-repo` Git repository and served
publicly via GitHub Pages.

| Role | Location |
|------|----------|
| Application sources | `habitv/` (this repository) |
| Local Maven staging (build only) | `${habitv.static.repo.path}` (see below) |
| Public runtime download URL | `https://mika3578.github.io/habitv-repo/repository/` |

**Strict separation:** Maven uses `file://${habitv.static.repo.path}` only at
build/deploy time. Habitv at runtime never reads that path; it downloads from the
HTTPS URL in `FrameworkConf.UPDATE_URL`.

## One-command workflow (preferred)

The `static-repo-publish` profile adds a final reactor module,
`build/static-repo-publisher`, that runs **after** all other modules have deployed.

Windows PowerShell:

```powershell
mvn -B -ntp -DskipTests clean deploy -Pstatic-repo-publish `
  '-DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}' `
  "-pl=!application/habiTv"
```

Linux/macOS (metadata and `index.html` only; tool ZIP downloads require PowerShell):

```bash
mvn -B -ntp -DskipTests clean deploy -Pstatic-repo-publish \
  '-DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}' \
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
  '-DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}' `
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

Tool sources: `scripts/static-repo/tool-sources.properties`.

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

When `-Dhabitv.update.enabled=true` (optional `-Dhabitv.update.url=...`):

1. **Plugins** — `plugins.txt` or `habitv-update-manifest.properties` plugin entries.
2. **Artifact versions** — manifest, optional `index.html`, then Maven path layout.
3. **External tools** — manifest `tool` entries, then `tools/<name>/<version>/<file>`.

Updates are **disabled by default**. If GitHub Pages is unreachable, Habitv keeps
local plugins and tools.

## Validation URLs

- https://mika3578.github.io/habitv-repo/repository/
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

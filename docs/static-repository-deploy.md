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

## Default local staging path

Root `pom.xml` defines:

```xml
<habitv.static.repo.path>${user.home}/dev/habitv-repo/repository</habitv.static.repo.path>
```

This expands per OS (forward slashes, no hardcoded username):

| OS | Example |
|----|---------|
| Windows | `C:/Users/<user>/dev/habitv-repo/repository` |
| Linux | `/home/<user>/dev/habitv-repo/repository` |
| macOS | `/Users/<user>/dev/habitv-repo/repository` |

Inspect the resolved value:

```bash
mvn help:evaluate -Dexpression=habitv.static.repo.path -q -DforceStdout
```

Override for a non-default checkout:

```bash
mvn -Dhabitv.static.repo.path=/path/to/habitv-repo/repository help:evaluate \
  -Dexpression=habitv.static.repo.path -q -DforceStdout
```

## Workspace layout

```text
${user.home}/dev/
  habitv/                      # application (clone)
  habitv-repo/                 # static artifacts (clone)
    repository/
      com/dabi/habitv/...      # Maven layout
      tools/...                # external tools (zip/exe)
      plugins.txt
      habitv-update-manifest.properties
      index.html               # optional directory listings
```

## Maven deploy (preferred: `altDeploymentRepository`)

`distributionManagement` references `file://${habitv.static.repo.path}` for
modules that deploy without overrides. For portable, explicit staging, prefer:

```bash
mvn -B -ntp -DskipTests clean deploy \
  -DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}
```

Use single quotes on Unix shells so `${habitv.static.repo.path}` is resolved by
Maven, not the shell:

```bash
mvn -B -ntp -DskipTests clean deploy \
  '-DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}'
```

### Overrides

Linux/macOS:

```bash
mvn -B -ntp -DskipTests clean deploy \
  -Dhabitv.static.repo.path="$HOME/dev/habitv-repo/repository" \
  -DaltDeploymentRepository=habitv-local::default::file://$HOME/dev/habitv-repo/repository
```

Windows PowerShell:

```powershell
mvn -B -ntp -DskipTests clean deploy `
  "-Dhabitv.static.repo.path=$env:USERPROFILE/dev/habitv-repo/repository" `
  "-DaltDeploymentRepository=habitv-local::default::file:///$env:USERPROFILE/dev/habitv-repo/repository"
```

Or property-only (Maven resolves the path; recommended when using the default):

```powershell
mvn -B -ntp -DskipTests clean deploy `
  '-DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}'
```

Helper scripts (deploy only, no `git push`):

```bash
./scripts/static-repo/deploy-static-repo.sh
```

```powershell
.\scripts\static-repo\deploy-static-repo.ps1
```

Optional script argument / `-StaticRepoPath` sets `-Dhabitv.static.repo.path=...`.

## Manual publication to GitHub Pages

After `mvn deploy`, from the `habitv-repo` checkout (parent of `repository/`):

```bash
cd "$(dirname "$(mvn help:evaluate -Dexpression=habitv.static.repo.path -q -DforceStdout)")"
git status --short
git add repository
git commit -m "repo: publish habitv artifacts"
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

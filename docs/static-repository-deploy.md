# Static repository publication (`habitv-repo`)

Habitv publishes Maven artifacts and external tool binaries to a sibling Git
checkout that is served publicly via GitHub Pages.

| Role | Location |
|------|----------|
| Application sources | `habitv/` (this repository) |
| Published static tree | `../habitv-repo/repository/` |
| Public HTTPS base | `https://mika3578.github.io/habitv-repo/repository/` |

Runtime downloads always use the **HTTPS** URL. The local `file://` path is
only for `mvn deploy` from a developer machine.

## Workspace layout

```text
dev/
  habitv/                 # application (this repo)
  habitv-repo/            # static artifacts (GitHub Pages source)
    repository/
      com/dabi/habitv/... # Maven layout
      tools/...           # external tools (zip/exe)
      plugins.txt         # optional plugin id list
      habitv-update-manifest.properties
      index.html          # optional Apache-style listings per directory
```

## Maven deploy (local file repository)

The root `pom.xml` defines:

- `habitv.deploy.repo.path` — default `${maven.multiModuleProjectDirectory}/../habitv-repo/repository`
- `distributionManagement` — `file://${habitv.deploy.repo.path}` (`habitv-local`)

From `habitv/` (Linux/macOS):

```bash
mvn -B -ntp -DskipTests clean deploy \
  -DaltDeploymentRepository=habitv-local::default::file://../habitv-repo/repository
```

Windows PowerShell:

```powershell
mvn -B -ntp -DskipTests clean deploy `
  "-DaltDeploymentRepository=habitv-local::default::file://../habitv-repo/repository"
```

Override the local path:

```bash
mvn -B -ntp -DskipTests deploy \
  -Dhabitv.deploy.repo.path=/absolute/path/to/habitv-repo/repository
```

Helper scripts (deploy only, no `git push`):

```bash
./scripts/static-repo/deploy-static-repo.sh
```

```powershell
.\scripts\static-repo\deploy-static-repo.ps1
```

## Manual publication to GitHub Pages

After `mvn deploy`:

```bash
cd ../habitv-repo
git status --short
git add repository
git commit -m "repo: publish habitv artifacts"
git push
```

GitHub Pages serves `repository/` at
`https://mika3578.github.io/habitv-repo/repository/`.

## Runtime update discovery

When `-Dhabitv.update.enabled=true` (optional `-Dhabitv.update.url=...`):

1. **Plugins** — `plugins.txt` (one artifact id per line), or
   `habitv-update-manifest.properties` plugin entries.
2. **Artifact versions** — manifest first, then optional `index.html` directory
   listings, then Maven path `com/dabi/habitv/<artifactId>/...`.
3. **External tools** — manifest `tool` entries, then
   `tools/<tool-name>/<version>/<file>` (e.g. `tools/ffmpeg/3.0/ffmpeg.zip`).

Manifest line format (pipe-separated):

```text
type|groupId|artifactId|version|packaging|relativeUrl|checksum
```

Example:

```text
plugin|com.dabi.habitv|6play|4.1.0-SNAPSHOT|jar|com/dabi/habitv/6play/4.1.0-SNAPSHOT/6play-4.1.0-SNAPSHOT.jar
tool|com.dabi.habitv|ffmpeg|3.0|zip|tools/ffmpeg/3.0/ffmpeg.zip
```

See `scripts/static-repo/habitv-update-manifest.properties.example`.

Updates are **disabled by default**. If GitHub Pages is unreachable, Habitv
keeps installed local plugins and tools.

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
mkdir "$HOME\dev"; cd "$HOME\dev"
git clone https://github.com/Mika3578/habitv.git
git clone https://github.com/Mika3578/habitv-repo.git
```

Optional override: `HABITV_STATIC_REPO_LOCAL_PATH` or script `-StaticRepoPath`.

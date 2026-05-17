## Recommended cross-OS workspace

```text
$HOME/dev/
  habitv/
  habitv-repo/
```

## Windows setup

```powershell
mkdir "$HOME\dev"
cd "$HOME\dev"
git clone https://github.com/Mika3578/habitv.git
git clone https://github.com/Mika3578/habitv-repo.git
cd habitv
.\scripts\static-repo\deploy-static-repo.ps1
```

## Linux/macOS setup

```sh
mkdir -p "$HOME/dev"
cd "$HOME/dev"
git clone https://github.com/Mika3578/habitv.git
git clone https://github.com/Mika3578/habitv-repo.git
cd habitv
./scripts/static-repo/deploy-static-repo.sh
```

## Override path

Windows:

```powershell
.\scripts\static-repo\deploy-static-repo.ps1 -StaticRepoPath "D:\repositories\habitv-repo"
```

Linux/macOS:

```sh
./scripts/static-repo/deploy-static-repo.sh "/opt/repositories/habitv-repo"
```

## Environment variable override

Windows PowerShell:

```powershell
$env:HABITV_STATIC_REPO_LOCAL_PATH="D:\repositories\habitv-repo"
```

Linux/macOS:

```sh
export HABITV_STATIC_REPO_LOCAL_PATH="/opt/repositories/habitv-repo"
```

## Public URLs

- Maven: https://mika3578.github.io/habitv-repo/maven/
- Tools: https://mika3578.github.io/habitv-repo/tools/
- Manifests: https://mika3578.github.io/habitv-repo/manifests/

## Static repository content layout

```text
habitv-repo/
  maven/
    com/
      dabi/
        habitv/
          ...
  tools/
    yt-dlp/
    ffmpeg/
    aria2/
    curl/
    rtmpdump/
  manifests/
    plugins.json
    tools.json
  README.md
```

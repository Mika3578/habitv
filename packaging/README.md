# Habitv packaging assets

This directory holds shared packaging assets for Habitv distributions.

## Shared staging

After a Maven package build:

```bash
mvn -B -ntp -DskipTests package
```

Stage built JARs with:

- Linux/macOS: `bash scripts/stage-package.sh`
- Windows: `powershell -ExecutionPolicy Bypass -File scripts/stage-package.ps1`

Output layout:

```text
target/package-staging/Habitv/
  lib/
    habiTv-<version>.jar
    grabconfig.xml   minimal valid grab config (local mode + empty plugins)
    plugins/
  bin/          reserved for platform launchers in follow-up PRs
  README.txt
```

Platform-specific installers and CI artifact upload are tracked in separate PRs.

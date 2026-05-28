# Habitv cross-platform packaging

This directory holds launcher templates, installer metadata, and platform-specific
packaging assets used by the scripts under `scripts/`.

Build flow:

1. `mvn -B -ntp -DskipTests package`
2. `scripts/stage-package.sh` (Linux/macOS) or `scripts/stage-package.ps1` (Windows)
   — collect JARs into `target/package-staging/Habitv/`
3. `scripts/package-<platform>.{sh,ps1}` — produce native artifacts under
   `target/packages/`

See [`docs/packaging.md`](../docs/packaging.md) for full documentation.

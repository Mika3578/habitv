# Linux packaging

Builds on the shared staging script `scripts/stage-package.sh`.

Windows packaging is documented in [`../windows/README.md`](../windows/README.md).

## Prerequisites

- Maven build: `mvn -B -ntp -DskipTests package`
- **Java 8 with JavaFX** on `PATH` (for example Liberica JDK 8 Full or Zulu 8 FX).  
  Linux packages do **not** bundle a JDK/JRE.
- `dpkg-deb` (Debian `dpkg-dev` package) to build the `.deb`

## Build

```bash
mvn -B -ntp -DskipTests package
bash scripts/stage-package.sh   # optional; package-linux.sh runs staging
bash scripts/package-linux.sh
```

## Artifacts

Written under `target/packages/`:

| Artifact | Description |
|----------|-------------|
| `habitv-linux.tar.gz` | Portable layout: `Habitv/bin/habitv`, `Habitv/lib/` |
| `habitv.deb` | Installs under `/opt/habitv`, symlink `/usr/bin/habitv` |

## Portable archive

Extract and run:

```bash
tar -xzf target/packages/habitv-linux.tar.gz
./Habitv/bin/habitv
```

The launcher resolves paths relative to `bin/` (works for extracted trees and `/opt/habitv`).

## DEB install

```bash
sudo apt install ./target/packages/habitv.deb
habitv
```

Uninstall:

```bash
sudo apt remove habitv
```

Optional desktop entry: `/usr/share/applications/habitv.desktop`.

## Non-goals (this tree)

- No bundled Java runtime
- No RPM or AppImage
- No macOS or Windows installers (separate PRs)

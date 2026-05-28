# Habitv cross-platform packaging

This document describes how to build portable archives and native installers for
Habitv on Windows, Linux, and macOS.

Habitv remains a Java 8 application in this PR. Native installers install Habitv
files and launchers, but they do not bundle a Java runtime.

## Prerequisites

- **JDK 8 with JavaFX** for building and running the GUI application (for example
  [Liberica JDK 8 Full](https://bell-sw.com/pages/downloads/) or Zulu 8 FX).
- Maven 3.6+
- Platform-specific tools for native installers (see below).

## Maven build

Build the full reactor first:

```bash
mvn -B -ntp -DskipTests package
```

This produces:

- Main application JAR: `application/habiTv/target/habiTv-<version>.jar`
- Plugin JARs: `plugins/<name>/target/<name>-<version>.jar`

The main JAR is a shaded/fat JAR launched by `com.dabi.habitv.HabitvLauncher`.

## Shared staging layout

All packaging scripts call `scripts/stage-package.sh`, which collects artifacts into:

```text
target/package-staging/Habitv/
  bin/                 OS launchers (added by platform scripts)
  lib/
    habiTv-<version>.jar
    plugins/           provider/downloader/exporter plugin JARs
  README.txt
```

At runtime, Habitv resolves its application directory from the main JAR location.
Plugin JARs must live in `lib/plugins/` relative to that directory.

## Output artifacts

Final packages are written to `target/packages/`:

| Platform | Required artifact | Path |
|----------|-------------------|------|
| Windows | Portable ZIP | `target/packages/habitv-windows.zip` |
| Windows | EXE launcher | `target/packages/bin/Habitv.exe` |
| Windows | Batch launcher | `target/packages/bin/habitv.bat` |
| Windows | Inno Setup installer | `target/packages/HabitvSetup.exe` |
| Linux | Portable archive | `target/packages/habitv-linux.tar.gz` |
| Linux | Shell launcher | `target/packages/bin/habitv` |
| Linux | Debian package | `target/packages/habitv.deb` |
| macOS | Portable archive | `target/packages/habitv-macos.tar.gz` |
| macOS | Shell launcher | `target/packages/bin/habitv` |
| macOS | App bundle | `target/packages/Habitv.app` |
| macOS | Disk image | `target/packages/Habitv.dmg` |

Optional artifacts (RPM, MSI, PKG, AppImage) are intentionally not produced in
this PR to keep CI reliable.

## Windows

### Tools

- [Launch4j](https://launch4j.sourceforge.net/) for `Habitv.exe`
- [Inno Setup 6](https://jrsoftware.org/isinfo.php) for `HabitvSetup.exe`
- Git Bash (for `scripts/stage-package.sh`)

Chocolatey installs both tools in CI:

```powershell
choco install launch4j innosetup -y
```

### Build locally

```powershell
mvn -B -ntp -DskipTests package
powershell -ExecutionPolicy Bypass -File scripts/package-windows.ps1
```

Validation:

```powershell
Test-Path target/packages/habitv-windows.zip
Test-Path target/packages/bin/Habitv.exe
Test-Path target/packages/bin/habitv.bat
Test-Path target/packages/HabitvSetup.exe
```

For staging-only validation without Launch4j/Inno Setup:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/package-windows.ps1 -SkipLaunch4j -SkipInnoSetup
```

### Behavior

- `habitv.bat` and `Habitv.exe` resolve the install directory relative to `bin/`.
- Launch4j shows a clear message when Java is missing.
- `HabitvSetup.exe` installs under `%LOCALAPPDATA%\Programs\Habitv` without
  requiring administrator rights.
- The installer creates Start Menu and optional Desktop shortcuts.
- An uninstaller is included.

## Linux

### Tools

- `bash`, `tar`, `gzip`
- `dpkg-deb` (`dpkg-dev` package on Debian/Ubuntu)

### Build locally

```bash
mvn -B -ntp -DskipTests package
bash scripts/package-linux.sh
```

Validation:

```bash
test -f target/packages/habitv-linux.tar.gz
test -f target/packages/habitv.deb
test -x target/package-staging/Habitv/bin/habitv
```

### Behavior

- Portable archive extracts to a `Habitv/` directory runnable from any location.
- `.deb` installs files under `/opt/habitv`.
- `/usr/bin/habitv` symlink is created on install.
- Desktop entry: `/usr/share/applications/habitv.desktop`.
- Java dependency is documented in package metadata but not enforced strictly
  because Java package names vary across distributions.

## macOS

### Tools

- `bash`, `tar`, `hdiutil`

### Build locally

```bash
mvn -B -ntp -DskipTests package
bash scripts/package-macos.sh
```

Validation:

```bash
test -f target/packages/habitv-macos.tar.gz
test -d target/packages/Habitv.app
test -f target/packages/Habitv.dmg
test -x target/package-staging/Habitv/bin/habitv
```

### Behavior

- `Habitv.app` bundles the staged application under
  `Contents/Resources/Habitv/`.
- The app launcher runs `java -jar` against the bundled main JAR.
- `Habitv.dmg` contains `Habitv.app` and a README about Java requirements.

### Unsigned macOS limitation

Packages are **unsigned and not notarized**. macOS Gatekeeper may block the first
launch. Users can allow the app from System Settings > Privacy & Security, or
remove the quarantine attribute:

```bash
xattr -dr com.apple.quarantine /Applications/Habitv.app
```

## CI workflow

`.github/workflows/package.yml` builds packages on matching runners:

- `ubuntu-latest` → Linux archive and DEB
- `windows-latest` → Windows ZIP, EXE, and installer
- `macos-latest` → macOS archive, app bundle, and DMG

Triggers: `pull_request` against `develop`, and `workflow_dispatch`.

Artifacts are uploaded per platform with 14-day retention.

## Portable archives vs native installers

| Type | Purpose |
|------|---------|
| Portable archive (`.zip`, `.tar.gz`) | Extract anywhere; no system integration |
| Native installer (`.exe`, `.deb`, `.dmg`) | Installs to a standard location with shortcuts/menu entries |

Both require a separately installed **Java 8 runtime with JavaFX support**.

## Non-goals (this PR)

- No Java migration beyond Java 8.
- No OpenJFX migration.
- No bundled JDK/JRE.
- No provider or replay behavior changes.
- No GitHub Release publishing.
- No code signing or notarization.
- No vendored third-party installer binaries in the repository.

## Future improvements

After Java/OpenJFX migration strategy is clarified:

1. Optional bundled runtime packages with `jpackage`.
2. Signed Windows installer.
3. Signed and notarized macOS DMG.
4. Optional RPM and AppImage packages if CI can produce them reliably.

See also:

- [`docs/java-runtime-policy.md`](java-runtime-policy.md)
- [`docs/runtime-quickstart.md`](runtime-quickstart.md)
- [`docs/ci.md`](ci.md)

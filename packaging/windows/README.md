# Windows packaging

Builds on the shared staging scripts in `scripts/stage-package.ps1`.

## Prerequisites

- Maven build: `mvn -B -ntp -DskipTests package`
- **Java 8 with JavaFX** on `PATH` (for example Liberica JDK 8 Full or Zulu 8 FX).  
  Windows packages do **not** bundle a JDK/JRE.
- **Launch4j 3.14+** (optional) — required to generate `Habitv.exe` (Chocolatey: `choco install launch4j -y`)
- **Inno Setup 6** (`ISCC.exe` on `PATH` or default install location) — required to generate `HabitvSetup.exe`

## Commands

Stage only:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/stage-package.ps1
```

Portable ZIP (no Launch4j / Inno Setup):

```powershell
powershell -ExecutionPolicy Bypass -File scripts/package-windows.ps1 -SkipLaunch4j -SkipInnoSetup
```

Full Windows packages (Launch4j + Inno Setup installed):

```powershell
powershell -ExecutionPolicy Bypass -File scripts/package-windows.ps1
```

## Artifacts

Written under `target/packages/`:

| Artifact | Description |
|----------|-------------|
| `habitv-windows.zip` | Portable layout: `Habitv/bin/`, `Habitv/lib/` |
| `Habitv.exe` | GUI launcher (Launch4j), also copied into the ZIP under `bin/` |
| `HabitvSetup.exe` | Inno Setup installer (Start Menu shortcut, uninstall support) |

Launchers resolve paths relative to `bin/` (`habitv.bat`, `Habitv.exe` → application root → `lib/`).

## Non-goals (this tree)

- No bundled Java runtime
- No Linux `.deb` or macOS `.app` / `.dmg` (separate PRs)

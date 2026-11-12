# Troubleshooting

Runtime and JDK: [`development.md`](development.md).
Providers: [`providers.md`](providers.md). Configuration:
[`configuration.md`](configuration.md).

## Build

| Symptom | Likely cause | What to do |
|---------|--------------|------------|
| GUI `package` fails with missing JavaFX classes | OpenJFX dependencies not resolved | Build with JDK 21 and let Maven resolve OpenJFX, or package console only |
| `mvn test` hits the network | Live `*PluginManagerTest` | Default Surefire excludes them; do not enable `-Plive-provider-tests` unless intended |
| `mvn verify` used as merge bar | Full lifecycle still gated | Required CI: validate + deterministic tests + Java 21 package |
| Compile fails on a JDK below 21 | Wrong build JDK | `maven.compiler.release` is `21`; build with JDK 21 |

## Run

| Symptom | Likely cause | What to do |
|---------|--------------|------------|
| No providers listed (`-lp`) | Plugin JARs missing | Copy module JARs into `plugins/` |
| Command not parsed (spaces) | Missing `<cmdProcessor>` | Windows: `cmd.exe /c #CMD#`. Unix: `/bin/sh -c #CMD#` |
| Plugin updates at startup | Default on | `-Dhabitv.update.enabled=false` disables plugin-JAR checks; tool plugins may still update binaries |
| France Télévisions empty | Legacy plugin id | `pluzz` → `francetv` in grab-config |
| 403 / DNS / empty replay | Obsolete or protected | See [`providers.md`](providers.md) |
| yt-dlp missing / SSL errors | Tool or network | Install yt-dlp; point `<youtube>` at the binary |
| Windows yt-dlp fails before retrieval (`[PYI-`, `_MEI`, extract errors) | PyInstaller bootstrap / TEMP | Habitv redirects yt-dlp `TEMP`/`TMP` under the bin home and runs `yt-dlp.exe --version` first. Stop Habitv, clear TEMP `_MEI*` folders, replace `yt-dlp.exe`, then run `--version` outside Habitv |

## Configuration

Do not delete user media files or indexes while debugging. Index files
store composite episode keys; older files may still use display names.
Sample `application/consoleView/config.xml` is illustrative only.

Vulnerabilities: [`../SECURITY.md`](../SECURITY.md).

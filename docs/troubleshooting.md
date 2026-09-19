# Troubleshooting

Runtime and JDK: [`development.md`](development.md).
Providers: [`providers.md`](providers.md). Configuration:
[`configuration.md`](configuration.md).

## Build

| Symptom | Likely cause | What to do |
|---------|--------------|------------|
| GUI `package` fails with missing JavaFX / `jfxrt` | Build JDK has no JavaFX | Use a JavaFX-capable JDK 8, or package console only |
| `mvn test` hits the network | Live `*PluginManagerTest` | Default Surefire excludes them; do not enable `-Plive-provider-tests` unless intended |
| `mvn verify` used as merge bar | Full lifecycle still gated | Required CI: validate + deterministic tests + Java 8 package |
| Java 9+ APIs in app code | Baseline drift | Bytecode is **1.8**; `maven.compiler.release` is unset |

## Run

| Symptom | Likely cause | What to do |
|---------|--------------|------------|
| No providers listed (`-lp`) | Plugin JARs missing | Copy module JARs into `plugins/` |
| Command not parsed (spaces) | Missing `<cmdProcessor>` | Windows: `cmd.exe /c #CMD#`. Unix: `/bin/sh -c #CMD#` |
| Plugin updates at startup | Default on | `-Dhabitv.update.enabled=false` |
| France Télévisions empty | Legacy plugin id | `pluzz` → `francetv` in grab-config |
| 403 / DNS / empty replay | Obsolete or protected | See providers; no DRM/paywall bypass |
| yt-dlp missing / SSL errors | Tool or network | Install yt-dlp; point `<youtube>` at the binary |
| Windows yt-dlp fails before download (`[PYI-`, `_MEI`, extract errors) | PyInstaller bootstrap / TEMP | Habitv redirects yt-dlp `TEMP`/`TMP` under the bin home and runs `yt-dlp.exe --version` first. Stop Habitv, clear TEMP `_MEI*` folders, replace `yt-dlp.exe`, then run `--version` outside Habitv |

## Configuration

Do not delete user downloads or indexes while debugging. Index keys are
episode display names. Sample `application/consoleView/config.xml` is
illustrative only.

Vulnerabilities: [`../SECURITY.md`](../SECURITY.md).

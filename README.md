# HabiTV

HabiTV is a Maven application that watches French TV replay catalogues
and saves new episodes via provider and tool plugins (yt-dlp, curl,
ffmpeg, …).

Canonical repository: [Mika3578/habitv](https://github.com/Mika3578/habitv).
Integration branch: [`develop`](https://github.com/Mika3578/habitv/tree/develop).

## Status

Current **build/runtime baseline is Java 8**. Java **21** is the
modernization target; Java **25** is next. Neither 21 nor 25 is
supported until the compiler and required CI change. Full table:
[`docs/development.md`](docs/development.md).

The Maven reactor and console path are the working baseline. Many
provider sites have changed. Runtime updates use HTTPS
[habitv-repo](https://mika3578.github.io/habitv-repo/repository/).

## Build

JDK 8, Maven 3.6+, Git. Commands: [`docs/development.md`](docs/development.md).

```bash
mvn -B -ntp -DskipTests validate
```

## Layout

`fwk/` (API + framework), `application/` (core, console, JavaFX GUI),
`plugins/` (providers, downloaders, exporters).
`application/habiTv-linux` and `application/habiTv-windows` are out of the default
reactor. See [`docs/architecture.md`](docs/architecture.md).

## Providers

Do not assume a module works because it compiles. HabiTV supports only
public, user-authorized workflows. Status: [`docs/providers.md`](docs/providers.md).

## Docs

| | |
|--|--|
| [`docs/development.md`](docs/development.md) | JDK, Maven, run, CI |
| [`docs/configuration.md`](docs/configuration.md) | XML config and naming |
| [`docs/troubleshooting.md`](docs/troubleshooting.md) | Common failures |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | How to contribute |
| [`AGENTS.md`](AGENTS.md) | Agent and public git-text rules |
| [`SECURITY.md`](SECURITY.md) | Vulnerability reporting |

No `LICENSE` file yet — treat sources as proprietary until one is added.

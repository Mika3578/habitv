# habiTv

[![Java](https://img.shields.io/badge/Java-1.7+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.0+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-lightgrey.svg)]()

habiTv automates the discovery and download of videos from TV replay/catch‑up and other sources. You define shows, series, or feeds to follow, and habiTv periodically checks for new episodes and downloads them automatically. Optional post‑processing commands (encode, copy/move, FTP, etc.) can run after a download completes.

Note on repository layout: this project uses a non‑standard Maven source layout. Production sources live under src/ and tests under test/ in each module. Commands below reflect this.

## Overview

- GUI tray app to monitor and notify downloads
- CLI to search, fetch, and run as a daemon with file logging
- Modular plugin system for providers (content discovery), downloaders (wrappers for external tools), and exporters (post‑processing)

## Tech stack

- Language: Java 7+ (built and tested commonly with JDK 8)
- Build system / package manager: Maven (multi‑POM layout)
- Tests: JUnit 4.x
- Logging: log4j 1.x
- Parsing/IO/libs: jsoup, Jackson, JAXB, Rome, Guava, Commons CLI

Entry point
- Main class: com.dabi.habitv.HabitvLauncher (packaged via maven‑shade‑plugin in application/habiTv)

## Requirements

- JDK 8 recommended (source/target 1.7 as per parent POM)
- Maven 3.x
- Optional external tools (needed depending on plugins you use):
  - youtube-dl
  - rtmpdump
  - curl
  - aria2c
  - ffmpeg

Environment and configuration
- Config directory: by default, a user‑level folder named habitv (the historical docs mention %USER_DIR%/habitv). TODO: Verify the exact path variable (%USER_HOME% or OS‑specific) and update here.
- XML configs:
  - config.xml: general application configuration
  - grabconfig.xml: categories/shows to download
- Example schemas: application/core/xsd/ (see XSDs for structure)

## Getting started

Clone and build

```bash
git clone https://github.com/Mika3578/habitv.git
cd habitv

# Full build (parent is not an aggregator; build per aggregator/module)
mvn -f fwk\pom.xml clean install
mvn -f plugins\pom.xml clean install
mvn -f application\pom.xml clean install
```

Build only the desktop launcher

```bash
mvn -f application\habiTv\pom.xml clean package
```

Run

```bash
# GUI (tray) or CLI; shaded JAR produced by the habiTv module
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar

# CLI options
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar --help
```

Notes
- Some plugins wrap native binaries. Ensure those tools are installed and available on PATH before using the related providers/downloaders/exporters.
- On JDK 9+, you may need toolchain adjustments for legacy target 1.7. JDK 8 is the safest default.

## Scripts and useful Maven commands

- Build and test a single module (example: fwk/api)
  - mvn -f fwk\api\pom.xml clean test
- Run tests for all plugins (aggregator):
  - mvn -f plugins\pom.xml test
- Build the main application launcher:
  - mvn -f application\habiTv\pom.xml clean package
- Run all application module tests via the aggregator:
  - mvn -f application\pom.xml test
- Run one test class via Surefire (-Dtest filter):
  - mvn -f fwk\api\pom.xml -Dtest=SomeTestName test

## Configuration and environment variables

- Primary configs: config.xml, grabconfig.xml
- Location search order: TODO — confirm whether files next to the executable override the user config directory (historical behavior suggests so).
- Logging: log4j configuration is classpath‑driven (log4j.properties). TODO — document default log location and how to enable file logging for daemon mode.

## Supported providers and plugins

The repository contains provider, downloader, and exporter plugins under plugins/.

Known modules (subject to availability/maintenance):
- Providers: CanalPlus, Pluzz, Arte, BeinSport, L’Equipe, 6play, SFR, WAT, GlobalNews, MLSSoccer, Footyroom, Clubic, RSS, File, Email, YouTube
- Downloaders: youtube-dl, rtmpDump, curl, aria2c, ffmpeg, adobeHDS
- Exporters: ffmpeg, curl, cmd

TODO: Verify the current operational status of each provider against the target sites’ latest changes.

## Project structure

```
habitv/
├── application/            # Desktop and CLI apps (non‑standard src/ and test/)
│   ├── core/               # Domain/core logic and configuration schemas (XSD)
│   ├── consoleView/        # Command‑line UI
│   ├── trayView/           # Tray/GUI
│   └── habiTv/             # Shaded launcher (main class: HabitvLauncher)
├── fwk/                    # Framework libraries used by apps/plugins
│   ├── api/                # Public API and DTOs
│   └── framework/          # Utilities/helpers and command execution layer
├── plugins/                # Providers, downloaders, exporters (many submodules)
├── pom.xml                 # Root parent POM (packaging=pom; not an aggregator)
└── README.md
```

## Testing

- Framework
  - mvn -f fwk\framework\pom.xml test
- API
  - mvn -f fwk\api\pom.xml test
- Plugins (all)
  - mvn -f plugins\pom.xml test
- Application (all app modules)
  - mvn -f application\pom.xml test
- Single test class
  - mvn -f <module\pom.xml> -Dtest=ClassName test

Guidelines
- Tests live under test/ (not src/test/java) and use JUnit 4 (org.junit.Test).
- Avoid network and external tool dependencies in unit tests; mock execution layers where possible.

## Contribution

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and follow the non‑standard src/ and test/ layout used by this repository.

## License

MIT — see [LICENSE](LICENSE).

## Credits

Originally developed by dabi. Thanks to all contributors who helped improve habiTv.

## TODOs

- Confirm exact config directory and precedence (user folder vs. executable folder).
- Document CLI arguments and examples.
- Verify current provider support matrix and mark deprecated/broken ones.
- Add guidance for logging configuration and daemon mode.

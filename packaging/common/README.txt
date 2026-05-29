Habitv
======

Habitv is a Java 8 application for automatic French TV replay downloads.

Requirements
------------
- Java 8 runtime with JavaFX support (for example Liberica JDK 8 Full or Zulu 8 FX).
- This package does NOT include a Java runtime.

Layout
------
  lib/              Main application JAR
  lib/grabconfig.xml  Minimal valid grab config (local mode, empty categories)
  lib/plugins/        Provider, downloader, and exporter plugin JARs

The staged grabconfig.xml is parsed at runtime (not a mere marker file). It
enables local mode so bundled lib/plugins are loaded; populate categories via
the application UI or console update options after install.

More information: https://github.com/Mika3578/habitv

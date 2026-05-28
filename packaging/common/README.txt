Habitv
======

Habitv is a Java 8 application for automatic French TV replay downloads.

Requirements
------------
- Java 8 runtime with JavaFX support (for example Liberica JDK 8 Full or Zulu 8 FX).
- This package does NOT include a Java runtime.

Layout
------
  bin/     OS launchers (habitv, habitv.bat, Habitv.exe)
  lib/     Main application JAR and bundled runtime libraries
  lib/plugins/  Provider, downloader, and exporter plugin JARs

Portable use
------------
Extract the archive anywhere and run the launcher from bin/.
Configuration and downloads are stored under your user profile unless
configuration.xml is placed next to the application JAR directory.

More information: https://github.com/Mika3578/habitv

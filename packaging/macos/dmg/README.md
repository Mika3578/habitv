Habitv for macOS
================

Habitv is a Java 8 application. Drag Habitv.app to Applications, then launch it.

Requirements
------------
- Java 8 runtime with JavaFX support (for example Liberica JDK 8 Full).
- This package does NOT include a Java runtime.

Gatekeeper
----------
This build is unsigned and not notarized. macOS may block the first launch.
Open System Settings > Privacy & Security and choose Open Anyway, or run:

  xattr -dr com.apple.quarantine /Applications/Habitv.app

More information: https://github.com/Mika3578/habitv

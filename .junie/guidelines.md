Project-specific development guidelines for habiTv

This document captures build, test, and development conventions that are specific to this repository so advanced contributors can be productive quickly.

1) Build and configuration instructions

- Build tool: Maven (multi-POM layout).
- Minimum recommended JDK: 8. The parent POM compiles sources with source/target 1.7 via maven-compiler-plugin 3.1. Using JDK 8 avoids modern toolchain flags that Java 9+ might require for legacy targets. If you build with JDK 9+, be prepared to adjust toolchains or plugin configuration.
- Non-standard source layout (important): The parent POM overrides Maven defaults:
  - Main sources: src (not src/main/java)
  - Test sources: test (not src/test/java)
  Ensure any new code/tests follow this layout per module.
- Repository structure (high level):
  - application/ (multiple app modules)
  - fwk/ (framework modules; e.g., fwk/api, fwk/framework)
  - plugins/ (plugin aggregator with many submodules)
  - Root POM (packaging=pom) provides dependencyManagement and build config but does not aggregate modules directly. Build per-aggregator or per-module.
- External runtime tools for some plugins: Certain plugins wrap native/CLI tools. Install and make them available on PATH if you intend to run those plugins or their integration tests.
  - ffmpeg (plugins/ffmpeg)
  - rtmpdump (plugins/rtmpDump)
  - aria2c (plugins/aria2)
  - curl (plugins/curl)
  - Others may leverage system mail, etc. Review each plugin’s README/POM before executing integration-like tests.
- Custom repositories: The root POM declares a repository at http://dabiboo.free.fr/repository and distributionManagement via FTP. Normal development builds resolve dependencies from Maven Central plus that HTTP repo. If the HTTP repo is unavailable, you may need to provide local mirrors or pre-populate ~/.m2.

Common build commands (Windows PowerShell syntax for paths):

- Build and test an individual module (example: fwk/api):
  mvn -f fwk\api\pom.xml clean test

- Run tests for all plugins via the plugins aggregator:
  mvn -f plugins\pom.xml test

- Build a specific application module (e.g., application/habiTv):
  mvn -f application\habiTv\pom.xml clean package

Notes:
- Some POMs mix parent versions 4.1.0 and 4.1.0-SNAPSHOT. Maven may warn about parent.relativePath; this is benign for local builds. If you restructure modules, keep parent relationships coherent.

2) Testing information

Test layout and framework

- Unit tests use JUnit 4.x (managed in the parent POM). Use org.junit.Test and org.junit.Assert.*.
- Place tests under the module’s test directory mirroring package names, for example:
  fwk/api/test/com/dabi/habitv/api/MyClassTest.java
- Many existing tests live across application/core/test and individual plugin modules under their respective test folders. Review them for patterns and utilities.

Running tests

- Single module, all tests:
  mvn -f fwk\framework\pom.xml test

- Single test class in a module (Surefire -Dtest filter):
  mvn -f fwk\api\pom.xml -Dtest=GuidelinesSmokeTest test

- Run all plugin tests via aggregator:
  mvn -f plugins\pom.xml test

- Run all tests for application modules via aggregator:
  mvn -f application\pom.xml test

Adding new tests (guidelines)

- Put test code under test/, not src/test/java. Example skeleton:
  package com.dabi.habitv.something;
  import org.junit.Test;
  import static org.junit.Assert.*;
  public class MyFeatureTest {
      @Test public void doesWhatItSays() { assertTrue(true); }
  }
- Name test classes with the *Test suffix so Surefire discovers them.
- Avoid network and external tool dependencies in unit tests. If a plugin needs external binaries, mock the executor layer or guard the test with environment checks to keep CI stable.

Verified example (executed locally before writing these guidelines)

- A minimal JUnit test was added temporarily under fwk/api/test and executed with:
  mvn -f fwk\api\pom.xml -Dtest=GuidelinesSmokeTest test
  The Surefire output reported BUILD SUCCESS with 1 test run.
  The temporary class was removed after verification to keep the repository clean.

3) Additional development information

Code style and conventions

- Java 7 language level for production modules; prefer APIs compatible with Java 7 unless you explicitly upgrade compiler settings in the relevant module.
- Logging: log4j 1.x is used in legacy code. Be conscious of classpath clashes if adding newer logging stacks. If you must introduce SLF4J/log4j2, do so incrementally and document bridges.
- Dependencies are managed centrally in the root POM’s dependencyManagement. In child modules, declare dependencies without versions when possible so the BOM-like versions apply consistently.

Building and running the desktop apps

- Application modules (under application/) package UI artifacts and resources from src. Review their POMs for JavaFX/FXML resource inclusion patterns (the parent config includes resource filtering and includes *.properties, *.fxml from src).

Debugging tips

- Many providers/parsers rely on jsoup and HTTP. When debugging parsing failures, capture raw HTML and add regression tests around selector logic in small, isolated tests that do not hit the network.
- For command-based plugins (ffmpeg/curl/aria2/rtmpdump), the framework layer provides command execution utilities (see fwk/framework). Add verbose logging at the command invocation boundary to troubleshoot PATH and exit code issues.
- If Maven cannot find modules you expect at the root, invoke mvn with -f pointing to a specific aggregator POM (plugins/pom.xml, application/pom.xml, fwk/pom.xml) or directly to a child module POM.

Release and versioning notes

- Distribution management points to an FTP repository; this is historical. Do not deploy there unless you are a maintainer. For local development, prefer mvn install and run from target/.

Housekeeping

- Keep to the non-standard src/ and test/ layout unless a module explicitly overrides it. Mixing standard and non-standard directories will lead to tests not being compiled or executed.
- If you add integration tests that require network access or external binaries, mark them or separate them (e.g., surefire includes/excludes) so regular unit test runs remain reliable.

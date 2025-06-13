# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- Added missing Maven modules to parent POMs:
  - Added `habiTv-linux` and `habiTv-windows` modules to `application/pom.xml`
  - Added `api` and `framework` modules to `fwk/pom.xml`
  - Added `plugin-tester` module to `plugins/pom.xml`
- Added project structure documentation to README.md
- Migrated to Java 21 and JavaFX 21.0.2:
  - Updated maven-compiler-plugin to use Java 21
  - Replaced system dependencies on `javafx:jfxrt` with org.openjfx modules (javafx-controls, javafx-fxml, javafx-graphics)
  - Added org.openjfx:javafx-maven-plugin to modules with JavaFX main classes
  - Updated documentation to reflect the migration
- Modernized build for Java 21 and JavaFX 21.0.2
- All modules now use Java 21 for compilation
- Removed all references to Java 1.7/1.8
- Updated maven-compiler-plugin to 3.11.0 where needed
- Ensured all submodules inherit or override Java 21 config
- Main application (GUI/CLI) is now built as a fat JAR with all dependencies using Maven Shade Plugin.
- Java 21 and JavaFX 21.0.2 compatibility for all modules.
- All entity classes are generated from XSDs and included in the build.
- build: generate JavaFX executable JAR for habiTv with Maven Shade

### Fixed
- Fixed Maven parent POM resolution issues:
  - Updated parent version in root `pom.xml` from `4.1.0-SNAPSHOT` to `4.1.0`
  - Updated parent version in all module POMs to consistently use `4.1.0`
  - Added `<relativePath/>` to all parent references to force repository resolution
  - Added repository configuration to all POMs to ensure access to the Maven mirror
  - Fixed invalid repository tag in root POM (`<n>` to `<name>`)
- Fixed JAXB code generation issues:
  - Added version (1.1.1) to maven-jaxb-plugin in core module
  - Added Maven Central and Java.net repositories to core module for plugin resolution
  - Ensured proper generation of com.dabi.habitv.grabconfig.entities, com.dabi.habitv.config.entities, and com.dabi.habitv.configuration.entities packages
- Fixed version mismatch in parent POM references:
  - Updated parent version in `application/pom.xml` from `4.1.0` to `4.1.0-SNAPSHOT` (reverted)
  - Updated parent version in `plugins/pom.xml` from `4.1.0` to `4.1.0-SNAPSHOT` (reverted)
- **Fixed entity class compilation issues (2025-06-13):**
  - Regenerated all JAXB entity classes from XSD schemas using maven-jaxb-plugin
  - Fixed boolean getter method names in generated classes (using 'is' prefix instead of 'get' for boolean properties)
  - Updated XMLUserConfig.java to use correct method names: `isUpdateOnStartup()`, `isAutoriseSnapshot()`
  - Updated GrabConfigDAO.java to use correct method names: `isDownload()`, `isDownloadable()`, `isTemplate()`, `isDeleted()`, `isToDownload()`
  - Fixed JAXB marshalling in XMLUserConfig.java to use FileOutputStream instead of File directly
  - Added missing maven-compiler-plugin configurations to consoleView and trayView modules for Java 21 compatibility
  - Core and consoleView modules now compile successfully
  - Resolved all "cannot resolve type" and "syntax error" issues in generated entity classes
- Removed legacy JavaFX classpath code from HabitvLauncher for Java 21 compatibility.
- All modules now build without 'cannot resolve type' or syntax errors.

## [4.1.0-SNAPSHOT] - Current Development Version

This is the current development version of habiTv.

- Removed duplicate manual sources in core module; now only generated entity classes are used (Category, CategoryType, Channel, GrabConfig, ObjectFactory, Parameter, Plugin, Config)
- Marked network-dependent test TestUrl as @Ignore to skip in CI (integration test)

chore: replaced legacy dabiboo HTTP references with secure GitHub Pages Maven mirror

# Changelog

All notable changes to habiTv will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Plugin Auto-Update Security**: Disabled automatic plugin updates by default for enhanced security
- **Security-First Configuration**: Plugin updates now require explicit user consent via configuration
- **Enhanced Security Documentation**: Comprehensive security considerations and configuration guides
- **Unified Logging System**: Centralized log4j 1.2.15 configuration with thread-safe operation
- **HabitvLogger Class**: New centralized logging manager with automatic initialization
- **Custom Log Configuration**: Support for `habitv-log.properties` with fallback mechanisms
- **Enhanced Log Format**: Standardized format with timestamp, level, logger name, and message
- **Automatic Log Directory**: Creates `log/` directory and manages log file rotation
- **Environment Variable Support**: `HABITV_LOG_LEVEL` for runtime log level configuration
- **Comprehensive Logging Documentation**: Detailed logging configuration guide in README.md
- Comprehensive documentation suite
- Environment variables support
- Improved error handling and logging
- Enhanced plugin development framework
- Updated documentation with current project state
- Added last updated timestamps to documentation

### Changed
- **Plugin Update Behavior**: Changed default plugin auto-update from enabled to disabled
- **Configuration Logic**: Modified updateOnStartup() to return false by default, requiring explicit true setting
- **Security Model**: Implemented opt-in approach for plugin updates to prevent unauthorized code execution
- **Logging Infrastructure**: Replaced scattered System.out/System.err usage with proper logging
- **Main Application Classes**: Updated HabitvLauncher, ConsoleLauncher, CoreManager to use unified logging
- **Framework Classes**: Updated ZipUtils and other utility classes with proper logging
- **Plugin Classes**: Standardized logging across plugin modules
- **LogUtils Class**: Enhanced with backward compatibility and deprecated methods
- Updated Java compatibility from 1.7 to 1.8
- Removed hardcoded JavaFX dependencies
- Fixed deprecated API usage
- Improved build configuration
- Enhanced documentation structure and clarity
- Updated all documentation links and references

### Fixed
- **System.out/System.err Usage**: Replaced all direct console output with proper logging
- **Logging Configuration**: Fixed missing configuration file handling with fallback support
- **Thread Safety**: Ensured all logging operations are thread-safe
- **Performance**: Optimized logging for minimal performance impact
- Java version compatibility issues
- Missing plugin version specifications
- Deprecated URL constructor usage
- Unused import warnings
- Markdown linting issues
- Documentation inconsistencies and outdated information

### Security
- **Plugin Auto-Update Security**: Disabled automatic plugin updates by default to prevent unauthorized code execution
- **Reduced Attack Surface**: Eliminated potential security risks from automatic plugin downloads
- **User Consent Model**: Implemented explicit opt-in requirement for plugin updates
- **Security Documentation**: Added comprehensive security considerations and best practices

### Debug Improvements
- **Configuration File Path Logging**: Added debug output to show which configuration.xml file is being loaded at startup
- **Path Resolution Debugging**: Enhanced logging to display all attempted configuration file paths and resolution logic
- **XML Parsing Debugging**: Added logging before and after XML parsing operations to help troubleshoot configuration issues

### Technical
- **Log Format**: `[yyyy-MM-dd HH:mm:ss.SSS] [LEVEL] [LOGGER] Message`
- **Log Outputs**: Console (INFO+) and File (DEBUG+) with automatic rotation
- **Configuration**: `habitv-log.properties` with environment variable override support
- **Fallback**: Automatic fallback to console-only logging if configuration fails
- **Performance**: Thread-safe, buffered file writing, level filtering

## [4.1.0-SNAPSHOT] - 2025-06-15

### Added
- **yt-dlp Integration**: Replaced youtube-dl with yt-dlp for better YouTube support
- **Enhanced Download Commands**: Improved format selection and subtitle handling
- **Binary Distribution**: Added yt-dlp binaries for Windows and Linux in bin/ directory

### Changed
- **YouTube Plugin**: Updated to use yt-dlp instead of youtube-dl
- **Configuration**: Updated default downloader configuration to use yt-dlp
- **Documentation**: Updated all references from youtube-dl to yt-dlp

### Fixed
- **Arte Plugin**: Fixed to work with current website structure using JSON parsing
- **Plugin Compilation**: All 22 plugins now compile successfully
- **Build System**: Optimized Maven build process

### Technical
- **External Tools**: rtmpDump, curl, aria2c, yt-dlp, ffmpeg
- **Java Version**: Requires Java 8 or higher
- **Build Tool**: Maven 3.6+

### Current Features
- **Content Providers**: Support for major French TV channels (Canal+, Arte, Pluzz, etc.)
- **Download Methods**: Multiple download protocols (HTTP, RTMP, HLS, etc.)
- **Export Options**: Post-processing and file conversion capabilities
- **Dual Interface**: GUI (system tray) and CLI modes
- **Plugin System**: Extensible architecture for new content sources
- **Automatic Monitoring**: Background checking for new episodes
- **Multi-Platform**: Windows and Linux support
- **Environment Configuration**: Flexible configuration via environment variables

### Supported Platforms
- **Canal+**: Premium French TV content
- **Pluzz**: France TV channels (France 2, 3, 4, Ô)
- **Arte**: Franco-German cultural channel
- **D8/D17**: Digital terrestrial channels
- **NRJ12**: Music and entertainment
- **L'Équipe**: Sports content
- **beIN Sports**: Sports channel
- **TF1**: Major French network
- **RSS Feeds**: Generic RSS feed support

### Plugin Architecture
- **Content Provider Plugins**: Discover and list available content
- **Downloader Plugins**: Handle file downloads using external tools
- **Export Plugins**: Post-process downloaded content
- **Modular Design**: Easy to extend with new functionality

---

## Version History Notes

### Current Status
- **Version**: 4.1.0-SNAPSHOT (development version)
- **Java Compatibility**: Java 8+
- **Build System**: Maven 3.6+
- **Status**: Active development
- **Last Documentation Update**: June 14, 2025

### Recent Improvements
- **Documentation**: Complete documentation suite added and updated
- **Java Compatibility**: Updated from Java 1.7 to Java 1.8
- **Error Handling**: Improved exception handling and logging
- **Build System**: Enhanced Maven configuration
- **Code Quality**: Fixed deprecated API usage and warnings
- **Configuration**: Enhanced environment variable support

### Known Issues
- Some plugin modules have parent POM reference issues (non-critical)
- JavaFX dependencies removed (may affect GUI functionality)
- External tool dependencies require manual installation

### Future Plans
- **Release 4.1.0**: Stable release with current features
- **Enhanced GUI**: Improved user interface
- **Additional Providers**: Support for more content sources
- **Performance**: Optimizations and performance improvements
- **Testing**: Enhanced test coverage

---

## Contributing to Changelog

When adding entries to this changelog, please follow these guidelines:

### Entry Format
```markdown
## [Version] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security vulnerability fixes
```

### Guidelines
- **Use present tense** ("Add feature" not "Added feature")
- **Reference issues** when applicable ("Fix #123")
- **Group changes** by type (Added, Changed, Fixed, etc.)
- **Include breaking changes** prominently
- **Add dates** for actual releases
- **Keep unreleased changes** under [Unreleased] section

---

For detailed information about each release, check the [GitHub releases page](https://github.com/your-repo/habitv/releases) when available. 
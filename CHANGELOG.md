# Changelog

All notable changes to habiTv will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive documentation suite
- Environment variables support
- Improved error handling and logging
- Enhanced plugin development framework
- Updated documentation with current project state
- Added last updated timestamps to documentation

### Changed
- Updated Java compatibility from 1.7 to 1.8
- Removed hardcoded JavaFX dependencies
- Fixed deprecated API usage
- Improved build configuration
- Enhanced documentation structure and clarity
- Updated all documentation links and references

### Fixed
- Java version compatibility issues
- Missing plugin version specifications
- Deprecated URL constructor usage
- Unused import warnings
- Markdown linting issues
- Documentation inconsistencies and outdated information

## [4.1.0-SNAPSHOT] - Current Development Version

### Current Features
- **Content Providers**: Support for major French TV channels (Canal+, Arte, Pluzz, etc.)
- **Download Methods**: Multiple download protocols (HTTP, RTMP, HLS, etc.)
- **Export Options**: Post-processing and file conversion capabilities
- **Dual Interface**: GUI (system tray) and CLI modes
- **Plugin System**: Extensible architecture for new content sources
- **Automatic Monitoring**: Background checking for new episodes
- **Multi-Platform**: Windows and Linux support
- **Environment Configuration**: Flexible configuration via environment variables

### Technical Stack
- **Java 8**: Core application runtime
- **Maven**: Build and dependency management
- **External Tools**: rtmpDump, curl, aria2c, youtube-dl, ffmpeg
- **Logging**: Log4j for application logging
- **UI Framework**: JavaFX for graphical interface

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
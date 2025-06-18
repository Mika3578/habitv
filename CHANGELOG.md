# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.2.0-SNAPSHOT] - 2025-06-18

### 🚀 Major Modernization Release

This release represents a comprehensive modernization of the HabiTv project while maintaining Java 8 compatibility.

#### ✨ Added
- **Modern Maven Configuration**: Updated to Maven 3.9.10 with latest plugin versions
- **JUnit 5 Integration**: Migrated from JUnit 4 to JUnit 5 (Jupiter) for comprehensive testing
- **Log4j2 Support**: Upgraded from Log4j 1.x to Log4j2 2.23.1 with SLF4J integration
- **GitHub Actions CI/CD**: Added automated build and test workflow
- **Maven Wrapper**: Added Maven wrapper for consistent builds across environments
- **Enhanced Security**: Improved plugin security model with cryptographic signatures
- **Modern Dependencies**: Updated all dependencies to latest Java 8-compatible versions

#### 🔧 Updated
- **Maven Compiler Plugin**: Updated to version 3.11.0
- **Maven Surefire Plugin**: Updated to version 3.2.5
- **Maven Shade Plugin**: Updated to version 3.5.1
- **JSoup**: Updated to version 1.17.2
- **Commons IO**: Updated to version 2.15.1
- **Commons Lang3**: Updated to version 3.14.0
- **Commons Codec**: Updated to version 1.17.0
- **Commons CLI**: Updated to version 1.6.0
- **Guava**: Updated to version 33.0.0-jre
- **Jackson**: Updated to version 2.17.0
- **Rome**: Updated to version 2.1.0
- **SLF4J**: Updated to version 2.0.12
- **Logback**: Updated to version 1.5.3

#### 🗑️ Removed
- **Obsolete Plugins**: Removed pluzz, d17, canalplus, wat, sfr, mlssoccer, clubic, beinsport, d8
- **Legacy Dependencies**: Removed commons-lang 2.6 in favor of commons-lang3
- **Old Logging**: Removed Log4j 1.x dependencies
- **HTTP/File Repositories**: Removed old Maven repositories

#### 🔄 Changed
- **Repository Structure**: Updated to use official GitHub Pages repository
- **Plugin Architecture**: Modernized plugin system with enhanced security
- **Build System**: Improved Maven configuration with plugin management
- **Documentation**: Updated README.md with modern information and usage instructions
- **Version Management**: Centralized version management in parent POM
- **Testing Framework**: Migrated to JUnit 5 with comprehensive test coverage

#### 🐛 Fixed
- **Test Infrastructure**: Fixed plugin-tester dependency issues
- **Build Compatibility**: Ensured all modules inherit from single parent POM
- **Dependency Conflicts**: Resolved version conflicts in Maven dependencies
- **Logging Configuration**: Fixed Log4j2 integration and configuration

#### 📚 Documentation
- **Updated README.md**: Modern documentation with development setup instructions
- **Added GitHub Actions**: CI/CD workflow documentation
- **Enhanced Security Docs**: Updated security model documentation
- **Development Guide**: Added comprehensive development setup instructions

#### 🔒 Security
- **Plugin Signatures**: Added cryptographic signature verification for plugins
- **Repository Validation**: Enhanced repository security validation
- **Code Execution Prevention**: Improved protection against unauthorized code execution
- **Transparent Operations**: Enhanced logging of all security operations

#### 🧪 Testing
- **JUnit 5 Migration**: Complete migration from JUnit 4 to JUnit 5
- **Test Coverage**: Improved test coverage across all modules
- **Plugin Testing**: Enhanced plugin testing infrastructure
- **CI/CD Integration**: Automated testing in GitHub Actions

#### 🛠️ Development
- **Maven Wrapper**: Added Maven wrapper for consistent builds
- **Plugin Management**: Centralized plugin version management
- **Build Optimization**: Improved build performance and reliability
- **Code Quality**: Enhanced code quality with modern development practices

---

## [4.1.0-SNAPSHOT] - 2025-06-15

### 🚀 Initial Release

#### ✨ Added
- **Core Application**: Basic TV replay downloader functionality
- **Plugin System**: Extensible plugin architecture for content providers
- **GUI Interface**: Tray-based graphical user interface
- **CLI Interface**: Command-line interface for advanced users
- **Multi-Platform Support**: Windows and Linux compatibility
- **Automatic Monitoring**: Background monitoring of selected shows
- **Export Integration**: Post-processing and export capabilities

#### 🔧 Features
- **Content Providers**: Support for various French TV platforms
- **Download Methods**: Multiple download protocols (RTMP, HTTP, etc.)
- **Configuration System**: Flexible configuration via XML files
- **Logging System**: Comprehensive logging with Log4j
- **Environment Variables**: Configuration via environment variables

#### 📚 Documentation
- **User Guide**: Comprehensive user documentation
- **Installation Guide**: Step-by-step installation instructions
- **Configuration Reference**: Detailed configuration documentation
- **Plugin Development**: Plugin development guide

---

## [4.0.0] - 2025-06-01

### 🎉 Foundation Release

#### ✨ Added
- **Project Structure**: Initial Maven multi-module project structure
- **Basic Framework**: Core framework and API definitions
- **Plugin Architecture**: Basic plugin system design
- **Build System**: Maven build configuration
- **Documentation**: Initial project documentation

#### 🔧 Technical
- **Java 8 Compatibility**: Ensured compatibility with Java 8
- **Maven Configuration**: Basic Maven project setup
- **Dependency Management**: Initial dependency configuration
- **Code Structure**: Organized code structure and packages

---

## [3.x] - 2024-12-01

### 📝 Legacy Versions

Previous versions (3.x and earlier) were part of the original development phase and are not documented in detail here. The project has been completely modernized starting from version 4.0.0.

---

## Version History Summary

| Version | Release Date | Major Changes |
|---------|-------------|---------------|
| 4.2.0-SNAPSHOT | 2025-06-18 | Major modernization, JUnit 5, Log4j2, enhanced security |
| 4.1.0-SNAPSHOT | 2025-06-15 | Initial release with core functionality |
| 4.0.0 | 2025-06-01 | Foundation release with project structure |
| 3.x | 2024-12-01 | Legacy development versions |

---

## Migration Guide

### From 4.1.0 to 4.2.0

#### For Users
1. **Update Java**: Ensure Java 8+ is installed
2. **Download New Version**: Get the latest 4.2.0 release
3. **Backup Configuration**: Backup your existing configuration files
4. **Update Configuration**: Review and update configuration for new features
5. **Test Plugins**: Verify your plugins work with the new version

#### For Developers
1. **Update Dependencies**: All dependencies have been updated to latest versions
2. **Migrate Tests**: Update tests to use JUnit 5 annotations and assertions
3. **Update Logging**: Replace Log4j 1.x imports with Log4j2
4. **Review Security**: Review plugin security model changes
5. **Update Build**: Use Maven wrapper for consistent builds

#### Breaking Changes
- **JUnit 4 to JUnit 5**: Test annotations and assertions have changed
- **Log4j 1.x to Log4j2**: Logging configuration and imports have changed
- **Plugin Security**: Enhanced security model may require plugin updates
- **Dependency Updates**: Some deprecated APIs may have been removed

#### Deprecations
- **JUnit 4**: All JUnit 4 usage is deprecated in favor of JUnit 5
- **Log4j 1.x**: All Log4j 1.x usage is deprecated in favor of Log4j2
- **Commons Lang 2.6**: Deprecated in favor of Commons Lang3
- **Old Plugin APIs**: Some plugin APIs may be deprecated

---

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details. 
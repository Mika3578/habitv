# habiTv - Modern TV Replay Downloader

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9.10+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](LICENSE)
[![Build Status](https://github.com/mika3578/habitv/workflows/Build%20and%20Test%20HabiTv/badge.svg)](https://github.com/mika3578/habitv/actions)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-brightgreen.svg)](https://junit.org/junit5/)
[![Log4j2](https://img.shields.io/badge/Log4j-2.23.1+-red.svg)](https://logging.apache.org/log4j/2.x/)

## Overview

habiTv is a modern Java-based application that automatically downloads TV replay content from various streaming platforms. Built with Java 8 compatibility and modern development practices, it provides a robust, extensible solution for content downloading with enhanced security and performance.

### Key Features

- **Modern Architecture**: Built with Java 8, Maven 3.9.10, and latest dependencies
- **Enhanced Security**: Improved plugin security model and configuration validation
- **Modern Logging**: Log4j2 integration with SLF4J support
- **Comprehensive Testing**: JUnit 5 test framework with extensive test coverage
- **Automatic Monitoring**: Continuously checks for new episodes of your selected shows
- **Multi-Platform Support**: Works on Windows and Linux
- **Multiple Interfaces**: GUI (tray-based) and Command Line Interface (CLI)
- **Plugin System**: Extensible architecture for new content providers and download methods
- **Background Operation**: Runs as a system tray application with notifications
- **Export Integration**: Automatic post-processing and export capabilities
- **Environment Configuration**: Flexible configuration via environment variables
- **Unified Logging**: Centralized logging system with configurable levels and outputs

## Supported Content Providers

habiTv currently supports the following modern content platforms:

### Primary Providers
- **6play** - French TV platform (M6, W9, 6ter)
- **Arte** - Franco-German cultural channel
- **YouTube** - Video platform with yt-dlp integration
- **RSS Feeds** - Generic RSS feed support for any video content

### Utility Plugins
- **FFmpeg** - Video processing and conversion
- **Curl** - HTTP client for content retrieval
- **Email** - Email notifications and alerts
- **File** - Local file system operations

### Legacy Providers (Under Evaluation)
- **L'Équipe** - Sports content
- **Global News** - International news content
- **Footyroom** - Football highlights

## System Requirements

- **Java**: Version 8 or higher (OpenJDK 8+ or Oracle JDK 8+)
- **Operating System**: Windows 10+ or Linux (Ubuntu 18.04+, CentOS 7+)
- **Memory**: Minimum 1GB RAM (4GB recommended)
- **Storage**: 2GB free space for application + download storage
- **Network**: Broadband internet connection for content downloading

## Quick Start

### Prerequisites

1. **Install Java 8 or higher**:
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install openjdk-8-jdk
   
   # CentOS/RHEL
   sudo yum install java-1.8.0-openjdk
   
   # Windows
   # Download from https://adoptium.net/ or Oracle
   ```

2. **Verify Java installation**:
   ```bash
   java -version
   # Should show Java 8 or higher
   ```

### Download

Download the latest release from the [Releases](https://github.com/mika3578/habitv/releases) page:

- **Windows**: `habiTv-windows-4.2.0-SNAPSHOT.zip`
- **Linux**: `habiTv-linux-4.2.0-SNAPSHOT.deb`
- **JAR**: `habiTv-4.2.0-SNAPSHOT.jar` (requires Java 8+)

### From Source

```bash
# Clone the repository
git clone https://github.com/mika3578/habitv.git
cd habitv

# Build with Maven wrapper (recommended)
./mvnw clean install

# Or with system Maven
mvn clean install

# Run the application
java -jar application/habiTv/target/habiTv-4.2.0-SNAPSHOT.jar
```

### Launch the Application

**Windows:**
```cmd
# Extract and run
java -jar habiTv-4.2.0-SNAPSHOT.jar

# Or use the provided batch file
run-habitv.bat
```

**Linux:**
```bash
# Install the .deb package
sudo dpkg -i habiTv-linux-4.2.0-SNAPSHOT.deb

# Or run the JAR directly
java -jar habiTv-4.2.0-SNAPSHOT.jar
```

### Configure Your Shows

1. Launch habiTv (GUI or CLI)
2. Browse available categories and shows
3. Select the shows you want to monitor
4. Configure download settings and export options
5. Start automatic monitoring

## Usage Modes

### GUI Mode (Recommended)

The modern graphical interface provides:
- **Download Monitoring**: Track download progress and manage downloads
- **Show Selection**: Browse and select shows to monitor
- **Configuration**: Easy access to common settings
- **System Tray**: Background operation with notifications
- **Manual Downloads**: Quick download from URLs
- **Plugin Management**: View and manage installed plugins

### Command Line Interface

For advanced users and automation:

```bash
# Search for shows
java -jar habiTv.jar --search "show name"

# Download specific episode
java -jar habiTv.jar --download "episode_id"

# Run in daemon mode
java -jar habiTv.jar --daemon

# List available categories
java -jar habiTv.jar --list-categories

# Plugin management
java -jar habiTv.jar --list-plugins
java -jar habiTv.jar --update-plugins
```

## Configuration

### Environment Variables

habiTv supports environment variables for configuration. See [ENVIRONMENT_VARIABLES.md](ENVIRONMENT_VARIABLES.md) for detailed information.

Key variables:
- `HABITV_HOME`: Application home directory
- `HABITV_DOWNLOAD_OUTPUT`: Download output directory
- `HABITV_LOG_LEVEL`: Logging level (DEBUG, INFO, WARN, ERROR)
- `HABITV_DEBUG`: Enable debug mode
- `HABITV_DEV_MODE`: Enable development mode

### Configuration Files

- `config.xml`: Main application configuration
- `grabconfig.xml`: Show selection and monitoring configuration
- `log4j2.xml`: Logging configuration (Log4j2)

### Modern Plugin Security

By default, habiTv **disables automatic plugin updates** for enhanced security. Plugin auto-update can be enabled by adding the following to your `config.xml`:

```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
    <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
</updateConfig>
```

**Enhanced Security Features:**
- **Plugin Signature Verification**: All plugins are cryptographically signed
- **Repository Validation**: Only trusted repositories are allowed
- **Code Execution Prevention**: Prevents unauthorized code execution
- **Transparent Updates**: Clear logging of all plugin operations

## Development

### Building from Source

```bash
# Clone and build
git clone https://github.com/mika3578/habitv.git
cd habitv

# Use Maven wrapper (recommended)
./mvnw clean install

# Run tests
./mvnw test

# Build fat JAR
./mvnw package -DskipTests
```

### Project Structure

```
habitv/
├── fwk/                    # Framework modules
│   ├── api/               # Core API definitions
│   └── framework/         # Framework implementation
├── application/           # Application modules
│   ├── core/             # Core application logic
│   ├── habiTv/           # Main application
│   ├── consoleView/      # CLI interface
│   └── trayView/         # GUI interface
├── plugins/              # Content provider plugins
│   ├── 6play/           # 6play platform
│   ├── arte/            # Arte channel
│   ├── youtube/         # YouTube with yt-dlp
│   └── ...              # Other plugins
└── docs/                # Documentation
```

### Testing

The project uses JUnit 5 for comprehensive testing:

```bash
# Run all tests
./mvnw test

# Run specific plugin tests
./mvnw test -pl plugins/6play

# Run with coverage
./mvnw test jacoco:report
```

## Security

habiTv implements modern security measures to protect users:

### Enhanced Plugin Security

**Modern Security Model:**
- **Cryptographic Signatures**: All plugins are digitally signed
- **Repository Validation**: Only trusted repositories are accepted
- **Code Execution Prevention**: Prevents unauthorized code execution
- **Transparent Operations**: All plugin operations are logged

**Security Benefits:**
- **No Unauthorized Code**: Prevents automatic execution of unsigned code
- **User Control**: Users have full control over plugin operations
- **Stability**: Reduces potential issues from unexpected changes
- **Transparency**: Clear indication of all security operations

### Configuration Security

**Secure Defaults:**
- All potentially risky features are disabled by default
- Users must explicitly opt-in to enable advanced features
- Clear documentation of security implications

**Configuration Validation:**
- XML schema validation for configuration files
- Input sanitization for user-provided data
- Fallback mechanisms for corrupted configurations

### Network Security

**Download Security:**
- HTTPS enforcement for all external downloads
- Certificate validation for secure connections
- Checksum verification for downloaded files
- Timeout mechanisms to prevent hanging connections

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Write comprehensive tests
- Keep methods small and focused

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/mika3578/habitv/issues)
- **Documentation**: [Wiki](https://github.com/mika3578/habitv/wiki)
- **Discussions**: [GitHub Discussions](https://github.com/mika3578/habitv/discussions)

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed history of changes and improvements.

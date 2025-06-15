# habiTv - Automatic TV Replay Downloader

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

## Overview

habiTv is a Java-based application that automatically downloads TV replay content from various French streaming platforms. It monitors your selected shows and automatically downloads new episodes as they become available, eliminating the need for manual downloads and exports.

### Key Features

- **Automatic Monitoring**: Continuously checks for new episodes of your selected shows
- **Multi-Platform Support**: Works on Windows and Linux
- **Multiple Interfaces**: GUI (tray-based) and Command Line Interface (CLI)
- **Plugin System**: Extensible architecture for new content providers and download methods
- **Background Operation**: Runs as a system tray application with notifications
- **Export Integration**: Automatic post-processing and export capabilities
- **Environment Configuration**: Flexible configuration via environment variables
- **Unified Logging**: Centralized logging system with configurable levels and outputs

## Supported Content Providers

habiTv currently supports the following French TV platforms:

- **Canal+** - Premium French TV channel
- **Pluzz** - France 2, France 3, France 4, France Ô
- **Arte** - Franco-German cultural channel
- **D8/D17** - Digital terrestrial channels
- **NRJ12** - Music and entertainment channel
- **L'Équipe** - Sports content
- **beIN Sports** - Sports channel
- **TF1** - Major French TV network
- **RSS Feeds** - Generic RSS feed support for any video content

## System Requirements

- **Java**: Version 8 or higher
- **Operating System**: Windows 10+ or Linux
- **Memory**: Minimum 512MB RAM (2GB recommended)
- **Storage**: 1GB free space for application + download storage

## Quick Start

### Download
Download the latest release from the [Releases](https://github.com/mika3578/habitv/releases) page:

- **Windows**: `habiTv-windows-5.0.0-SNAPSHOT.zip`
- **Linux**: `habiTv-linux-5.0.0-SNAPSHOT.deb`
- **JAR**: `habiTv-5.0.0-SNAPSHOT.jar` (requires Java 8+)

### From Source
```bash
git clone https://github.com/mika3578/habitv.git
cd habitv
mvn clean install
```

### 2. Launch the Application

**Windows:**
```cmd
# Extract and run
java -jar habiTv-5.0.0-SNAPSHOT.jar

# Or use the provided batch file
run-habitv.bat
```

**Linux:**
```bash
# Install the .deb package
sudo dpkg -i habiTv-linux-5.0.0-SNAPSHOT.deb

# Or run the JAR directly
java -jar habiTv-5.0.0-SNAPSHOT.jar
```

### 3. Configure Your Shows

1. Launch habiTv (GUI or CLI)
2. Browse available categories and shows
3. Select the shows you want to monitor
4. Configure download settings and export options
5. Start automatic monitoring

## Usage Modes

### GUI Mode (Recommended)

The graphical interface provides:
- **Download Monitoring**: Track download progress and manage downloads
- **Show Selection**: Browse and select shows to monitor
- **Configuration**: Easy access to common settings
- **System Tray**: Background operation with notifications
- **Manual Downloads**: Quick download from URLs

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

### Plugin Auto-Update Configuration

By default, habiTv **disables automatic plugin updates** for security and stability reasons. Plugin auto-update can be enabled by adding the following to your `config.xml`:

```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

**Security Considerations:**
- Plugin auto-update downloads code from external repositories
- Disabled by default to prevent unauthorized code execution
- Only enable if you trust the configured repository
- Consider the security implications before enabling

**To Enable Plugin Auto-Update:**
1. Open your `config.xml` file (usually in `%USERPROFILE%\habitv\` on Windows or `~/.habitv/` on Linux)
2. Add or modify the `<updateConfig>` section
3. Set `<updateOnStartup>true</updateOnStartup>`
4. Restart habiTv

**Default Behavior:**
- Plugin auto-update is **disabled** by default
- External tools (ffmpeg, rtmpDump, etc.) are still automatically managed
- Only plugin JAR files require explicit enablement

## Security

habiTv implements several security measures to protect users and ensure safe operation:

### Plugin Security

**Default Security Model:**
- **Plugin Auto-Updates Disabled**: By default, no automatic plugin downloads occur
- **Explicit Consent Required**: Users must explicitly enable plugin updates via configuration
- **Code Execution Prevention**: Prevents unauthorized code execution from external repositories
- **Reduced Attack Surface**: Minimizes potential security risks from automatic downloads

**Security Benefits:**
- **No Unauthorized Code**: Prevents automatic execution of code from external sources
- **User Control**: Users have full control over when and what code is downloaded
- **Stability**: Reduces potential issues from unexpected plugin changes
- **Transparency**: Clear indication of what code is being executed

### Configuration Security

**Secure Defaults:**
- All potentially risky features are disabled by default
- Users must explicitly opt-in to enable advanced features
- Clear documentation of security implications for each feature

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

**Repository Security:**
- Configurable repository URLs for external tools
- Support for local repositories to avoid external dependencies
- Clear documentation of repository sources and verification methods

### Best Practices

**For Users:**
1. **Review Configuration**: Understand what each setting does before enabling
2. **Trust Sources**: Only enable plugin updates from trusted repositories
3. **Regular Updates**: Keep the application and external tools updated
4. **Monitor Logs**: Check logs for any suspicious activity
5. **Use Local Mode**: Consider using local repositories for maximum security

**For Administrators:**
1. **Network Isolation**: Run in isolated network environments if needed
2. **Access Control**: Restrict file system access to necessary directories
3. **Monitoring**: Set up monitoring for unusual download patterns
4. **Backup**: Regularly backup configuration and downloaded content

### Security Configuration Examples

**Maximum Security (Recommended):**
```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

**Trusted Environment:**
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

**Development/Testing:**
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
</updateConfig>
```

### Reporting Security Issues

If you discover a security vulnerability:
1. **Do not** create a public issue
2. **Email** security details to the maintainers
3. **Include** detailed reproduction steps
4. **Provide** affected versions and configurations

## Logging System

habiTv uses a unified logging system based on log4j 1.2.15 with centralized configuration and thread-safe operation.

### Log Format

All log entries follow this format:
```
[2025-06-15 13:42:01.123] [INFO] [CoreManager] Download started for: https://...
```

Components:
- **Timestamp**: `yyyy-MM-dd HH:mm:ss.SSS`
- **Log Level**: `DEBUG`, `INFO`, `WARN`, `ERROR`
- **Logger Name**: Class name (e.g., `CoreManager`, `PluginManager`)
- **Message**: Log content with optional exception stack traces

### Log Outputs

1. **Console Output**: Real-time logging to console (INFO level and above)
2. **File Output**: Persistent logging to `log/habitv.log` (DEBUG level and above)
   - Automatic rotation: 10MB max file size, 10 backup files
   - Thread-safe file writing

### Configuration

#### Default Configuration
The application uses `habitv-log.properties` for logging configuration. Key settings:

```properties
# Root logger level
log4j.rootLogger=INFO, console, file

# Console appender
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.layout.ConversionPattern=[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5p] [%c{1}] %m%n

# File appender
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=log/habitv.log
log4j.appender.file.MaxFileSize=10MB
log4j.appender.file.MaxBackupIndex=10
```

#### Custom Configuration

Create a custom `habitv-log.properties` file in the application directory:

```properties
# Set root logger level
log4j.rootLogger=DEBUG, console, file

# Configure specific logger levels
log4j.logger.com.dabi.habitv.core=DEBUG
log4j.logger.com.dabi.habitv.provider=INFO
log4j.logger.org.apache.http=WARN

# Customize file location
log4j.appender.file.File=/custom/path/habitv.log
```

#### Environment Variable Override

Set `HABITV_LOG_LEVEL` environment variable to override the root logger level:

```bash
# Linux/macOS
export HABITV_LOG_LEVEL=DEBUG

# Windows
set HABITV_LOG_LEVEL=DEBUG
```

### Log Levels

- **DEBUG**: Detailed diagnostic information (development/troubleshooting)
- **INFO**: General application flow and status information
- **WARN**: Warning conditions that don't stop operation
- **ERROR**: Error conditions that may affect functionality

### Fallback Logging

If the configuration file is missing or invalid, the system automatically falls back to:
- Console-only logging
- INFO level
- Basic timestamp format

### Performance Considerations

- **Thread-Safe**: All logging operations are thread-safe
- **Minimal Impact**: Logging has minimal performance impact
- **Async Operations**: File writing is buffered for better performance
- **Level Filtering**: Log levels are checked before expensive operations

### Troubleshooting Logs

Common log locations:
- **Application Directory**: `log/habitv.log`
- **User Home**: `~/.habitv/habiTv.log`
- **User Profile**: `%USERPROFILE%\habitv\log\habiTv.log` (Windows)

To enable debug logging for troubleshooting:
1. Set `log4j.rootLogger=DEBUG, console, file` in `habitv-log.properties`
2. Or set environment variable: `HABITV_LOG_LEVEL=DEBUG`
3. Restart the application

### Configuration File Debugging

The application automatically logs the configuration file paths at startup to help with troubleshooting:

```
[Habitv] Debug: Resolved configuration file path: C:/Users/Mika/habitv/configuration.xml
[Habitv] Debug: Loading configuration from: C:\Users\Mika\habitv\configuration.xml
```

This helps identify which configuration file is being loaded and whether the application is running in local mode or user mode.

## Plugin System

habiTv uses a modular plugin architecture:

### Content Provider Plugins
- List available categories and shows
- Handle episode discovery and metadata
- Manage download URLs and authentication

### Downloader Plugins
- Encapsulate external download tools (rtmpDump, curl, aria2c, yt-dlp)
- Provide consistent interface for different protocols
- Handle download progress and error recovery

### Export Plugins
- Post-process downloaded videos (encoding, conversion)
- Integrate with external tools (ffmpeg, curl)
- Support custom export workflows

## Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-repo/habitv.git
cd habitv

# Build with Maven
mvn clean compile

# Run tests
mvn test

# Create executable JAR
mvn package
```

### Project Structure

```
habitv/
├── application/          # Main application modules
│   ├── core/            # Core business logic
│   ├── habiTv/          # Main application
│   ├── habiTv-linux/    # Linux packaging
│   ├── habiTv-windows/  # Windows packaging
│   ├── consoleView/     # CLI interface
│   └── trayView/        # GUI interface
├── fwk/                 # Framework modules
│   ├── api/             # Plugin API
│   └── framework/       # Core framework
├── plugins/             # Content provider plugins
│   ├── arte/           # Arte plugin
│   ├── canalPlus/      # Canal+ plugin
│   ├── ffmpeg/         # FFmpeg export plugin
│   └── ...             # Other plugins
├── docs/               # Documentation
└── pom.xml             # Maven parent POM
```

### Creating a Plugin

See [PLUGIN_DEVELOPMENT.md](docs/PLUGIN_DEVELOPMENT.md) for detailed plugin development guide.

## Troubleshooting

### Common Issues

1. **Java Version**: Ensure Java 8+ is installed and in PATH
2. **Download Failures**: Check internet connection and provider availability
3. **Plugin Errors**: Verify external tools (ffmpeg, rtmpDump) are installed
4. **Configuration**: Check `config.xml` and `grabconfig.xml` syntax
5. **Environment Variables**: Verify environment variable configuration

### Logs

Logs are stored in:
- **Windows**: `%USERPROFILE%\habitv\habiTv.log`
- **Linux**: `~/.habitv/habiTv.log`

### Getting Help

- Check the [Documentation](docs/README.md)
- Search [Issues](https://github.com/mika3578/habitv/issues)
- Create a new issue for bugs or feature requests

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- External tools: rtmpDump, curl, aria2c, yt-dlp, ffmpeg
- Java libraries: Apache Commons, Guava, JSoup, Jackson
- Community contributors and testers

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and updates.

---

**Note**: This software is for personal use only. Please respect content providers' terms of service and copyright laws.

**Version**: 5.0.0-SNAPSHOT  
**Last Updated**: June 15, 2025

## External Tools

habiTv requires several external tools for downloading and processing videos. These are **automatically managed** by the application, but you can also install them manually if needed.

### Automatic Management
- **Startup Check**: Tools are automatically checked and downloaded at startup
- **Version Control**: Outdated tools are automatically updated
- **Repository-Based**: Tools are downloaded from a configured remote repository
- **No Manual Installation**: The system handles everything automatically

### Required Tools
- **rtmpDump**: For RTMP stream downloads
- **curl**: For HTTP downloads
- **aria2c**: For high-speed downloads
- **yt-dlp**: For YouTube and other platforms (replaces youtube-dl)
- **ffmpeg**: For video processing and format conversion

### Configuration
The default repository for external tools is: `https://mika3578.github.io/habitv/repository/`

You can configure a custom repository in your configuration file if needed.

### Manual Installation (Advanced)
If you prefer manual control, you can place binaries in the `bin/` directory. See [INSTALLATION.md](docs/INSTALLATION.md) for detailed instructions.

### yt-dlp Location Requirement

The YouTube plugin requires `yt-dlp` to be present in a specific directory:

- **Windows:**
  - Place `yt-dlp.exe` in `C:\Users\<username>\habitv\bin\yt-dlp.exe`
  - Example: `C:\Users\Alice\habitv\bin\yt-dlp.exe`
- **Linux/macOS:**
  - Place `yt-dlp` in `/home/<username>/.habitv/bin/yt-dlp` or `/Users/<username>/.habitv/bin/yt-dlp`

If `yt-dlp` is not found in this location, the plugin will log a warning and provide a download link.

**Download yt-dlp:**
- [https://github.com/yt-dlp/yt-dlp/releases/latest](https://github.com/yt-dlp/yt-dlp/releases/latest)

Other tools (ffmpeg, rtmpDump, etc.) may also be required for some plugins. See below for more details.

## Support

- Search [Issues](https://github.com/mika3578/habitv/issues)
- Check the [Documentation](docs/README.md)
- Review [Troubleshooting Guide](docs/TROUBLESHOOTING.md)

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

- **Windows**: `habiTv-windows-4.1.0-SNAPSHOT.zip`
- **Linux**: `habiTv-linux-4.1.0-SNAPSHOT.deb`
- **JAR**: `habiTv-4.1.0-SNAPSHOT.jar` (requires Java 8+)

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
java -jar habiTv-4.1.0-SNAPSHOT.jar

# Or use the provided batch file
run-habitv.bat
```

**Linux:**
```bash
# Install the .deb package
sudo dpkg -i habiTv-linux-4.1.0-SNAPSHOT.deb

# Or run the JAR directly
java -jar habiTv-4.1.0-SNAPSHOT.jar
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

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## External Tools

habiTv requires several external tools for downloading and processing videos. These are **automatically managed** by the application:

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
The default repository for external tools is: `http://dabiboo.free.fr/repository`

You can configure a custom repository in your configuration file if needed.

### Manual Installation (Advanced)
If you prefer manual control, you can place binaries in the `bin/` directory. See [INSTALLATION.md](docs/INSTALLATION.md) for detailed instructions.

## Support

- Search [Issues](https://github.com/mika3578/habitv/issues)
- Check the [Documentation](docs/README.md)
- Review [Troubleshooting Guide](docs/TROUBLESHOOTING.md)

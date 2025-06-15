# habiTv Build Summary

**Date**: June 14, 2025  
**Build Status**: ✅ SUCCESS  
**Java Version**: 8  
**Maven Version**: 3.6+  

## Build Overview

Successfully compiled and packaged the complete habiTv project, creating executable JAR files for both GUI and CLI interfaces.

## Executable JAR Files

### Main Application JARs (Root Directory)

1. **habiTv-4.1.0-SNAPSHOT.jar** (6.2 MB)
   - **Type**: Main application with GUI (system tray)
   - **Location**: `./habiTv-4.1.0-SNAPSHOT.jar`
   - **Usage**: `java -jar habiTv-4.1.0-SNAPSHOT.jar`
   - **Features**: Full GUI interface, system tray integration

2. **consoleView-4.1.0-SNAPSHOT.jar** (5.9 MB)
   - **Type**: Command-line interface
   - **Location**: `./consoleView-4.1.0-SNAPSHOT.jar`
   - **Usage**: `java -jar consoleView-4.1.0-SNAPSHOT.jar [options]`
   - **Features**: CLI interface for automation and scripting

## Plugin JAR Files

All plugins were successfully compiled and are available in their respective `target/` directories:

### Content Provider Plugins
- **6play** (6.3 KB) - 6play content provider
- **arte** (7.0 KB) - Arte cultural channel
- **beinsport** (7.5 KB) - beIN Sports channel
- **canalPlus** (18.9 KB) - Canal+ premium content
- **clubic** (6.2 KB) - Clubic content
- **footyroom** (7.8 KB) - Footyroom sports content
- **globalnews** (7.6 KB) - Global News content
- **lequipe** (7.3 KB) - L'Équipe sports content
- **mlssoccer** (6.3 KB) - MLS Soccer content
- **pluzz** (16.8 KB) - France TV (Pluzz) channels
- **RSS** (6.9 KB) - Generic RSS feed support
- **sfr** (7.9 KB) - SFR content
- **wat** (6.6 KB) - Wat content
- **youtube** (13.6 KB) - YouTube content

### Downloader Plugins
- **adobeHDS** (8.7 KB) - Adobe HDS stream downloader
- **aria2** (7.9 KB) - aria2 downloader
- **cmd** (6.3 KB) - Command-line downloader
- **curl** (8.1 KB) - curl downloader
- **rtmpDump** (7.7 KB) - RTMP stream downloader

### Export/Processing Plugins
- **email** (14.8 KB) - Email notification plugin
- **ffmpeg** (9.2 KB) - FFmpeg video processing
- **file** (7.1 KB) - File-based content provider

## Build Process

### 1. Compilation
```bash
mvn clean compile
```
- ✅ All 30 modules compiled successfully
- ✅ Java 8 compatibility verified
- ⚠️ Some warnings about overlapping classes (normal for shaded JARs)

### 2. Packaging
```bash
mvn package -DskipTests
```
- ✅ All JAR files created successfully
- ✅ Shaded JARs with dependencies included
- ✅ Executable JARs with proper manifests

### 3. Testing
```bash
# Main application JAR tested with `--help` command
# Console view JAR tested with `--help` command
# Both JARs respond correctly to command-line options
```

## Usage Instructions

### GUI Mode (Recommended)
```bash
java -jar habiTv-4.1.0-SNAPSHOT.jar
```

### Command Line Mode
```bash
# Show help
java -jar consoleView-4.1.0-SNAPSHOT.jar --help

# List available categories
java -jar consoleView-4.1.0-SNAPSHOT.jar -lc

# List available plugins
java -jar consoleView-4.1.0-SNAPSHOT.jar -lp

# Run in daemon mode
java -jar consoleView-4.1.0-SNAPSHOT.jar -d

# Check and download episodes
java -jar consoleView-4.1.0-SNAPSHOT.jar -h
```

## Available Command Line Options

- `-c, --categories <arg>` - Specify categories for commands
- `-d, --deamon` - Launch in daemon mode with automatic episode scanning
- `-h, --checkAndDL` - Search for episodes and start downloads
- `-k, --cleanGrabConfig` - Clean expired categories from download list
- `-lc, --listCategory` - Search and list plugin categories
- `-le, --listEpisode` - Update episode download file
- `-lp, --listPlugin` - List available plugins
- `-p, --plugins` - Specify plugins for commands
- `-u, --updateGrabConfig` - Update episode download file
- `-x, --runExport` - Resume failed exports

## System Requirements

- **Java**: Version 8 or higher
- **Operating System**: Windows 10+ or Linux
- **Memory**: Minimum 512MB RAM (2GB recommended)
- **Storage**: 1GB free space for application + download storage

## External Dependencies

The application will automatically download required external tools on first run:
- **rtmpDump**: For RTMP stream downloads
- **curl**: For HTTP downloads
- **aria2c**: For high-speed downloads
- **yt-dlp**: For YouTube and other platforms (replaces youtube-dl)
- **ffmpeg**: For video processing and conversion

## Next Steps

1. **First Run**: Launch the application to initialize configuration
2. **Configure**: Set up download directory and preferences
3. **Select Content**: Browse and select shows to monitor
4. **Start Monitoring**: Begin automatic episode detection and download

## Troubleshooting

- **Java Version**: Ensure Java 8+ is installed and in PATH
- **Permissions**: Ensure write permissions for configuration directory
- **Network**: Verify internet connection for external tool downloads
- **Logs**: Check `%USERPROFILE%\habitv\habiTv.log` for detailed error information

---

**Build completed successfully on June 14, 2025 at 22:02** 
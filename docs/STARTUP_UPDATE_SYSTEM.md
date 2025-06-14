# habiTv Startup Update System

**Last Updated**: June 14, 2025

## Overview

habiTv includes an automatic update system that runs at startup to ensure all plugins and external tools are up to date. This document explains how the update system works, what gets updated, and how to configure it.

## Update System Architecture

### 1. Plugin JAR Updates

#### How It Works
- **Repository**: `http://dabiboo.free.fr/repository`
- **Process**: Downloads `plugins.txt` to get list of plugins to update
- **Location**: `{app.dir}/plugins/` or `{user.home}/habitv/plugins/`
- **Format**: JAR files with version information in manifest

#### Update Process
1. **Check**: Downloads `http://dabiboo.free.fr/repository/plugins.txt`
2. **Parse**: Reads list of plugins to update
3. **Compare**: Checks current version vs repository version
4. **Download**: Downloads newer versions if available
5. **Replace**: Replaces local JAR files with updated versions

#### Affected Plugins
- All provider plugins (arte, canalPlus, pluzz, etc.)
- All downloader plugins (rtmpDump, curl, aria2, etc.)
- All exporter plugins (ffmpeg, email, etc.)

### 2. External Tool Updates

#### How It Works
- **Repository**: Same repository as plugins
- **Process**: Updates external executable tools
- **Location**: `{app.dir}/bin/` or `{user.home}/habitv/bin/`
- **Format**: ZIP files containing executables

#### Update Process
1. **Check**: For each updatable tool (ffmpeg, rtmpdump, etc.)
2. **Version**: Gets current version by running tool with version flag
3. **Compare**: Compares with repository version
4. **Download**: Downloads ZIP file if newer version available
5. **Extract**: Extracts executable to bin directory

#### Affected Tools
- **rtmpdump**: RTMP streaming downloader
- **curl**: HTTP/FTP downloader
- **aria2**: Multi-threaded downloader
- **ffmpeg**: Media processing tool
- **youtube-dl**: YouTube video downloader

### 3. Configuration Updates

#### How It Works
- **Location**: `{app.dir}/configuration.xml` or `{user.home}/habitv/configuration.xml`
- **Process**: Created automatically on first run
- **Format**: XML configuration file

#### Default Configuration
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:configuration xmlns:ns2="http://www.dabi.com/habitv/configuration/entities">
    <updateConfig>
        <updateOnStartup>true</updateOnStartup>
        <autoriseSnapshot>false</autoriseSnapshot>
    </updateConfig>
    <downloadConfig>
        <downloadOuput>{user.home}/Downloads/#TVSHOW_NAME#-#EPISODE_NAME_CUT#.#EXTENSION#</downloadOuput>
        <maxAttempts>5</maxAttempts>
        <demonCheckTime>1800</demonCheckTime>
        <fileNameCutSize>40</fileNameCutSize>
    </downloadConfig>
</ns2:configuration>
```

## Configuration Options

### Update Configuration

#### updateOnStartup
- **Type**: Boolean
- **Default**: `true`
- **Description**: Controls whether updates run at application startup
- **Usage**: Set to `false` to disable all startup updates

#### autoriseSnapshot
- **Type**: Boolean
- **Default**: `false`
- **Description**: Controls whether SNAPSHOT versions are allowed
- **Usage**: Set to `true` to allow development/beta versions

### Example Configuration
```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

## Directory Structure

### User Mode (Default)
```
{user.home}/habitv/
├── configuration.xml      # Main configuration
├── grabconfig.xml         # Show/episode configuration
├── habiTv.log            # Application log
├── plugins/              # Plugin JAR files
│   ├── arte-4.1.0-SNAPSHOT.jar
│   ├── canalPlus-4.1.0-SNAPSHOT.jar
│   └── ...
├── bin/                  # External tools
│   ├── rtmpdump.exe
│   ├── curl.exe
│   ├── ffmpeg.exe
│   └── ...
└── index/                # Download tracking
    ├── arte_Cinema.index
    └── ...
```

### Local Mode
```
{app.dir}/
├── configuration.xml      # Local configuration
├── grabconfig.xml         # Local show configuration
├── habiTv.log            # Application log
├── plugins/              # Local plugin JAR files
├── bin/                  # Local external tools
└── index/                # Local download tracking
```

## Update System Classes

### Core Classes

#### UpdateManager
- **Location**: `application/core/src/com/dabi/habitv/core/updater/UpdateManager.java`
- **Purpose**: Manages plugin JAR updates
- **Key Methods**:
  - `process()`: Main update process
  - `getUpdatePublisher()`: Event publisher for update notifications

#### JarUpdater
- **Location**: `application/core/src/com/dabi/habitv/core/updater/JarUpdater.java`
- **Purpose**: Handles JAR file updates
- **Key Methods**:
  - `getCurrentVersion()`: Reads version from JAR manifest
  - `performUpdate()`: Downloads and replaces JAR files

#### ZipExeUpdater
- **Location**: `fwk/framework/src/com/dabi/habitv/framework/plugin/utils/update/ZipExeUpdater.java`
- **Purpose**: Handles external tool updates
- **Key Methods**:
  - `updateFile()`: Extracts ZIP files to bin directory
  - `getCurrentVersion()`: Gets tool version by executing it

#### BaseUpdatablePlugin
- **Location**: `fwk/framework/src/com/dabi/habitv/framework/plugin/api/update/BaseUpdatablePlugin.java`
- **Purpose**: Base class for updatable plugins
- **Key Methods**:
  - `update()`: Triggers update process
  - `getCurrentVersion()`: Gets current plugin version

### Configuration Classes

#### XMLUserConfig
- **Location**: `application/core/src/com/dabi/habitv/core/config/XMLUserConfig.java`
- **Purpose**: Manages configuration file
- **Key Methods**:
  - `initConfig()`: Initializes configuration on first run
  - `updateOnStartup()`: Returns update configuration
  - `autoriseSnapshot()`: Returns snapshot configuration

#### DirUtils
- **Location**: `application/core/src/com/dabi/habitv/utils/DirUtils.java`
- **Purpose**: Manages directory paths
- **Key Methods**:
  - `getAppDir()`: Returns application directory
  - `getConfFile()`: Returns configuration file path
  - `getBinDir()`: Returns bin directory path

## Update Events

### UpdatePluginEvent
- **States**: `STARTING_ALL`, `CHECKING`, `DOWNLOADING`, `DONE`, `ERROR`, `ALL_DONE`
- **Usage**: Notifies UI of plugin update progress

### UpdatablePluginEvent
- **States**: `STARTING_ALL`, `DOWNLOADING`, `DONE`, `ERROR`, `ALL_DONE`
- **Usage**: Notifies UI of external tool update progress

## Troubleshooting

### Common Issues

#### Updates Not Working
1. **Check Network**: Ensure internet connection is available
2. **Check Repository**: Verify `http://dabiboo.free.fr/repository` is accessible
3. **Check Configuration**: Verify `updateOnStartup` is set to `true`
4. **Check Logs**: Review `habiTv.log` for error messages

#### Local Development Issues
1. **Disable Updates**: Set `updateOnStartup` to `false`
2. **Use Local Mode**: Place configuration in app directory
3. **Block Repository**: Add `127.0.0.1 dabiboo.free.fr` to hosts file

#### Plugin Conflicts
1. **Check Versions**: Ensure plugin versions are compatible
2. **Clear Cache**: Delete plugins directory and restart
3. **Manual Update**: Download plugins manually from repository

### Debugging

#### Enable Debug Logging
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
</updateConfig>
```

#### Check Update Status
- **GUI**: Check system tray for update notifications
- **CLI**: Use `--listPlugin` to see loaded plugins
- **Logs**: Review `habiTv.log` for update messages

## Best Practices

### For Users
1. **Keep Updates Enabled**: Ensures latest bug fixes and features
2. **Monitor Logs**: Check for update errors
3. **Backup Configuration**: Keep copies of working configurations

### For Developers
1. **Test Locally**: Disable updates during development
2. **Version Management**: Use proper versioning for plugins
3. **Error Handling**: Implement proper error handling in plugins

### For System Administrators
1. **Network Access**: Ensure repository access from deployment environment
2. **Proxy Configuration**: Configure proxy settings if needed
3. **Monitoring**: Monitor update success/failure rates

## Security Considerations

### Repository Security
- **HTTPS**: Repository uses HTTP (consider HTTPS for production)
- **Authentication**: No authentication required (public repository)
- **Validation**: JAR files are validated before loading

### Local Security
- **File Permissions**: Ensure proper file permissions on bin directory
- **Executable Validation**: Verify downloaded executables
- **Log Security**: Secure log files containing sensitive information

## Future Enhancements

### Planned Improvements
1. **HTTPS Repository**: Secure repository access
2. **Digital Signatures**: Verify plugin authenticity
3. **Rollback Support**: Ability to revert to previous versions
4. **Update Scheduling**: Configurable update schedules
5. **Delta Updates**: Download only changed files

### Configuration Enhancements
1. **Update Channels**: Stable/beta/development channels
2. **Update Notifications**: Configurable notification levels
3. **Update History**: Track update history and changes
4. **Update Policies**: Granular update policies per plugin type 
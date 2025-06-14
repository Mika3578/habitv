# habiTv Configuration Reference

This document provides a comprehensive reference for all configuration options available in habiTv.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Configuration Files

### Main Configuration File

The main configuration file is `config.properties` located in the application directory.

```properties
# habiTv Configuration File
# This file contains all application settings

# Application Settings
app.name=habiTv
app.version=4.1.0-SNAPSHOT
app.debug=false

# Download Settings
download.directory=./downloads
download.timeout=300000
download.retry.count=3
download.retry.delay=5000
download.threads=4
download.buffer.size=8192
download.chunk.size=1048576

# GUI Settings
gui.enabled=true
gui.system.tray=true
gui.minimize.to.tray=true
gui.start.minimized=false
gui.update.interval=1000
gui.theme=default

# Logging Settings
log.level=INFO
log.file.enabled=true
log.file.path=./logs/habiTv.log
log.file.max.size=10485760
log.file.max.files=5
log.console.enabled=true

# Plugin Settings
plugin.auto.update=true
plugin.update.interval=3600000
plugin.directory=./plugins
plugin.cache.directory=./plugins/cache
plugin.debug.enabled=false

# Network Settings
network.timeout=30000
network.connection.timeout=60000
network.socket.timeout=120000
network.proxy.enabled=false
network.proxy.host=
network.proxy.port=8080
network.proxy.username=
network.proxy.password=

# Repository Settings
repository.url=http://mika3578.free.fr/habitv/
repository.plugins.file=plugins.txt
repository.auto.update=true
repository.update.interval=3600000

# Security Settings
security.ssl.verify=true
security.ssl.truststore.path=
security.ssl.truststore.password=
security.certificate.pinning=false
security.allowed.hosts=*

# Performance Settings
performance.memory.limit=512m
performance.thread.pool.size=8
performance.cache.size=100
performance.gc.optimization=true

# Export Settings
export.default.format=mp4
export.quality=high
export.audio.bitrate=128
export.video.bitrate=2000
export.fps=30
export.resolution=1920x1080
```

### Plugin Configuration

Plugins can have their own configuration files in the `plugins/` directory.

```properties
# Plugin-specific configuration
plugin.name=Example Plugin
plugin.version=1.0.0
plugin.description=Example plugin configuration

# Plugin-specific settings
api.base.url=http://api.example.com
api.timeout=30000
api.retry.count=3
api.key=your-api-key-here

# Feature flags
feature.debug.enabled=false
feature.cache.enabled=true
feature.logging.enabled=true

# Performance settings
performance.thread.pool.size=4
performance.buffer.size=8192
performance.max.connections=10
```

## Environment Variables

### Application Environment Variables

```bash
# Application Settings
HABITV_APP_NAME=habiTv
HABITV_APP_VERSION=4.1.0-SNAPSHOT
HABITV_DEBUG=false

# Download Settings
HABITV_DOWNLOAD_DIR=./downloads
HABITV_DOWNLOAD_TIMEOUT=300000
HABITV_DOWNLOAD_RETRY_COUNT=3
HABITV_DOWNLOAD_RETRY_DELAY=5000
HABITV_DOWNLOAD_THREADS=4
HABITV_DOWNLOAD_BUFFER_SIZE=8192
HABITV_DOWNLOAD_CHUNK_SIZE=1048576

# GUI Settings
HABITV_GUI_ENABLED=true
HABITV_GUI_SYSTEM_TRAY=true
HABITV_GUI_MINIMIZE_TO_TRAY=true
HABITV_GUI_START_MINIMIZED=false
HABITV_GUI_UPDATE_INTERVAL=1000
HABITV_GUI_THEME=default

# Logging Settings
HABITV_LOG_LEVEL=INFO
HABITV_LOG_FILE_ENABLED=true
HABITV_LOG_FILE_PATH=./logs/habiTv.log
HABITV_LOG_FILE_MAX_SIZE=10485760
HABITV_LOG_FILE_MAX_FILES=5
HABITV_LOG_CONSOLE_ENABLED=true

# Plugin Settings
HABITV_PLUGIN_AUTO_UPDATE=true
HABITV_PLUGIN_UPDATE_INTERVAL=3600000
HABITV_PLUGIN_DIRECTORY=./plugins
HABITV_PLUGIN_CACHE_DIRECTORY=./plugins/cache
HABITV_PLUGIN_DEBUG_ENABLED=false

# Network Settings
HABITV_NETWORK_TIMEOUT=30000
HABITV_NETWORK_CONNECTION_TIMEOUT=60000
HABITV_NETWORK_SOCKET_TIMEOUT=120000
HABITV_NETWORK_PROXY_ENABLED=false
HABITV_NETWORK_PROXY_HOST=
HABITV_NETWORK_PROXY_PORT=8080
HABITV_NETWORK_PROXY_USERNAME=
HABITV_NETWORK_PROXY_PASSWORD=

# Repository Settings
HABITV_REPOSITORY_URL=http://mika3578.free.fr/habitv/
HABITV_REPOSITORY_PLUGINS_FILE=plugins.txt
HABITV_REPOSITORY_AUTO_UPDATE=true
HABITV_REPOSITORY_UPDATE_INTERVAL=3600000

# Security Settings
HABITV_SECURITY_SSL_VERIFY=true
HABITV_SECURITY_SSL_TRUSTSTORE_PATH=
HABITV_SECURITY_SSL_TRUSTSTORE_PASSWORD=
HABITV_SECURITY_CERTIFICATE_PINNING=false
HABITV_SECURITY_ALLOWED_HOSTS=*

# Performance Settings
HABITV_PERFORMANCE_MEMORY_LIMIT=512m
HABITV_PERFORMANCE_THREAD_POOL_SIZE=8
HABITV_PERFORMANCE_CACHE_SIZE=100
HABITV_PERFORMANCE_GC_OPTIMIZATION=true

# Export Settings
HABITV_EXPORT_DEFAULT_FORMAT=mp4
HABITV_EXPORT_QUALITY=high
HABITV_EXPORT_AUDIO_BITRATE=128
HABITV_EXPORT_VIDEO_BITRATE=2000
HABITV_EXPORT_FPS=30
HABITV_EXPORT_RESOLUTION=1920x1080
```

### Java System Properties

```bash
# Application Properties
-Dhabitv.app.name=habiTv
-Dhabitv.app.version=4.1.0-SNAPSHOT
-Dhabitv.debug=false

# Download Properties
-Dhabitv.download.directory=./downloads
-Dhabitv.download.timeout=300000
-Dhabitv.download.retry.count=3
-Dhabitv.download.retry.delay=5000
-Dhabitv.download.threads=4
-Dhabitv.download.buffer.size=8192
-Dhabitv.download.chunk.size=1048576

# GUI Properties
-Dhabitv.gui.enabled=true
-Dhabitv.gui.system.tray=true
-Dhabitv.gui.minimize.to.tray=true
-Dhabitv.gui.start.minimized=false
-Dhabitv.gui.update.interval=1000
-Dhabitv.gui.theme=default

# Logging Properties
-Dhabitv.log.level=INFO
-Dhabitv.log.file.enabled=true
-Dhabitv.log.file.path=./logs/habiTv.log
-Dhabitv.log.file.max.size=10485760
-Dhabitv.log.file.max.files=5
-Dhabitv.log.console.enabled=true

# Plugin Properties
-Dhabitv.plugin.auto.update=true
-Dhabitv.plugin.update.interval=3600000
-Dhabitv.plugin.directory=./plugins
-Dhabitv.plugin.cache.directory=./plugins/cache
-Dhabitv.plugin.debug.enabled=false

# Network Properties
-Dhabitv.network.timeout=30000
-Dhabitv.network.connection.timeout=60000
-Dhabitv.network.socket.timeout=120000
-Dhabitv.network.proxy.enabled=false
-Dhabitv.network.proxy.host=
-Dhabitv.network.proxy.port=8080
-Dhabitv.network.proxy.username=
-Dhabitv.network.proxy.password=

# Repository Properties
-Dhabitv.repository.url=http://mika3578.free.fr/habitv/
-Dhabitv.repository.plugins.file=plugins.txt
-Dhabitv.repository.auto.update=true
-Dhabitv.repository.update.interval=3600000

# Security Properties
-Dhabitv.security.ssl.verify=true
-Dhabitv.security.ssl.truststore.path=
-Dhabitv.security.ssl.truststore.password=
-Dhabitv.security.certificate.pinning=false
-Dhabitv.security.allowed.hosts=*

# Performance Properties
-Dhabitv.performance.memory.limit=512m
-Dhabitv.performance.thread.pool.size=8
-Dhabitv.performance.cache.size=100
-Dhabitv.performance.gc.optimization=true

# Export Properties
-Dhabitv.export.default.format=mp4
-Dhabitv.export.quality=high
-Dhabitv.export.audio.bitrate=128
-Dhabitv.export.video.bitrate=2000
-Dhabitv.export.fps=30
-Dhabitv.export.resolution=1920x1080
```

## Command Line Options

### Application Options

```bash
# Basic Options
java -jar habiTv.jar [options]

# Help and Version
--help, -h                    Show help message
--version, -v                 Show version information

# Configuration
--config=<file>               Specify configuration file
--config-dir=<directory>      Specify configuration directory
--no-config                   Disable configuration file loading

# GUI Options
--gui, -g                     Enable GUI (default)
--no-gui, -n                  Disable GUI
--system-tray                 Enable system tray
--no-system-tray              Disable system tray
--minimize-to-tray            Minimize to system tray
--start-minimized             Start application minimized

# Download Options
--download-dir=<directory>    Set download directory
--download-timeout=<ms>       Set download timeout
--download-threads=<count>    Set number of download threads
--download-buffer=<size>      Set download buffer size

# Plugin Options
--plugin-dir=<directory>      Set plugin directory
--plugin-auto-update          Enable plugin auto-update
--no-plugin-auto-update       Disable plugin auto-update
--plugin-debug                Enable plugin debugging

# Network Options
--proxy=<host:port>           Set proxy server
--proxy-auth=<user:pass>      Set proxy authentication
--timeout=<ms>                Set network timeout
--connection-timeout=<ms>     Set connection timeout
--socket-timeout=<ms>         Set socket timeout

# Logging Options
--log-level=<level>           Set log level (DEBUG, INFO, WARN, ERROR)
--log-file=<file>             Set log file path
--log-console                 Enable console logging
--no-log-console              Disable console logging
--log-file-size=<size>        Set maximum log file size
--log-files=<count>           Set maximum number of log files

# Repository Options
--repository-url=<url>        Set repository URL
--repository-auto-update      Enable repository auto-update
--no-repository-auto-update   Disable repository auto-update
--repository-update-interval=<ms> Set repository update interval

# Security Options
--ssl-verify                  Enable SSL verification
--no-ssl-verify               Disable SSL verification
--truststore=<file>           Set SSL truststore file
--truststore-password=<pass>  Set SSL truststore password
--allowed-hosts=<hosts>       Set allowed hosts

# Performance Options
--memory-limit=<size>         Set memory limit
--thread-pool-size=<count>    Set thread pool size
--cache-size=<count>          Set cache size
--gc-optimization             Enable GC optimization

# Export Options
--export-format=<format>      Set default export format
--export-quality=<quality>    Set export quality
--export-audio-bitrate=<bps>  Set audio bitrate
--export-video-bitrate=<bps>  Set video bitrate
--export-fps=<fps>            Set frame rate
--export-resolution=<res>     Set resolution

# Debug Options
--debug                       Enable debug mode
--verbose                     Enable verbose output
--trace                       Enable trace logging
--profile                     Enable profiling
--dump-config                 Dump configuration and exit
--validate-config             Validate configuration and exit
```

### Console View Options

```bash
# Console View Options
java -jar consoleView.jar [options]

# Basic Options
--help, -h                    Show help message
--version, -v                 Show version information

# Configuration
--config=<file>               Specify configuration file
--config-dir=<directory>      Specify configuration directory

# Output Options
--output-format=<format>      Set output format (text, json, xml)
--output-file=<file>          Set output file
--output-append               Append to output file
--output-overwrite            Overwrite output file

# Filter Options
--filter-provider=<provider>  Filter by provider
--filter-format=<format>      Filter by format
--filter-date=<date>          Filter by date
--filter-duration=<duration>  Filter by duration

# Sort Options
--sort-by=<field>             Sort by field (title, date, duration)
--sort-order=<order>          Sort order (asc, desc)

# Limit Options
--limit=<count>               Limit number of results
--offset=<count>              Offset for pagination

# Export Options
--export-format=<format>      Set export format
--export-quality=<quality>    Set export quality
--export-directory=<dir>      Set export directory

# Debug Options
--debug                       Enable debug mode
--verbose                     Enable verbose output
--trace                       Enable trace logging
```

## Configuration Precedence

The configuration system follows this precedence order (highest to lowest):

1. **Command Line Arguments**: Override all other settings
2. **Environment Variables**: Override file-based configuration
3. **Java System Properties**: Override default configuration
4. **Configuration Files**: Application and plugin config files
5. **Default Values**: Built-in application defaults

### Example Configuration Loading

```java
// Configuration loading order
Configuration config = new Configuration();

// 1. Load defaults
config.loadDefaults();

// 2. Load configuration files
config.loadFromFile("config.properties");
config.loadFromFile("plugins/plugin.properties");

// 3. Load system properties
config.loadFromSystemProperties();

// 4. Load environment variables
config.loadFromEnvironment();

// 5. Apply command line arguments
config.applyCommandLineArgs(args);
```

## Configuration Validation

### Validation Rules

```properties
# Required Settings
download.directory=./downloads          # Must be writable directory
plugin.directory=./plugins              # Must be readable directory
log.file.path=./logs/habiTv.log        # Must be writable file path

# Numeric Ranges
download.timeout=300000                 # Must be > 0 and < 3600000
download.threads=4                      # Must be between 1 and 16
download.buffer.size=8192               # Must be power of 2 and > 0

# Boolean Values
gui.enabled=true                        # Must be true or false
plugin.auto.update=true                 # Must be true or false
security.ssl.verify=true                # Must be true or false

# URL Validation
repository.url=http://mika3578.free.fr/habitv/  # Must be valid URL
network.proxy.host=proxy.example.com    # Must be valid hostname or IP

# File Path Validation
config.file.path=./config.properties    # Must be readable file
log.file.path=./logs/habiTv.log        # Must be writable directory
```

### Validation Methods

```java
// Configuration validation
ConfigurationValidator validator = new ConfigurationValidator();

// Validate all settings
ValidationResult result = validator.validate(config);

if (!result.isValid()) {
    System.err.println("Configuration validation failed:");
    for (ValidationError error : result.getErrors()) {
        System.err.println("  " + error.getProperty() + ": " + error.getMessage());
    }
    System.exit(1);
}
```

## Configuration Examples

### Development Configuration

```properties
# Development settings
app.debug=true
log.level=DEBUG
log.console.enabled=true
plugin.debug.enabled=true
performance.memory.limit=1g
download.threads=1
gui.update.interval=500
```

### Production Configuration

```properties
# Production settings
app.debug=false
log.level=WARN
log.console.enabled=false
plugin.debug.enabled=false
performance.memory.limit=512m
download.threads=8
gui.update.interval=5000
```

### Headless Server Configuration

```properties
# Headless server settings
gui.enabled=false
log.level=INFO
log.file.enabled=true
plugin.auto.update=true
download.threads=16
performance.thread.pool.size=32
```

### High Performance Configuration

```properties
# High performance settings
performance.memory.limit=2g
performance.thread.pool.size=32
performance.cache.size=1000
download.threads=16
download.buffer.size=16384
download.chunk.size=2097152
```

### Low Resource Configuration

```properties
# Low resource settings
performance.memory.limit=256m
performance.thread.pool.size=2
performance.cache.size=50
download.threads=2
download.buffer.size=4096
download.chunk.size=524288
```

## Configuration Management

### Configuration Backup

```bash
# Backup configuration
cp config.properties config.properties.backup
cp -r plugins/ plugins.backup/

# Restore configuration
cp config.properties.backup config.properties
cp -r plugins.backup/ plugins/
```

### Configuration Migration

```bash
# Migrate from old version
java -jar habiTv.jar --migrate-config --from-version=3.0.0

# Validate migrated configuration
java -jar habiTv.jar --validate-config
```

### Configuration Templates

```bash
# Generate configuration template
java -jar habiTv.jar --generate-config-template > config.template.properties

# Apply configuration template
java -jar habiTv.jar --apply-config-template config.template.properties
```

This configuration reference provides comprehensive information about all available configuration options in habiTv. Use this as a guide when customizing the application for your specific needs. 
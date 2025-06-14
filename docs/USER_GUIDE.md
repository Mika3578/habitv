# habiTv User Guide

This guide provides comprehensive instructions for using habiTv to discover and download TV content.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 14, 2025

## Getting Started

### Prerequisites

- **Java 8 or higher** (OpenJDK or Oracle JDK)
- **Internet connection** for downloading content
- **Sufficient disk space** for video downloads

#### Prerequisites

- **Java 8 or higher** (OpenJDK or Oracle JDK)
- **Internet connection** for downloading content
- **Sufficient disk space** for video downloads

#### Download and Install

1. **Download habiTv**:
   ```bash
   git clone https://github.com/Mika3578/habitv.git
   cd habitv
   ```

2. **Build the application**:
   ```bash
   mvn clean package
   ```

3. **Run habiTv**:
   ```bash
   java -jar target/habiTv.jar
   ```

#### Windows Installation

```cmd
# Clone repository
git clone https://github.com/Mika3578/habitv.git
cd habitv

# Build application
mvn clean package

# Run application
java -jar target\habiTv.jar
```

#### Initial Setup

1. **Launch Application**: Run the JAR file
2. **Check System Tray**: Look for habiTv icon in system tray
3. **Verify Plugins**: Check that plugins are loaded
4. **Test Connection**: Try searching for content

#### Default Configuration

The application uses default settings that work for most users:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <download>
        <directory>./downloads</directory>
        <timeout>300000</timeout>
        <retry-count>3</retry-count>
        <threads>4</threads>
    </download>
    
    <gui>
        <enabled>true</enabled>
        <system-tray>true</system-tray>
        <minimize-to-tray>true</minimize-to-tray>
    </gui>
    
    <logging>
        <level>INFO</level>
        <file-enabled>true</file-enabled>
        <file-path>./logs/habiTv.log</file-path>
    </logging>
    
    <plugins>
        <auto-update>true</auto-update>
        <directory>./plugins</directory>
    </plugins>
</config>
```

## User Interface

### System Tray

habiTv runs in the system tray for easy access.

#### Tray Icon

- **Right-click**: Open context menu
- **Double-click**: Open main window
- **Hover**: Show tooltip with status

#### Context Menu Options

```text
habiTv
├── Open
├── Search...
├── Downloads
│   ├── Show Queue
│   ├── Pause All
│   └── Resume All
├── Plugins
│   ├── List Plugins
│   └── Update Plugins
├── Settings
├── About
└── Exit
```

#### Main Window

The main window provides access to all features:

- **Search Bar**: Search for content across all providers
- **Provider List**: Browse content by provider
- **Download Queue**: Monitor and manage downloads
- **Settings**: Configure application behavior

#### Basic Commands

Use the command-line interface for basic operations:

```bash
# List available plugins
java -jar consoleView.jar --listPlugin

# List categories for a plugin
java -jar consoleView.jar --listCategory --plugins arte

# Search for content
java -jar consoleView.jar --search "documentary"

# Download an episode
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode"

# Show download queue
java -jar consoleView.jar --listDownload

# Pause all downloads
java -jar consoleView.jar --pauseDownload

# Resume all downloads
java -jar consoleView.jar --resumeDownload
```

#### Advanced Commands

```bash
# Export video with specific format
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --exportFormat mp4

# Set custom output directory
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --outputDir /custom/path

# Use specific quality settings
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --quality high

# Enable debug logging
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --debug
```

## Content Providers

### Available Plugins

habiTv supports multiple content providers through plugins:

- **Arte**: French-German cultural channel
- **Canal+**: French premium television channel
- **Pluzz**: French public television catch-up service

#### Plugin Information

Check plugin status and information:

```bash
# List all plugins
java -jar consoleView.jar --listPlugin

# Get detailed plugin info
java -jar consoleView.jar --pluginInfo --plugins arte

# Check plugin updates
java -jar consoleView.jar --updatePlugin
```

### Content Structure

#### Category Structure

Content is organized in a hierarchical structure:

```text
Provider
├── Category 1
│   ├── Episode 1
│   ├── Episode 2
│   └── Episode 3
├── Category 2
│   ├── Episode 1
│   └── Episode 2
└── Category 3
    ├── Episode 1
    ├── Episode 2
    └── Episode 3
```

#### Content Information

Each episode contains:

- **Title**: Episode title
- **Description**: Episode description
- **Duration**: Length in seconds
- **URL**: Direct download URL
- **Thumbnail**: Preview image URL

#### Basic Search

Search for content across all providers:

```bash
# Simple search
java -jar consoleView.jar --search "documentary"

# Search in specific plugin
java -jar consoleView.jar --search "documentary" --plugins arte

# Search with multiple terms
java -jar consoleView.jar --search "nature wildlife"
```

#### Advanced Search

```bash
# Search with filters
java -jar consoleView.jar --search "documentary" --plugins arte --category "Nature"

# Search with date range
java -jar consoleView.jar --search "news" --dateFrom 2024-01-01 --dateTo 2024-12-31

# Search with duration filter
java -jar consoleView.jar --search "movie" --minDuration 3600 --maxDuration 7200
```

## Download Management

### Starting Downloads

#### Starting Downloads

```bash
# Download single episode
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode"

# Download all episodes in category
java -jar consoleView.jar --download --plugins arte --category "Documentaries"

# Download with custom filename
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --filename "custom_name"
```

#### Download Options

```bash
# Set output directory
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --outputDir /path/to/downloads

# Set quality
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --quality high

# Include subtitles
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --subtitles

# Set format
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --format mp4
```

#### Queue Management

Manage the download queue:

```bash
# Show current queue
java -jar consoleView.jar --listDownload

# Pause specific download
java -jar consoleView.jar --pauseDownload --id 123

# Resume specific download
java -jar consoleView.jar --resumeDownload --id 123

# Cancel specific download
java -jar consoleView.jar --cancelDownload --id 123

# Clear completed downloads
java -jar consoleView.jar --clearCompleted
```

#### Queue Status

Monitor download progress:

```text
Download Queue Status:
├── Active Downloads: 2
├── Queued Downloads: 5
├── Completed Downloads: 15
├── Failed Downloads: 1
└── Total Downloads: 23

Current Downloads:
├── ID: 123 | Arte - Documentaries - Episode 1 | 75% | 2.5 MB/s
└── ID: 124 | Canal+ - News - Episode 2 | 45% | 1.8 MB/s
```

## Configuration

### Configuration Options

#### Configuration Options

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <download>
        <directory>./downloads</directory>
        <timeout>300000</timeout>
        <retry-count>3</retry-count>
        <threads>4</threads>
        <buffer-size>8192</buffer-size>
    </download>
    
    <gui>
        <enabled>true</enabled>
        <system-tray>true</system-tray>
        <minimize-to-tray>true</minimize-to-tray>
        <start-minimized>false</start-minimized>
        <update-interval>1000</update-interval>
    </gui>
    
    <logging>
        <level>INFO</level>
        <file-enabled>true</file-enabled>
        <file-path>./logs/habiTv.log</file-path>
        <file-max-size>10485760</file-max-size>
        <file-max-files>5</file-max-files>
        <console-enabled>true</console-enabled>
    </logging>
    
    <plugins>
        <auto-update>true</auto-update>
        <update-interval>3600000</update-interval>
        <directory>./plugins</directory>
        <cache-directory>./plugins/cache</cache-directory>
        <debug-enabled>false</debug-enabled>
    </plugins>
</config>
```

#### Environment Variables

Set environment variables for configuration:

```properties
# Download settings
HABITV_DOWNLOAD_DIR=./downloads
HABITV_DOWNLOAD_TIMEOUT=300000
HABITV_DOWNLOAD_THREADS=4

# GUI settings
HABITV_GUI_ENABLED=true
HABITV_GUI_SYSTEM_TRAY=true

# Logging settings
HABITV_LOG_LEVEL=INFO
HABITV_LOG_FILE_ENABLED=true

# Plugin settings
HABITV_PLUGIN_AUTO_UPDATE=true
HABITV_PLUGIN_DIRECTORY=./plugins
```

#### Basic Configuration

Essential settings for most users:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <download>
        <directory>/home/user/Videos/habiTv</directory>
        <threads>4</threads>
        <timeout>300000</timeout>
    </download>
    
    <gui>
        <enabled>true</enabled>
        <system-tray>true</system-tray>
        <minimize-to-tray>true</minimize-to-tray>
    </gui>
    
    <logging>
        <level>INFO</level>
        <file-enabled>true</file-enabled>
    </logging>
    
    <plugins>
        <auto-update>true</auto-update>
    </plugins>
</config>
```

#### Advanced Configuration

Advanced settings for power users:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <download>
        <directory>/home/user/Videos/habiTv</directory>
        <threads>8</threads>
        <timeout>600000</timeout>
        <retry-count>5</retry-count>
        <buffer-size>16384</buffer-size>
        <chunk-size>2097152</chunk-size>
    </download>
    
    <gui>
        <enabled>true</enabled>
        <system-tray>true</system-tray>
        <minimize-to-tray>true</minimize-to-tray>
        <start-minimized>false</start-minimized>
        <update-interval>500</update-interval>
        <theme>dark</theme>
    </gui>
    
    <logging>
        <level>DEBUG</level>
        <file-enabled>true</file-enabled>
        <file-path>./logs/habiTv.log</file-path>
        <file-max-size>52428800</file-max-size>
        <file-max-files>10</file-max-files>
        <console-enabled>true</console-enabled>
    </logging>
    
    <plugins>
        <auto-update>true</auto-update>
        <update-interval>1800000</update-interval>
        <directory>./plugins</directory>
        <cache-directory>./plugins/cache</cache-directory>
        <debug-enabled>true</debug-enabled>
    </plugins>
    
    <performance>
        <memory-limit>2g</memory-limit>
        <thread-pool-size>16</thread-pool-size>
        <cache-size>1000</cache-size>
        <gc-optimization>true</gc-optimization>
    </performance>
</config>
```

#### Plugin Settings

Configure individual plugins:

```xml
<plugins>
    <plugin name="arte">
        <enabled>true</enabled>
        <timeout>30000</timeout>
        <retry-count>3</retry-count>
        <user-agent>habiTv/1.0</user-agent>
    </plugin>
    
    <plugin name="canalplus">
        <enabled>true</enabled>
        <timeout>45000</timeout>
        <retry-count>5</retry-count>
        <user-agent>habiTv/1.0</user-agent>
    </plugin>
</plugins>
```

#### Export Settings

Configure video export options:

```xml
<export>
    <default-format>mp4</default-format>
    <quality>high</quality>
    <audio-bitrate>128</audio-bitrate>
    <video-bitrate>2000</video-bitrate>
    <fps>30</fps>
    <resolution>1920x1080</resolution>
    <include-subtitles>true</include-subtitles>
    <include-metadata>true</include-metadata>
</export>
```

## Advanced Features

### Automation

#### Scheduled Downloads

Set up automatic downloads:

```bash
# Create download script
cat > download_script.sh << 'EOF'
#!/bin/bash
java -jar consoleView.jar --download --plugins arte --category "Documentaries"
java -jar consoleView.jar --download --plugins canalplus --category "News"
EOF

# Make executable
chmod +x download_script.sh

# Add to crontab (daily at 2 AM)
echo "0 2 * * * /path/to/download_script.sh" | crontab -
```

#### Daemon Mode

Run habiTv as a background service:

```bash
# Create systemd service
sudo tee /etc/systemd/system/habitv.service << EOF
[Unit]
Description=habiTv Download Service
After=network.target

[Service]
Type=simple
User=habitv
WorkingDirectory=/home/habitv
ExecStart=/usr/bin/java -jar /home/habitv/habiTv.jar --daemon
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl enable habitv
sudo systemctl start habitv
```

### Content Management

#### Favorites

Manage favorite content:

```bash
# Add to favorites
java -jar consoleView.jar --favorite --plugins arte --category "Documentaries" --episode "Example Episode"

# List favorites
java -jar consoleView.jar --listFavorites

# Remove from favorites
java -jar consoleView.jar --unfavorite --id 123

# Download all favorites
java -jar consoleView.jar --downloadFavorites
```

#### Playlists

Create and manage playlists:

```bash
# Create playlist
java -jar consoleView.jar --createPlaylist --name "My Documentaries"

# Add to playlist
java -jar consoleView.jar --addToPlaylist --playlist "My Documentaries" --plugins arte --category "Documentaries" --episode "Example Episode"

# List playlists
java -jar consoleView.jar --listPlaylists

# Download playlist
java -jar consoleView.jar --downloadPlaylist --name "My Documentaries"
```

### Post-Processing

#### Video Processing

Apply post-processing to downloaded videos:

```bash
# Convert to different format
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --postProcess "convert:mp4"

# Extract audio
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --postProcess "extract-audio:mp3"

# Add watermark
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --postProcess "watermark:/path/to/logo.png"
```

#### Metadata Management

Manage video metadata:

```bash
# Add metadata
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --metadata "title=Custom Title,artist=Custom Artist"

# Extract metadata
java -jar consoleView.jar --extractMetadata --input /path/to/video.mp4

# Update metadata
java -jar consoleView.jar --updateMetadata --input /path/to/video.mp4 --metadata "title=New Title"
```

## Troubleshooting

### Common Issues

#### Application Won't Start

Check Java installation and permissions:

```bash
# Check Java version
java -version

# Check file permissions
ls -la habiTv.jar

# Run with debug output
java -jar habiTv.jar --debug
```

#### Downloads Fail

Check network and configuration:

```bash
# Test network connectivity
ping google.com

# Check download directory permissions
ls -la /path/to/downloads

# Test with verbose output
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --verbose
```

#### Plugins Not Working

Check plugin status and updates:

```bash
# List plugins
java -jar consoleView.jar --listPlugin

# Update plugins
java -jar consoleView.jar --updatePlugin

# Check plugin logs
tail -f logs/habiTv.log | grep -i plugin
```

### Performance Optimization

#### Memory Usage

Optimize memory usage:

```bash
# Run with limited memory
java -Xmx512m -jar habiTv.jar

# Monitor memory usage
jps -l
jstat -gc <pid>
```

#### Network Optimization

Optimize network performance:

```bash
# Set custom timeout
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --timeout 600000

# Use proxy
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --proxy http://proxy:8080

# Limit bandwidth
java -jar consoleView.jar --download --plugins arte --category "Documentaries" --episode "Example Episode" --bandwidth 1024
```

## Best Practices

### Content Discovery

- **Use Search**: Search is faster than browsing categories
- **Check Categories**: Browse categories to discover new content
- **Monitor Updates**: Check for new episodes regularly
- **Use Filters**: Use filters to narrow down results

#### Download Management

- **Batch Downloads**: Download multiple episodes at once
- **Queue Management**: Monitor and manage download queue
- **Storage Planning**: Plan storage space for downloads
- **Backup Strategy**: Back up important downloads

#### Performance

- **Limit Concurrent Downloads**: Don't overload your connection
- **Monitor Resources**: Watch CPU and memory usage
- **Optimize Settings**: Adjust settings for your system
- **Regular Maintenance**: Clean up old files and logs

### File Organization

Organize downloaded content:

```text
downloads/
├── Arte/
│   ├── Documentaries/
│   │   ├── 2024/
│   │   │   ├── January/
│   │   │   └── February/
│   │   └── 2023/
│   └── News/
├── Canal+/
│   ├── Movies/
│   └── Series/
└── Pluzz/
    ├── Programs/
    └── Specials/
```

#### Backup Strategy

Backup important content:

```bash
# Create backup script
cat > backup_downloads.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backup/habitv/$(date +%Y-%m-%d)"
mkdir -p "$BACKUP_DIR"
rsync -av /downloads/ "$BACKUP_DIR/"
echo "Backup completed: $BACKUP_DIR"
EOF

# Make executable
chmod +x backup_downloads.sh

# Add to crontab (weekly backup)
echo "0 3 * * 0 /path/to/backup_downloads.sh" | crontab -
```

This user guide provides comprehensive instructions for using habiTv effectively. Follow these guidelines to get the most out of the application and troubleshoot common issues. 
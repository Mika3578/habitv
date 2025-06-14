# habiTv User Guide

This guide covers how to use habiTv effectively for both GUI and command-line interfaces.

## Getting Started

### First Launch

When you first launch habiTv, you'll see a splash screen and then be prompted to choose your interface mode:

1. **GUI Mode**: System tray application with graphical interface
2. **CLI Mode**: Command-line interface for advanced users

### Initial Configuration

After choosing your interface, habiTv will:
1. Create configuration files in your user directory
2. Download required external tools (if not already present)
3. Check for updates
4. Present the main interface

## GUI Mode

### System Tray Interface

When running in GUI mode, habiTv appears as an icon in your system tray:

- **Right-click** the tray icon for the main menu
- **Left-click** to open the main window
- **Double-click** to open manual download dialog

### Main Window

The main window has several tabs:

#### 1. Download Monitoring Tab

This tab shows all current and completed downloads:

**Features:**
- **Active Downloads**: Shows progress, speed, and estimated time
- **Completed Downloads**: History of finished downloads
- **Failed Downloads**: List of failed attempts with error details

**Actions:**
- **Pause/Resume**: Control individual downloads
- **Cancel**: Stop a download and mark as failed
- **Retry**: Restart a failed download
- **Open File**: Open the downloaded file location
- **View Log**: Open the detailed log for troubleshooting

#### 2. Show Selection Tab

Browse and select shows to monitor:

**Categories:**
- **TV Channels**: Organized by content provider (Canal+, Arte, etc.)
- **Genres**: Drama, Comedy, News, Sports, etc.
- **Custom**: RSS feeds and file-based sources

**Actions:**
- **Browse Categories**: Navigate through available content
- **Search**: Find specific shows or episodes
- **Add to Monitoring**: Select shows for automatic download
- **Remove from Monitoring**: Stop monitoring a show
- **Manual Download**: Download a specific episode immediately

#### 3. Configuration Tab

Manage application settings:

**General Settings:**
- **Download Directory**: Where files are saved
- **Log Level**: Detail level for logging (DEBUG, INFO, WARN, ERROR)
- **Update Settings**: Automatic update preferences
- **Proxy Configuration**: Network proxy settings

**Download Settings:**
- **Maximum Concurrent Downloads**: Number of simultaneous downloads
- **Retry Attempts**: How many times to retry failed downloads
- **Download Speed Limits**: Bandwidth restrictions
- **File Naming**: Customize downloaded file names

**Export Settings:**
- **Post-Processing**: Automatic video conversion or processing
- **Export Commands**: Custom commands to run after download
- **File Organization**: Automatic file sorting and renaming

### Manual Download Dialog

Access via system tray icon or main menu:

1. **Paste URL**: Enter a video URL to download
2. **Auto-Detect**: habiTv automatically finds the best download method
3. **Manual Selection**: Choose specific downloader if needed
4. **Start Download**: Begin the download process

## Command Line Interface (CLI)

### Basic Commands

```bash
# Show help
java -jar habiTv.jar --help

# List available categories
java -jar habiTv.jar --list-categories

# Search for shows
java -jar habiTv.jar --search "show name"

# Download specific episode
java -jar habiTv.jar --download "episode_id"

# Run in daemon mode
java -jar habiTv.jar --daemon
```

### Advanced CLI Options

```bash
# Specify configuration file
java -jar habiTv.jar --config /path/to/config.xml

# Set log level
java -jar habiTv.jar --log-level DEBUG

# Download with specific options
java -jar habiTv.jar --download "episode_id" --output /custom/path

# Run scheduled check
java -jar habiTv.jar --check-updates

# Export configuration
java -jar habiTv.jar --export-config /path/to/export.xml
```

### Daemon Mode

For continuous operation:

```bash
# Start daemon
java -jar habiTv.jar --daemon

# Check daemon status
java -jar habiTv.jar --status

# Stop daemon
java -jar habiTv.jar --stop
```

## Content Providers

### Supported Platforms

#### Canal+
- **Content**: Premium French TV shows and movies
- **Features**: High-quality streams, exclusive content
- **Requirements**: May require authentication for premium content

#### Pluzz (France TV)
- **Channels**: France 2, France 3, France 4, France Ô
- **Content**: News, documentaries, entertainment
- **Features**: Free content, good quality

#### Arte
- **Content**: Cultural and educational programming
- **Features**: High-quality documentaries, international content
- **Languages**: French and German content

#### Sports Channels
- **L'Équipe**: Sports news and highlights
- **beIN Sports**: Live sports and analysis
- **Features**: Live streams and replays

#### RSS Feeds
- **Generic Support**: Any RSS feed with video links
- **Formats**: HTTP, FTP, BitTorrent, YouTube, Dailymotion
- **Customization**: Full control over content sources

### Adding Custom Sources

#### RSS Feed Setup

1. **Find RSS Feed**: Locate the RSS feed URL for your content
2. **Add to habiTv**: Use the RSS plugin to add the feed
3. **Configure Filters**: Set up content matching rules
4. **Test**: Verify the feed works correctly

#### File-Based Sources

1. **Create Source File**: Text file with video URLs
2. **Add to habiTv**: Use the file plugin
3. **Set Update Schedule**: How often to check for new content
4. **Configure Processing**: How to handle the URLs

## Download Management

### Download States

- **Pending**: Queued for download
- **Downloading**: Currently being downloaded
- **Paused**: Temporarily stopped
- **Completed**: Successfully downloaded
- **Failed**: Download failed (with error details)
- **Cancelled**: Manually cancelled

### Download Options

#### Quality Selection
- **Automatic**: Let habiTv choose the best quality
- **Manual**: Select specific quality (HD, SD, etc.)
- **Custom**: Specify exact resolution or bitrate

#### Download Limits
- **Speed Limits**: Restrict bandwidth usage
- **Concurrent Downloads**: Control number of simultaneous downloads
- **Time Restrictions**: Download only during specific hours

#### Retry Logic
- **Automatic Retry**: Failed downloads are retried automatically
- **Retry Count**: Maximum number of retry attempts
- **Retry Delay**: Time between retry attempts

### File Management

#### Naming Conventions
- **Default**: `Show Name - Episode Title - Date.ext`
- **Custom**: Configure your own naming pattern
- **Variables**: Use placeholders for dynamic naming

#### Organization
- **By Show**: Separate folders for each show
- **By Date**: Organize by download date
- **By Quality**: Separate folders by video quality
- **Custom**: Define your own organization rules

## Export and Post-Processing

### Built-in Export Options

#### Video Conversion
- **Format Conversion**: Convert to different video formats
- **Quality Adjustment**: Resize or re-encode videos
- **Audio Extraction**: Extract audio tracks
- **Subtitle Handling**: Embed or extract subtitles

#### File Operations
- **Move Files**: Move to different locations
- **Copy Files**: Create copies in multiple locations
- **Delete Originals**: Remove original files after processing
- **Archive**: Compress or archive old files

### Custom Export Commands

#### Command Templates
```bash
# Basic FFmpeg conversion
ffmpeg -i "{input}" -c:v libx264 -c:a aac "{output}"

# Upload to FTP
curl -T "{input}" ftp://server.com/path/

# Send notification
notify-send "Download Complete" "{filename}"
```

#### Variables Available
- `{input}`: Input file path
- `{output}`: Output file path
- `{filename}`: Just the filename
- `{show}`: Show name
- `{episode}`: Episode title
- `{date}`: Download date

### Export Triggers

- **On Download Complete**: Run immediately after download
- **On Batch Complete**: Run after multiple downloads finish
- **Scheduled**: Run at specific times
- **Manual**: Run export commands manually

## Monitoring and Notifications

### System Notifications

#### Notification Types
- **Download Started**: When a new download begins
- **Download Complete**: When a download finishes successfully
- **Download Failed**: When a download fails
- **New Episodes**: When new episodes are found
- **Export Complete**: When post-processing finishes

#### Notification Settings
- **Enable/Disable**: Turn notifications on or off
- **Sound**: Play sounds for notifications
- **Duration**: How long notifications are displayed
- **Priority**: Which notifications to show

### Logging

#### Log Levels
- **DEBUG**: Detailed debugging information
- **INFO**: General information about operations
- **WARN**: Warning messages for potential issues
- **ERROR**: Error messages for problems

#### Log Management
- **Log Rotation**: Automatic log file rotation
- **Log Size Limits**: Maximum log file sizes
- **Log Retention**: How long to keep old logs
- **Log Export**: Export logs for analysis

## Troubleshooting

### Common Issues

#### Download Failures
1. **Check Internet Connection**: Ensure stable internet access
2. **Verify Content Availability**: Content may have been removed
3. **Check External Tools**: Ensure rtmpDump, curl, etc. are working
4. **Review Logs**: Check log files for specific error messages

#### Performance Issues
1. **Reduce Concurrent Downloads**: Lower the number of simultaneous downloads
2. **Check Disk Space**: Ensure sufficient storage space
3. **Monitor System Resources**: Check CPU and memory usage
4. **Optimize Network**: Use wired connection if possible

#### Configuration Problems
1. **Validate XML Files**: Check config.xml and grabconfig.xml syntax
2. **Reset Configuration**: Delete config files to start fresh
3. **Check Permissions**: Ensure write permissions to config directory
4. **Review Environment Variables**: Check for conflicting settings

### Getting Help

#### Self-Help Resources
- **Log Files**: Check `%USERPROFILE%\habitv\habiTv.log` (Windows) or `~/.habitv/habiTv.log` (Linux)
- **Configuration Files**: Review settings in config.xml
- **Documentation**: Refer to this guide and other documentation

#### Community Support
- **GitHub Issues**: Search existing issues or create new ones
- **Wiki**: Check the project wiki for additional information
- **Forums**: Community forums for user discussions

#### Reporting Issues
When reporting problems, include:
- **Operating System**: Windows/Linux version
- **Java Version**: Output of `java -version`
- **habiTv Version**: Version you're using
- **Error Messages**: Exact error text from logs
- **Steps to Reproduce**: How to recreate the issue
- **Configuration**: Relevant parts of your config files

## Advanced Features

### Automation

#### Scheduled Tasks
- **Automatic Checks**: Schedule regular content checks
- **Batch Downloads**: Download multiple episodes at once
- **Maintenance Tasks**: Automatic cleanup and organization

#### Integration
- **External Scripts**: Integrate with your own scripts
- **API Access**: Programmatic access to habiTv features
- **Web Interface**: Optional web-based management interface

### Customization

#### Plugin Development
- **Content Providers**: Add support for new video sources
- **Downloaders**: Implement new download methods
- **Exporters**: Create custom post-processing plugins

#### Configuration Templates
- **Preset Configurations**: Save and share configuration setups
- **Environment-Specific**: Different configs for different environments
- **Backup and Restore**: Export and import configurations

## Best Practices

### Performance Optimization
1. **Use SSD Storage**: Faster read/write speeds for downloads
2. **Limit Concurrent Downloads**: Balance speed with system resources
3. **Regular Cleanup**: Remove old downloads and logs
4. **Monitor Resources**: Keep an eye on system usage

### Content Management
1. **Organize Downloads**: Use consistent naming and folder structure
2. **Regular Backups**: Backup your configuration files
3. **Quality Selection**: Choose appropriate quality for your needs
4. **Storage Planning**: Plan for sufficient storage space

### Network Usage
1. **Bandwidth Management**: Set appropriate speed limits
2. **Scheduled Downloads**: Download during off-peak hours
3. **Proxy Configuration**: Use proxies if needed
4. **Connection Monitoring**: Monitor for connection issues

This user guide covers the essential features of habiTv. For more advanced topics, refer to the [Developer Documentation](DEVELOPER_GUIDE.md) or [Plugin Development Guide](PLUGIN_DEVELOPMENT.md). 
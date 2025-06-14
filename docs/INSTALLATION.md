# habiTv Installation Guide

This guide covers installing habiTv on different operating systems and configurations.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 14, 2025

## Prerequisites

### Java Requirements

habiTv requires **Java 8 or higher**. To check your Java version:

```bash
java -version
```

If Java is not installed or you need to upgrade:

**Windows:**
1. Download from [Oracle Java](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
2. Install and add to PATH
3. Restart your terminal/command prompt

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-8-jdk
```

**Linux (CentOS/RHEL/Fedora):**
```bash
sudo yum install java-1.8.0-openjdk
# or for newer versions
sudo dnf install java-1.8.0-openjdk
```

### System Requirements

- **RAM**: Minimum 512MB, recommended 2GB
- **Storage**: 1GB for application + space for downloads
- **Network**: Stable internet connection
- **OS**: Windows 10+ or Linux (Ubuntu 18.04+, CentOS 7+)

## Installation Methods

### Method 1: Pre-built Packages (Recommended)

#### Windows Installation

1. **Download the Windows package:**
   - Go to [Releases](https://github.com/your-repo/habitv/releases)
   - Download `habiTv-windows-4.1.0-SNAPSHOT.zip`

2. **Extract and run:**
   ```cmd
   # Extract to a folder (e.g., C:\habitv)
   # Run the executable
   habiTv.exe
   ```

3. **Alternative - JAR file:**
   ```cmd
   java -jar habiTv-4.1.0-SNAPSHOT.jar
   ```

#### Linux Installation

**Debian/Ubuntu (.deb package):**
```bash
# Download and install
wget https://github.com/your-repo/habitv/releases/download/v4.1.0/habiTv-linux-4.1.0-SNAPSHOT.deb
sudo dpkg -i habiTv-linux-4.1.0-SNAPSHOT.deb

# If dependencies are missing
sudo apt-get install -f
```

**Generic Linux (JAR file):**
```bash
# Download the JAR
wget https://github.com/your-repo/habitv/releases/download/v4.1.0/habiTv-4.1.0-SNAPSHOT.jar

# Make executable
chmod +x habiTv-4.1.0-SNAPSHOT.jar

# Run
java -jar habiTv-4.1.0-SNAPSHOT.jar
```

### Method 2: Building from Source

#### Prerequisites for Building

- **Maven**: Version 3.6 or higher
- **Git**: For cloning the repository
- **Java Development Kit (JDK)**: Version 8 or higher

**Install Maven:**

**Windows:**
1. Download from [Maven website](https://maven.apache.org/download.cgi)
2. Extract to a directory (e.g., `C:\Program Files\Apache\maven`)
3. Add to PATH: `C:\Program Files\Apache\maven\bin`

**Linux:**
```bash
sudo apt install maven  # Ubuntu/Debian
sudo yum install maven  # CentOS/RHEL
```

#### Build Steps

```bash
# Clone the repository
git clone https://github.com/your-repo/habitv.git
cd habitv

# Build the project
mvn clean compile

# Run tests (optional but recommended)
mvn test

# Create executable JAR
mvn package

# The JAR will be in application/habiTv/target/
java -jar application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar
```

## Configuration

### First Run Setup

1. **Launch habiTv** for the first time
2. **Choose interface mode:**
   - GUI Mode: System tray application with notifications
   - CLI Mode: Command line interface

3. **Configure basic settings:**
   - Download directory
   - Log level
   - Update preferences

### Configuration Files

habiTv creates configuration files in the user directory:

**Windows:** `%USERPROFILE%\habitv\`
**Linux:** `~/.habitv/`

Key files:
- `config.xml`: Main application configuration
- `grabconfig.xml`: Show selection and monitoring settings
- `habiTv.log`: Application logs

### Environment Variables

You can override default settings using environment variables:

```cmd
# Windows
set HABITV_HOME=C:\MyHabitv
set HABITV_DOWNLOAD_OUTPUT=D:\Downloads\TV
```

```bash
# Linux
export HABITV_HOME=/opt/habitv
export HABITV_DOWNLOAD_OUTPUT=/home/user/Downloads/TV
```

See [ENVIRONMENT_VARIABLES.md](../ENVIRONMENT_VARIABLES.md) for complete list.

## External Dependencies

habiTv uses external tools for downloading and processing videos. These are **automatically managed** by the application's update system.

### Automatic Tool Management

- **Startup Check**: habiTv automatically checks for required external tools at startup
- **Auto-Download**: Missing or outdated tools are downloaded from the configured repository
- **Version Control**: Tools are automatically updated when newer versions are available
- **No Manual Installation Required**: The system handles everything automatically

### Required Tools

- **rtmpDump**: For RTMP stream downloads
- **curl**: For HTTP downloads
- **aria2c**: For high-speed downloads
- **yt-dlp**: For YouTube and other platforms (replaces youtube-dl)
- **ffmpeg**: For video processing and conversion

### Repository Configuration

The external tools are downloaded from a remote repository. The default repository URL is:
```
http://dabiboo.free.fr/repository
```

You can configure a custom repository in your configuration file:
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>https://your-custom-repo.com/habitv-tools</repositoryUrl>
</updateConfig>
```

#### Repository Update Limitations

**Important**: The current repository system requires manual updates by the repository maintainer. This means:

- **Delayed Updates**: New tool versions may not be immediately available
- **Dependency**: Updates depend on repository maintainer availability
- **Manual Process**: Someone must manually download and upload new versions

**Solutions**:
- **Automated Updates**: Repository maintainers can set up automated update scripts
- **Hybrid Approach**: habiTv can use repository as primary source with direct download fallback
- **Custom Repository**: Set up your own automated repository

For more details on automated solutions, see [STARTUP_UPDATE_SYSTEM.md](STARTUP_UPDATE_SYSTEM.md#repository-management-challenges).

### Manual Installation (Advanced Users Only)

If you prefer manual control or have network restrictions, you can manually place binaries in the `bin/` directory:

**Windows:**
```cmd
# Create bin directory if it doesn't exist
mkdir bin

# Download tools manually
curl -L -o bin/yt-dlp.exe https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe
curl -L -o bin/ffmpeg.exe https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
# ... other tools as needed
```

**Linux:**
```bash
# Create bin directory if it doesn't exist
mkdir -p bin

# Download tools manually
curl -L -o bin/yt-dlp https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp
chmod +x bin/yt-dlp
# ... other tools as needed
```

### Troubleshooting External Tools

#### Common Issues

1. **"Tool not found" error:**
   - Check internet connection
   - Verify repository URL is accessible
   - Check firewall/proxy settings
   - Review logs for download errors

2. **"Permission denied" (Linux):**
   ```bash
   chmod +x bin/*
   ```

3. **"Version mismatch" error:**
   - The tool will be automatically updated on next startup
   - Or manually delete the old binary to force re-download

4. **"Download failed" error:**
   - Check repository availability
   - Verify network connectivity
   - Check disk space in bin directory

#### Logs and Debugging

External tool issues are logged in:
- **Windows**: `%USERPROFILE%\habitv\habiTv.log`
- **Linux**: `~/.habitv/habiTv.log`

Look for entries like:
```
INFO - Checking yt-dlp version
INFO - Downloading yt-dlp from repository
ERROR - Failed to download yt-dlp: Connection timeout
```

### Security Considerations

- **Repository Trust**: Ensure you trust the repository providing the tools
- **HTTPS**: Use HTTPS repositories when possible
- **Checksums**: The system verifies downloaded files when possible
- **Updates**: Keep tools updated for security patches

### Custom Repository Setup

If you want to host your own tool repository:

1. **Directory Structure:**
   ```
   your-repo/
   ├── bin/
   │   ├── yt-dlp.exe.zip
   │   ├── ffmpeg.exe.zip
   │   ├── aria2c.exe.zip
   │   ├── rtmpdump.exe.zip
   │   └── curl.exe.zip
   └── plugins.txt
   ```

2. **Tool Packaging:**
   - Package each tool as a ZIP file
   - Include the executable and any dependencies
   - Use consistent naming: `{toolname}.exe.zip` for Windows

3. **Version Management:**
   - Update the ZIP files when new versions are released
   - The system will detect version changes automatically

## Verification

### Test Installation

1. **Launch habiTv:**
   ```bash
   java -jar habiTv-4.1.0-SNAPSHOT.jar
   ```

2. **Check for errors** in the console output

3. **Verify configuration files** are created:
   ```cmd
   # Windows
   dir %USERPROFILE%\habitv

   # Linux
   ls -la ~/.habitv
   ```

4. **Test a simple download:**
   - Use the GUI to browse available shows
   - Or use CLI: `java -jar habiTv.jar --list-categories`

### Troubleshooting Installation

#### Common Issues

1. **"Java not found" error:**
   - Verify Java is installed: `java -version`
   - Check JAVA_HOME environment variable
   - Ensure Java is in PATH

2. **"Permission denied" (Linux):**
   ```bash
   chmod +x habiTv-4.1.0-SNAPSHOT.jar
   ```

3. **"Port already in use" error:**
   - Check if another instance is running
   - Kill existing process or change port in config

4. **Download failures:**
   - Check internet connection
   - Verify external tools are available
   - Check firewall settings

#### Log Analysis

Check the log file for detailed error information:

**Windows:** `%USERPROFILE%\habitv\habiTv.log`
**Linux:** `~/.habitv/habiTv.log`

Common log entries:
- `INFO`: Normal operation
- `WARN`: Non-critical issues
- `ERROR`: Problems that need attention
- `DEBUG`: Detailed debugging information

## Uninstallation

### Windows

1. **Stop habiTv** if running
2. **Delete installation directory**
3. **Remove configuration files** (optional):
   ```cmd
   rmdir /s %USERPROFILE%\habitv
   ```

### Linux

**If installed via .deb:**
```bash
sudo dpkg -r habitv
```

**If using JAR file:**
```bash
# Remove JAR file
rm habiTv-4.1.0-SNAPSHOT.jar

# Remove configuration (optional)
rm -rf ~/.habitv
```

## Next Steps

After successful installation:

1. **Read the [User Guide](USER_GUIDE.md)**
2. **Configure your first shows** to monitor
3. **Set up export options** for post-processing
4. **Join the community** for support and updates

## Support

If you encounter issues during installation:

1. **Check this guide** for common solutions
2. **Review the logs** for error details
3. **Search existing issues** on GitHub
4. **Create a new issue** with detailed information

Include in your support request:
- Operating system and version
- Java version
- Error messages or log excerpts
- Steps to reproduce the issue
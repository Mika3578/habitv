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

habiTv uses external tools for downloading and processing videos. These are automatically downloaded on first run, but you can install them manually:

### Required Tools

- **rtmpDump**: For RTMP stream downloads
- **curl**: For HTTP downloads
- **aria2c**: For high-speed downloads
- **youtube-dl**: For YouTube and other platforms
- **ffmpeg**: For video processing and conversion

### Manual Installation

**Windows:**
```cmd
# Download tools manually or use Chocolatey
choco install ffmpeg curl

# Or download from official websites
# https://ffmpeg.org/download.html
# https://curl.se/windows/
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt install ffmpeg curl aria2

# CentOS/RHEL
sudo yum install ffmpeg curl aria2
```

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
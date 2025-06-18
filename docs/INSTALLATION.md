# habiTv Installation Guide

This guide covers installing habiTv on different operating systems and configurations.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 15, 2025

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
   - Go to [Releases](https://github.com/mika3578/habitv/releases)
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
wget https://github.com/mika3578/habitv/releases/download/v4.1.0/habiTv-linux-4.1.0-SNAPSHOT.deb
sudo dpkg -i habiTv-linux-4.1.0-SNAPSHOT.deb

# If dependencies are missing
sudo apt-get install -f
```

**Generic Linux (JAR file):**

```bash
# Download the JAR
wget https://github.com/mika3578/habitv/releases/download/v4.1.0/habiTv-4.1.0-SNAPSHOT.jar

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
git clone https://github.com/mika3578/habitv.git
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

#### Deployment Workflow (For Maintainers)

If you're a maintainer or contributor who needs to deploy Maven artifacts to the GitHub Pages repository, follow this workflow:

**Prerequisites:**
- Git repository access with push permissions
- Maven installed and configured
- PowerShell (Windows) or Bash (Linux/macOS)

**Automated Deployment:**

**Windows (PowerShell):**
```powershell
# Combined build, deploy, and publish
.\scripts\deploy-and-publish.ps1

# Or step by step
mvn clean deploy
.\scripts\post-deploy.ps1
```

**Linux/macOS (Bash):**
```bash
# Combined build, deploy, and publish
./scripts/deploy-and-publish.sh

# Or step by step
mvn clean deploy
./scripts/post-deploy.sh
```

**Manual Deployment:**
```bash
# Step 1: Build and deploy to local repository
mvn clean deploy

# Step 2: Copy artifacts to docs/repository
cp -r ~/.m2/repository/com/dabi/habitv/* docs/repository/com/dabi/habitv/

# Step 3: Commit and push to GitHub Pages
git add docs/repository/com/dabi/habitv/
git commit -m "Deploy Maven artifacts to GitHub Pages"
git push
```

**What This Does:**
1. Builds all Maven modules and generates artifacts
2. Copies artifacts from local Maven repository to `docs/repository/`
3. Commits changes to git
4. Pushes to GitHub Pages for public distribution

**Verification:**
After deployment, artifacts will be available at:
```
https://mika3578.github.io/habitv/repository/com/dabi/habitv/
```

For detailed information about the deployment workflow, see the [Developer Guide](DEVELOPER_GUIDE.md#deployment-workflow).

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

### Plugin Auto-Update Configuration

**Important**: By default, habiTv **disables automatic plugin updates** for security and stability reasons. This is different from external tool management, which remains enabled by default.

#### Default Behavior

- **Plugin Auto-Update**: **Disabled** by default
- **External Tool Management**: **Enabled** by default (ffmpeg, rtmpDump, etc.)
- **Security**: Prevents unauthorized code execution from external repositories

#### Enabling Plugin Auto-Update

If you want to enable automatic plugin updates, add the following to your `config.xml`:

```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

**Security Considerations:**
- Plugin auto-update downloads executable JAR files from external repositories
- Only enable if you trust the configured repository
- Consider the security implications before enabling
- The repository should be under your control or from a trusted source

**Configuration Steps:**
1. Open your `config.xml` file (usually in `%USERPROFILE%\habitv\` on Windows or `~/.habitv/` on Linux)
2. Add or modify the `<updateConfig>` section
3. Set `<updateOnStartup>true</updateOnStartup>`
4. Restart habiTv

**Note**: External tools (ffmpeg, rtmpDump, curl, etc.) are still automatically managed regardless of this setting.

## Security Configuration

### Security Overview

habiTv implements a security-first approach with several layers of protection:

1. **Plugin Security**: Disabled by default to prevent unauthorized code execution
2. **Network Security**: HTTPS enforcement and certificate validation
3. **Configuration Security**: Secure defaults and input validation
4. **Repository Security**: Configurable and verifiable sources

### Security Levels

#### Level 1: Maximum Security (Recommended for Production)

**Configuration:**
```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

**Features:**
- ✅ Plugin auto-updates disabled
- ✅ External tool updates enabled (trusted sources only)
- ✅ HTTPS enforcement for all downloads
- ✅ Certificate validation enabled
- ✅ Input sanitization active

**Use Cases:**
- Production environments
- Corporate networks
- High-security requirements
- Unattended operation

#### Level 2: Standard Security (Default)

**Configuration:**
```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

**Features:**
- ✅ Plugin auto-updates disabled (default)
- ✅ External tool updates enabled
- ✅ Standard security measures
- ✅ User consent required for plugins

**Use Cases:**
- Home users
- Standard deployments
- General purpose usage

#### Level 3: Development/Testing

**Configuration:**
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
</updateConfig>
```

**Features:**
- ⚠️ Plugin auto-updates enabled
- ⚠️ Snapshot versions allowed
- ✅ All security measures still active
- ✅ Enhanced logging for debugging

**Use Cases:**
- Development environments
- Testing scenarios
- Plugin development
- Debugging sessions

### Security Best Practices

#### For End Users

1. **Start with Maximum Security:**
   - Use Level 1 configuration initially
   - Only enable features you understand and trust

2. **Regular Security Reviews:**
   - Review configuration files periodically
   - Check logs for unusual activity
   - Update the application regularly

3. **Network Security:**
   - Use HTTPS repositories when possible
   - Configure firewalls to restrict access
   - Monitor network traffic

4. **File System Security:**
   - Restrict access to habiTv directories
   - Use dedicated user accounts
   - Regular backups of configuration

#### For System Administrators

1. **Deployment Security:**
   - Deploy with maximum security settings
   - Use dedicated service accounts
   - Implement proper file permissions

2. **Network Configuration:**
   - Configure proxy settings if needed
   - Use internal repositories when possible
   - Implement network monitoring

3. **Monitoring and Logging:**
   - Set up log monitoring
   - Configure alerts for security events
   - Regular security audits

4. **Update Management:**
   - Test updates in staging environment
   - Implement change control procedures
   - Document configuration changes

### Security Configuration Examples

#### Corporate Environment

```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
    <repositoryUrl>https://internal-repo.company.com/habitv</repositoryUrl>
    <proxyHost>proxy.company.com</proxyHost>
    <proxyPort>8080</proxyPort>
</updateConfig>
```

#### Home User with Trusted Sources

```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
    <repositoryUrl>https://mika3578.github.io/habitv/repository</repositoryUrl>
</updateConfig>
```

#### Development Environment

```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>https://dev-repo.company.com/habitv</repositoryUrl>
    <logLevel>DEBUG</logLevel>
</updateConfig>
```

### Security Troubleshooting

#### Common Security Issues

1. **Certificate Errors:**
   ```
   ERROR - SSL certificate validation failed
   ```
   **Solution:** Verify repository certificate or use trusted sources

2. **Permission Denied:**
   ```
   ERROR - Access denied to configuration file
   ```
   **Solution:** Check file permissions and user access rights

3. **Repository Access:**
   ```
   ERROR - Cannot access repository
   ```
   **Solution:** Check network connectivity and repository availability

4. **Plugin Security Warnings:**
   ```
   WARN - Plugin auto-update is enabled
   ```
   **Solution:** Review security implications and disable if not needed

#### Security Log Analysis

Look for these security-related log entries:

```
INFO - Security: Plugin auto-update disabled
INFO - Security: Using HTTPS repository
INFO - Security: Certificate validation successful
WARN - Security: Plugin auto-update enabled (user choice)
ERROR - Security: Certificate validation failed
ERROR - Security: Repository access denied
```

### Reporting Security Issues

If you discover a security vulnerability:

1. **Do not create public issues** for security problems
2. **Email security details** to the maintainers
3. **Include:**
   - Detailed reproduction steps
   - Affected versions
   - Configuration details
   - Log files (sanitized)
4. **Follow responsible disclosure** practices

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

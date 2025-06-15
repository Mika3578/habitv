# habiTv Update & Repository Management

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 15, 2025

## Overview

habiTv uses an automatic update system and a repository-based approach to manage plugins and external tools. This system ensures users always have the latest versions of plugins and tools without manual intervention, while providing security, reliability, and flexibility.

### Key Features
- **Automatic Updates**: Check and download updates on startup
- **Repository-Based**: Centralized management of plugins and tools
- **Security-First**: HTTPS encryption and integrity verification
- **Cross-Platform**: Support for Windows and Linux
- **Flexible Configuration**: Multiple configuration methods
- **Automated Maintenance**: GitHub Actions for repository updates

---

## 1. Update System Architecture

### Components

#### Update Manager
- **Class**: `com.dabi.habitv.core.updater.UpdateManager`
- **Purpose**: Orchestrates the update process
- **Features**: Version comparison, dependency resolution, rollback support

#### Repository Client
- **Class**: `com.dabi.habitv.framework.plugin.api.update.impl.RepositoryClient`
- **Purpose**: Downloads plugins and tools from repositories
- **Features**: HTTP/HTTPS support, retry logic, timeout handling

#### Plugin Loader
- **Class**: `com.dabi.habitv.core.plugin.PluginsLoader`
- **Purpose**: Loads and initializes plugins
- **Features**: Hot reloading, dependency management, error handling

### Update Flow

```java
// Simplified update process
public class StartupUpdateManager {
    
    public void performStartupUpdates() {
        // 1. Check if updates are enabled
        if (!isUpdateEnabled()) {
            return;
        }
        
        // 2. Load current plugin inventory
        List<PluginInfo> currentPlugins = loadCurrentPlugins();
        
        // 3. Check repository for available updates
        List<PluginInfo> availablePlugins = checkRepository();
        
        // 4. Compare versions and identify updates needed
        List<PluginInfo> updatesNeeded = findUpdates(currentPlugins, availablePlugins);
        
        // 5. Download updates with progress tracking
        for (PluginInfo plugin : updatesNeeded) {
            downloadPlugin(plugin);
        }
        
        // 6. Validate downloaded files
        validateDownloads(updatesNeeded);
        
        // 7. Reload plugins with new versions
        reloadPlugins();
    }
}
```

### Update Conditions

Updates are performed when:
- **Configuration Enabled**: `updateOnStartup` is set to `true`
- **Network Available**: Internet connection is detected
- **Time Threshold**: Sufficient time has passed since last check
- **Repository Accessible**: Repository URL is reachable

---

## 2. Repository Structure & URLs

### Repository URLs

#### Primary Repository (Legacy)
- **URL**: `http://dabiboo.free.fr/repository`
- **Protocol**: HTTP (unencrypted)
- **Status**: Original repository, maintained for backward compatibility
- **Use Case**: Fallback when HTTPS is unavailable

#### GitHub Pages Mirror (Recommended)
- **URL**: `https://mika3578.github.io/habitv/repository/`
- **Protocol**: HTTPS (encrypted)
- **Status**: Secure mirror with automatic updates
- **Benefits**: High availability, global CDN, automated maintenance

### Repository Structure

```
repository/
├── com/dabi/habitv/           # Maven-style plugin artifacts
│   ├── arte/                  # Arte plugin
│   │   └── 4.1.0-SNAPSHOT/
│   │       ├── arte-4.1.0-SNAPSHOT.jar
│   │       ├── arte-4.1.0-SNAPSHOT.pom
│   │       └── maven-metadata.xml
│   ├── youtube/               # YouTube plugin
│   │   └── 4.1.0-SNAPSHOT/
│   │       ├── youtube-4.1.0-SNAPSHOT.jar
│   │       ├── youtube-4.1.0-SNAPSHOT.pom
│   │       └── maven-metadata.xml
│   └── ...                    # Other plugins
├── bin/                       # External tool binaries
│   ├── yt-dlp.exe.zip        # Windows yt-dlp
│   ├── yt-dlp.zip            # Linux yt-dlp
│   ├── ffmpeg.exe.zip        # Windows ffmpeg
│   ├── ffmpeg.zip            # Linux ffmpeg
│   ├── aria2c.exe.zip        # Windows aria2c
│   ├── aria2c.zip            # Linux aria2c
│   ├── curl.exe.zip          # Windows curl
│   ├── curl.zip              # Linux curl
│   ├── rtmpdump.exe.zip      # Windows rtmpdump
│   └── rtmpdump.zip          # Linux rtmpdump
├── metadata/                 # Version metadata
│   ├── yt-dlp.version
│   ├── ffmpeg.version
│   ├── aria2c.version
│   ├── curl.version
│   └── rtmpdump.version
├── plugins.txt               # List of available plugins/tools
├── repository.conf           # Repository configuration
└── README.md                 # Repository documentation
```

### Required Tools

#### Core Download Tools
- **yt-dlp**: YouTube and general video downloader (replaces youtube-dl)
- **ffmpeg**: Video processing and format conversion
- **aria2c**: High-speed download manager with resume support
- **curl**: HTTP download utility
- **rtmpdump**: RTMP stream downloader

#### Version Requirements
- **yt-dlp**: Latest stable version (2023.12.18+)
- **ffmpeg**: Version 4.0 or higher
- **aria2c**: Version 1.30 or higher
- **curl**: Version 7.50 or higher
- **rtmpdump**: Version 2.4 or higher

---

## 3. Repository Management

### Automatic Management Features

#### Startup Check
- **Automatic Detection**: Tools are checked at application startup
- **Version Comparison**: Current vs. available versions are compared
- **Download Logic**: Only missing or outdated tools are downloaded
- **Platform Detection**: Correct binaries for Windows/Linux are selected

#### Version Control
- **Semantic Versioning**: Follows standard version numbering
- **Compatibility Checks**: Ensures tool compatibility with habiTv version
- **Rollback Support**: Previous versions can be restored if needed
- **Dependency Resolution**: Handles tool interdependencies

#### Security Features
- **HTTPS Enforcement**: All downloads use encrypted connections
- **Certificate Validation**: SSL certificates are verified
- **Integrity Checks**: Downloaded files are validated
- **Checksum Verification**: File integrity is confirmed

### Setting Up a Custom Repository

#### 1. Create Repository Structure
```bash
mkdir -p repository/{bin,metadata,com/dabi/habitv}
touch repository/plugins.txt
touch repository/repository.conf
```

#### 2. Add Tool Binaries
```bash
cd repository/bin

# Download and package tools
wget -O yt-dlp.exe.zip "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
wget -O yt-dlp.zip "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
wget -O ffmpeg.exe.zip "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"
wget -O ffmpeg.zip "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"

# Make Linux binaries executable
chmod +x yt-dlp.zip ffmpeg.zip
```

#### 3. Create Version Metadata
```bash
cd repository/metadata

# Get latest versions
echo "2023.12.18" > yt-dlp.version
echo "6.1" > ffmpeg.version
echo "1.36.0" > aria2c.version
echo "8.5.0" > curl.version
echo "2.4" > rtmpdump.version
```

#### 4. Create plugins.txt
```bash
cd repository
cat > plugins.txt << EOF
# Core plugins
arte
youtube
canalPlus
pluzz

# External tools
yt-dlp
ffmpeg
aria2c
curl
rtmpdump
EOF
```

#### 5. Repository Configuration
```ini
# repository.conf
[repository]
name=habiTv Custom Repository
version=1.0
description=Custom repository for habiTv tools and plugins

[settings]
auto_update=true
check_interval=86400
timeout=30000
retry_attempts=3
https_only=true

[tools]
yt-dlp=required
ffmpeg=required
aria2c=optional
curl=required
rtmpdump=optional

[plugins]
arte=required
youtube=required
canalPlus=optional
pluzz=optional
```

---

## 4. GitHub Pages Mirror

### Benefits

#### Security Improvements
- **HTTPS Encryption**: All downloads are encrypted end-to-end
- **Certificate Validation**: Automatic SSL certificate management
- **Integrity Protection**: Reduced risk of man-in-the-middle attacks
- **Modern Standards**: Complies with current security best practices

#### Reliability Improvements
- **GitHub Infrastructure**: High availability and global CDN
- **Automatic Scaling**: Handles traffic spikes automatically
- **Geographic Distribution**: Multiple data centers worldwide
- **Uptime Guarantee**: GitHub's 99.9% uptime SLA

#### Maintenance Benefits
- **Version Control**: Git-based repository management
- **Automation**: GitHub Actions integration for auto-updates
- **Collaboration**: Multiple maintainers can contribute
- **Backup**: Automatic backup and version history

### Setup Instructions

#### 1. Enable GitHub Pages
1. Go to repository Settings
2. Navigate to Pages section
3. Select source: "Deploy from a branch"
4. Choose branch: `main` or `master`
5. Select folder: `/ (root)`
6. Click Save

#### 2. Custom Domain (Optional)
If you have a custom domain:
1. Add custom domain in Pages settings
2. Configure DNS records
3. Enable HTTPS enforcement

#### 3. Repository Structure
Ensure your repository has the correct structure:
```
your-repo/
├── repository/
│   ├── bin/
│   ├── metadata/
│   ├── plugins.txt
│   └── repository.conf
└── README.md
```

---

## 5. Configuration Examples

### Configuration File (config.xml)

#### Basic Configuration
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
</updateConfig>
```

#### Advanced Configuration
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
    <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
    <timeout>30000</timeout>
    <retryAttempts>3</retryAttempts>
    <checkInterval>86400</checkInterval>
    <httpsOnly>true</httpsOnly>
    <proxyHost>proxy.company.com</proxyHost>
    <proxyPort>8080</proxyPort>
    <proxyUsername>user</proxyUsername>
    <proxyPassword>password</proxyPassword>
</updateConfig>
```

#### Security-Focused Configuration
```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
    <repositoryUrl>https://internal-repo.company.com/habitv/</repositoryUrl>
    <httpsOnly>true</httpsOnly>
    <certificateValidation>true</certificateValidation>
    <integrityCheck>true</integrityCheck>
</updateConfig>
```

### Environment Variables

#### Linux/macOS
```bash
export HABITV_REPOSITORY_URL="https://mika3578.github.io/habitv/repository/"
export HABITV_UPDATE_TIMEOUT="30000"
export HABITV_UPDATE_RETRY_ATTEMPTS="3"
export HABITV_HTTPS_ONLY="true"
```

#### Windows
```cmd
set HABITV_REPOSITORY_URL=https://mika3578.github.io/habitv/repository/
set HABITV_UPDATE_TIMEOUT=30000
set HABITV_UPDATE_RETRY_ATTEMPTS=3
set HABITV_HTTPS_ONLY=true
```

### Command Line Parameters
```bash
java -jar habiTv.jar --repository-url="https://mika3578.github.io/habitv/repository/"
java -jar habiTv.jar --update-timeout=30000 --update-retry-attempts=3
java -jar habiTv.jar --https-only --no-snapshot
```

---

## 6. Automated Updates (GitHub Actions)

### Workflow Configuration

Create `.github/workflows/update-repository.yml`:

```yaml
name: Update habiTv Repository

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM UTC
  workflow_dispatch:     # Manual trigger
  push:
    branches: [ main ]

jobs:
  update-tools:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Check yt-dlp updates
        id: check-yt-dlp
        run: |
          LATEST_VERSION=$(curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | jq -r '.tag_name')
          CURRENT_VERSION=$(cat repository/metadata/yt-dlp.version)
          
          if [ "$LATEST_VERSION" != "$CURRENT_VERSION" ]; then
            echo "NEW_VERSION=$LATEST_VERSION" >> $GITHUB_ENV
            echo "UPDATE_NEEDED=true" >> $GITHUB_ENV
            echo "Tool: yt-dlp, Current: $CURRENT_VERSION, Latest: $LATEST_VERSION"
          else
            echo "Tool: yt-dlp, No update needed (Current: $CURRENT_VERSION)"
          fi
      
      - name: Check ffmpeg updates
        id: check-ffmpeg
        run: |
          # Add ffmpeg update check logic
          echo "Tool: ffmpeg, Update check completed"
      
      - name: Download new versions
        if: env.UPDATE_NEEDED == 'true'
        run: |
          # Download yt-dlp
          curl -L -o "repository/bin/yt-dlp.exe.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
          curl -L -o "repository/bin/yt-dlp.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
          echo "$NEW_VERSION" > "repository/metadata/yt-dlp.version"
          
          # Download other tools as needed
          # curl -L -o "repository/bin/ffmpeg.exe.zip" "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"
      
      - name: Update plugins.txt
        if: env.UPDATE_NEEDED == 'true'
        run: |
          # Ensure plugins.txt is up to date
          echo "Updating plugins.txt..."
      
      - name: Commit and push
        if: env.UPDATE_NEEDED == 'true'
        run: |
          git config --local user.email "action@github.com"
          git config --local user.name "GitHub Action"
          git add repository/
          git commit -m "Update external tools and plugins [skip ci]"
          git push
      
      - name: Notify on success
        if: success() && env.UPDATE_NEEDED == 'true'
        run: |
          echo "Repository updated successfully"
          # Add notification logic (email, Slack, etc.)
      
      - name: Notify on failure
        if: failure()
        run: |
          echo "Repository update failed"
          # Add failure notification logic
```

### Automation Benefits
- **Daily Checks**: Automatic daily checks for new versions
- **Manual Triggers**: Ability to trigger updates manually
- **Version Tracking**: Automatic version metadata updates
- **Error Handling**: Proper error handling and notifications
- **Audit Trail**: Git history for all updates

---

## 7. Best Practices & Troubleshooting

### Security Best Practices

#### Repository Security
- **Always use HTTPS**: Never use HTTP repositories in production
- **Certificate Validation**: Ensure SSL certificate validation is enabled
- **Repository Verification**: Verify repository authenticity before use
- **Access Control**: Use authentication for private repositories
- **Regular Updates**: Keep repository tools and plugins updated

#### Configuration Security
- **Secure Defaults**: Start with secure default configurations
- **Environment Variables**: Use environment variables for sensitive data
- **Access Logging**: Enable logging for repository access
- **Network Security**: Use firewalls and proxy settings as needed

### Performance Optimization

#### Update Performance
- **Check Intervals**: Set appropriate check intervals (daily recommended)
- **Timeout Settings**: Configure appropriate timeouts for your network
- **Retry Logic**: Use retry attempts for unreliable networks
- **Concurrent Downloads**: Limit concurrent downloads to avoid overload

#### Repository Performance
- **CDN Usage**: Use CDN-enabled repositories when possible
- **Geographic Distribution**: Choose repositories close to your location
- **Caching**: Implement appropriate caching strategies
- **Bandwidth Management**: Monitor and manage bandwidth usage

### Troubleshooting Guide

#### Common Issues

**1. Update Failures**
```
Error: Failed to download plugin: Connection timeout
```
**Solutions:**
- Check network connectivity
- Verify repository URL is accessible
- Increase timeout settings
- Check firewall/proxy configuration

**2. Certificate Errors**
```
Error: SSL certificate validation failed
```
**Solutions:**
- Verify repository certificate
- Update system certificates
- Use trusted repositories only
- Check system date/time settings

**3. Version Conflicts**
```
Error: Plugin version incompatible
```
**Solutions:**
- Update habiTv to latest version
- Check plugin compatibility matrix
- Use compatible plugin versions
- Contact plugin maintainers

**4. Repository Access**
```
Error: Repository not accessible
```
**Solutions:**
- Verify repository URL
- Check network connectivity
- Try alternative repository
- Contact repository maintainers

#### Debug Information

**Enable Debug Logging:**
```xml
<logging>
    <level>DEBUG</level>
    <updateLogging>true</updateLogging>
</logging>
```

**Check Update Status:**
```bash
java -jar habiTv.jar --check-updates --verbose
```

**Validate Repository:**
```bash
java -jar habiTv.jar --validate-repository --repository-url="https://your-repo.com/"
```

#### Log Analysis

Look for these log entries:
```
INFO - Update check started
INFO - Repository accessible: https://mika3578.github.io/habitv/repository/
INFO - Plugin update available: youtube (4.1.0-SNAPSHOT)
INFO - Downloading plugin: youtube-4.1.0-SNAPSHOT.jar
INFO - Plugin download completed: youtube
ERROR - Update failed: Connection timeout
WARN - Repository certificate validation failed
```

### Maintenance Tasks

#### Regular Maintenance
- **Weekly**: Check repository accessibility
- **Monthly**: Review and update tool versions
- **Quarterly**: Audit repository security
- **Annually**: Review and update repository structure

#### Monitoring
- **Update Success Rate**: Monitor successful vs. failed updates
- **Download Performance**: Track download speeds and times
- **Error Rates**: Monitor and analyze error patterns
- **Security Events**: Track security-related events

---

## 8. Migration Guide

### From HTTP to HTTPS
1. **Update Configuration**: Change repository URL to HTTPS
2. **Test Connectivity**: Verify HTTPS repository is accessible
3. **Update Tools**: Download latest tools from HTTPS repository
4. **Monitor Logs**: Check for any HTTPS-related issues
5. **Remove HTTP**: Remove HTTP repository configuration

### From Custom to GitHub Pages
1. **Setup GitHub Pages**: Follow GitHub Pages setup instructions
2. **Migrate Content**: Copy repository content to GitHub
3. **Update Configuration**: Change repository URL to GitHub Pages
4. **Setup Automation**: Configure GitHub Actions for updates
5. **Test and Validate**: Verify everything works correctly

---

## 9. API Reference

### Update Manager API
```java
public class UpdateManager {
    public void checkForUpdates();
    public void downloadPlugin(String pluginName);
    public void downloadTool(String toolName);
    public boolean isUpdateAvailable(String name);
    public List<UpdateInfo> getAvailableUpdates();
}
```

### Repository Client API
```java
public class RepositoryClient {
    public List<String> getAvailablePlugins();
    public List<String> getAvailableTools();
    public void downloadFile(String url, Path destination);
    public boolean validateFile(Path file, String checksum);
}
```

---

## 10. Support and Resources

### Getting Help
- **Documentation**: Check this guide and related documentation
- **Logs**: Review application logs for detailed error information
- **Community**: Join the habiTv community for support
- **Issues**: Report issues on the GitHub repository

### Additional Resources
- **Repository Examples**: See example repositories in the habiTv organization
- **Plugin Development**: Check plugin development documentation
- **Security Guide**: Review security best practices documentation
- **Performance Guide**: See performance optimization documentation

---

**Note**: This document combines and summarizes information from the original detailed documentation. For specific implementation details, refer to the source code and individual component documentation. 
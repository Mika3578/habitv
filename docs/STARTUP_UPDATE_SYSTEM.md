# habiTv Startup Update System

This document describes the automatic update system that runs when habiTv starts up.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 14, 2025

## Overview

habiTv includes an automatic update system that checks for and downloads plugin updates when the application starts. This system ensures users always have the latest versions of plugins without manual intervention.

## Update System Architecture

### Components

#### 1. Update Manager

- **Class**: `com.dabi.habitv.framework.plugin.api.update.impl.BaseUpdatablePluginManager`
- **Purpose**: Manages plugin updates and version checking
- **Location**: Framework module

#### 2. Repository Client

- **Class**: `com.dabi.habitv.framework.plugin.api.update.impl.RepositoryClient`
- **Purpose**: Downloads plugins from the repository
- **Location**: Framework module

#### 3. Plugin Loader

- **Class**: `com.dabi.habitv.framework.plugin.api.PluginLoader`
- **Purpose**: Loads and initializes plugins
- **Location**: Framework module

### Update Flow

```java
// Simplified update flow
public class StartupUpdateManager {
    
    public void performStartupUpdates() {
        // 1. Load current plugins
        List<PluginInfo> currentPlugins = loadCurrentPlugins();
        
        // 2. Check repository for updates
        List<PluginInfo> availablePlugins = checkRepository();
        
        // 3. Compare versions
        List<PluginInfo> updatesNeeded = findUpdates(currentPlugins, availablePlugins);
        
        // 4. Download updates
        for (PluginInfo plugin : updatesNeeded) {
            downloadPlugin(plugin);
        }
        
        // 5. Reload plugins
        reloadPlugins();
    }
}
```

## Repository Information

### Repository URL

```
http://dabiboo.free.fr/repository
```

### Repository Structure

```text
http://dabiboo.free.fr/repository/
├── plugins.txt                              # List of all available plugins
└── com/
    └── dabi/
        └── habitv/
            ├── arte/
            │   └── 4.1.0-SNAPSHOT/
            │       └── arte-4.1.0-SNAPSHOT.jar
            ├── canalPlus/
            │   └── 4.1.0-SNAPSHOT/
            │       └── canalPlus-4.1.0-SNAPSHOT.jar
            ├── pluzz/
            │   └── 4.1.0-SNAPSHOT/
            │       └── pluzz-4.1.0-SNAPSHOT.jar
            └── ffmpeg/
                └── 4.1.0-SNAPSHOT/
                    └── ffmpeg-4.1.0-SNAPSHOT.jar
```

### plugins.txt Format

```text
arte
canalPlus
pluzz
ffmpeg
```

## Update Process

### 1. Startup Trigger

#### When Updates Run

- **Application Startup**: Every time habiTv starts
- **Plugin Loading**: Before plugins are loaded
- **Background Process**: Non-blocking update check

#### Update Conditions

```java
public class UpdateConditions {
    
    public boolean shouldCheckForUpdates() {
        // Check if updates are enabled
        if (!isUpdateEnabled()) {
            return false;
        }
        
        // Check if enough time has passed since last check
        if (!isTimeToCheck()) {
            return false;
        }
        
        // Check network connectivity
        if (!isNetworkAvailable()) {
            return false;
        }
        
        return true;
    }
}
```

### 2. Repository Check

#### Fetching Plugin List

```java
public class RepositoryClient {
    
    public List<String> getAvailablePlugins() throws IOException {
        String pluginsUrl = "http://dabiboo.free.fr/repository/plugins.txt";
        
        // Download plugins.txt
        String content = HttpUtils.get(pluginsUrl);
        
        // Parse plugin names
        List<String> plugins = new ArrayList<>();
        for (String line : content.split("\n")) {
            String plugin = line.trim();
            if (!plugin.isEmpty() && !plugin.startsWith("#")) {
                plugins.add(plugin);
            }
        }
        
        return plugins;
    }
}
```

#### Version Comparison

```java
public class VersionComparator {
    
    public boolean isUpdateAvailable(String currentVersion, String availableVersion) {
        // Parse version strings
        Version current = parseVersion(currentVersion);
        Version available = parseVersion(availableVersion);
        
        // Compare versions
        return available.isNewerThan(current);
    }
    
    private Version parseVersion(String versionString) {
        // Handle different version formats
        if (versionString.contains("-SNAPSHOT")) {
            return new SnapshotVersion(versionString);
        } else {
            return new ReleaseVersion(versionString);
        }
    }
}
```

### 3. Plugin Download

#### Download Process

```java
public class PluginDownloader {
    
    public void downloadPlugin(String pluginName, String version) throws IOException {
        // Construct download URL
        String downloadUrl = String.format(
            "http://dabiboo.free.fr/repository/com/dabi/habitv/%s/%s/%s-%s.jar",
            pluginName, version, pluginName, version
        );
        
        // Download to temporary location
        String tempFile = downloadToTemp(downloadUrl);
        
        // Verify download
        if (!verifyDownload(tempFile)) {
            throw new IOException("Download verification failed");
        }
        
        // Move to plugins directory
        moveToPluginsDirectory(tempFile, pluginName, version);
    }
}
```

#### Download Verification

```java
public class DownloadVerifier {
    
    public boolean verifyDownload(String filePath) {
        try {
            // Check file size
            long fileSize = Files.size(Paths.get(filePath));
            if (fileSize < 1000) { // Minimum size check
                return false;
            }
            
            // Check if it's a valid JAR
            try (JarFile jar = new JarFile(filePath)) {
                // Check for required files
                return jar.getEntry("META-INF/MANIFEST.MF") != null;
            }
            
        } catch (IOException e) {
            return false;
        }
    }
}
```

### 4. Plugin Loading

#### Loading Process

```java
public class PluginLoader {
    
    public void loadPlugins() {
        // Get plugins directory
        File pluginsDir = new File(getPluginsDirectory());
        
        // Load each JAR file
        for (File jarFile : pluginsDir.listFiles(f -> f.getName().endsWith(".jar"))) {
            try {
                loadPlugin(jarFile);
            } catch (Exception e) {
                LOG.error("Failed to load plugin: " + jarFile.getName(), e);
            }
        }
    }
    
    private void loadPlugin(File jarFile) throws Exception {
        // Add JAR to classpath
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarFile.toURI().toURL()},
            getClass().getClassLoader()
        );
        
        // Find plugin classes
        List<Class<?>> pluginClasses = findPluginClasses(jarFile, classLoader);
        
        // Instantiate plugins
        for (Class<?> pluginClass : pluginClasses) {
            instantiatePlugin(pluginClass);
        }
    }
}
```

## Configuration

### Environment Variables

#### Update Configuration

```properties
# Enable/disable updates
HABITV_UPDATE_ON_STARTUP=true              # Check for updates on startup
HABITV_AUTORISE_SNAPSHOT=false             # Allow snapshot updates

# Repository configuration
HABITV_REPOSITORY_URL=http://dabiboo.free.fr/repository
HABITV_REPOSITORY_TIMEOUT=30000            # Timeout in milliseconds

# Update frequency
HABITV_UPDATE_CHECK_INTERVAL=86400         # Check interval in seconds (24 hours)
HABITV_UPDATE_RETRY_ATTEMPTS=3             # Number of retry attempts

# Network configuration
HABITV_UPDATE_PROXY_HOST=                  # Proxy host (optional)
HABITV_UPDATE_PROXY_PORT=                  # Proxy port (optional)
HABITV_UPDATE_PROXY_USER=                  # Proxy username (optional)
HABITV_UPDATE_PROXY_PASSWORD=              # Proxy password (optional)
```

### Configuration Files

#### config.xml Settings

```xml
<configuration>
    <update>
        <enabled>true</enabled>
        <checkOnStartup>true</checkOnStartup>
        <allowSnapshots>false</allowSnapshots>
        <repositoryUrl>http://dabiboo.free.fr/repository</repositoryUrl>
        <timeout>30000</timeout>
        <retryAttempts>3</retryAttempts>
        <checkInterval>86400</checkInterval>
    </update>
    
    <proxy>
        <host></host>
        <port></port>
        <username></username>
        <password></password>
    </proxy>
</configuration>
```

## Directory Structure

### Plugin Directories

#### Default Locations

```text
# Windows
C:\Users\{username}\habitv\plugins\

# Linux/Mac
~/habitv/plugins/

# Application directory
./plugins/
```

#### Directory Contents

```text
plugins/
├── arte-4.1.0-SNAPSHOT.jar
├── canalPlus-4.1.0-SNAPSHOT.jar
├── pluzz-4.1.0-SNAPSHOT.jar
├── ffmpeg-4.1.0-SNAPSHOT.jar
└── cache/                                  # Temporary files
    ├── download/
    └── temp/
```

### Cache Directories

#### Temporary Files

```text
cache/
├── download/                               # Downloaded files
│   ├── arte-4.1.0-SNAPSHOT.jar.tmp
│   └── canalPlus-4.1.0-SNAPSHOT.jar.tmp
├── temp/                                   # Temporary processing
│   └── plugin-extract/
└── metadata/                               # Update metadata
    ├── last-check.txt
    ├── plugin-versions.txt
    └── update-log.txt
```

## Key Classes

### BaseUpdatablePluginManager

#### Purpose

Base class for plugins that support automatic updates.

#### Key Methods

```java
public abstract class BaseUpdatablePluginManager<C, E> {
    
    /**
     * Gets the plugin name
     */
    public abstract String getName();
    
    /**
     * Gets the current plugin version
     */
    public String getVersion() {
        return "4.1.0-SNAPSHOT";
    }
    
    /**
     * Checks if an update is available
     */
    public boolean isUpdateAvailable() {
        // Implementation checks repository
    }
    
    /**
     * Downloads the latest version
     */
    public void downloadUpdate() {
        // Implementation downloads from repository
    }
}
```

### RepositoryClient

#### Purpose

Handles communication with the plugin repository.

#### Key Methods

```java
public class RepositoryClient {
    
    /**
     * Gets list of available plugins
     */
    public List<String> getAvailablePlugins() throws IOException;
    
    /**
     * Gets plugin version information
     */
    public PluginVersionInfo getPluginVersion(String pluginName) throws IOException;
    
    /**
     * Downloads a plugin
     */
    public void downloadPlugin(String pluginName, String version, File outputFile) throws IOException;
    
    /**
     * Verifies plugin integrity
     */
    public boolean verifyPlugin(File pluginFile, String expectedChecksum);
}
```

### PluginLoader

#### Purpose

Loads and manages plugin instances.

#### Key Methods

```java
public class PluginLoader {
    
    /**
     * Loads all available plugins
     */
    public void loadAllPlugins();
    
    /**
     * Loads a specific plugin
     */
    public <T> T loadPlugin(String className, Class<T> type);
    
    /**
     * Gets loaded plugins
     */
    public <T> List<T> getLoadedPlugins(Class<T> type);
    
    /**
     * Reloads a plugin
     */
    public void reloadPlugin(String pluginName);
}
```

## Events and Notifications

### Update Events

#### Event Types

```java
public enum UpdateEventType {
    UPDATE_CHECK_STARTED,
    UPDATE_CHECK_COMPLETED,
    UPDATE_AVAILABLE,
    UPDATE_DOWNLOAD_STARTED,
    UPDATE_DOWNLOAD_COMPLETED,
    UPDATE_DOWNLOAD_FAILED,
    PLUGIN_LOADED,
    PLUGIN_LOAD_FAILED
}
```

#### Event Handling

```java
public class UpdateEventListener {
    
    public void onUpdateEvent(UpdateEvent event) {
        switch (event.getType()) {
            case UPDATE_AVAILABLE:
                LOG.info("Update available for plugin: " + event.getPluginName());
                break;
                
            case UPDATE_DOWNLOAD_COMPLETED:
                LOG.info("Update downloaded for plugin: " + event.getPluginName());
                break;
                
            case UPDATE_DOWNLOAD_FAILED:
                LOG.error("Update failed for plugin: " + event.getPluginName(), event.getException());
                break;
        }
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Updates Not Working

**Symptoms:**

- No plugin updates downloaded
- Old plugin versions still in use
- No update check messages in logs

**Solutions:**

```bash
# Check configuration
grep -i update config.xml

# Check network connectivity
curl -I http://dabiboo.free.fr/repository/plugins.txt

# Check logs
tail -f habiTv.log | grep -i update

# Force update check
java -jar habiTv.jar --force-update
```

#### 2. Download Failures

**Symptoms:**

- Plugin downloads fail
- Incomplete or corrupted files
- Network timeout errors

**Solutions:**

```bash
# Check network connectivity
ping dabiboo.free.fr

# Check proxy settings
echo $HTTP_PROXY
echo $HTTPS_PROXY

# Clear cache
rm -rf plugins/cache/

# Increase timeout
export HABITV_REPOSITORY_TIMEOUT=60000
```

#### 3. Plugin Loading Issues

**Symptoms:**

- Plugins not loading after update
- Class not found errors
- Plugin initialization failures

**Solutions:**

```bash
# Check JAR files
jar tf plugins/arte-4.1.0-SNAPSHOT.jar

# Check classpath
java -cp "plugins/*" -jar habiTv.jar

# Remove corrupted plugins
rm plugins/corrupted-plugin.jar

# Restart application
java -jar habiTv.jar
```

### Debug Mode

#### Enable Debug Logging

```xml
<!-- In log4j.properties -->
log4j.logger.com.dabi.habitv.framework.plugin.api.update=DEBUG
log4j.logger.com.dabi.habitv.framework.plugin.api.PluginLoader=DEBUG
```

#### Debug Commands

```bash
# Check update status
java -jar habiTv.jar --debug-updates

# List available plugins
java -jar habiTv.jar --list-available-plugins

# Check plugin versions
java -jar habiTv.jar --check-versions

# Force update check
java -jar habiTv.jar --force-update-check
```

## Best Practices

### 1. For Users

#### Update Management

- **Keep Updates Enabled**: Allow automatic updates
- **Monitor Logs**: Check for update-related messages
- **Report Issues**: Report update problems
- **Backup Configuration**: Backup before major updates

#### Network Configuration

- **Use Reliable Connection**: Ensure stable internet
- **Configure Proxy**: If behind corporate firewall
- **Monitor Bandwidth**: Updates can use significant bandwidth
- **Check Firewall**: Ensure repository access allowed

### 2. For Developers

#### Plugin Development

```java
// Implement proper version checking
public class MyPlugin extends BaseUpdatablePluginManager<CategoryDTO, EpisodeDTO> {
    
    @Override
    public String getVersion() {
        return "1.0.0"; // Use semantic versioning
    }
    
    @Override
    public boolean isUpdateAvailable() {
        // Implement version comparison logic
        return super.isUpdateAvailable();
    }
}
```

#### Update Testing

- **Test Update Process**: Verify updates work correctly
- **Version Compatibility**: Ensure backward compatibility
- **Error Handling**: Handle update failures gracefully
- **Logging**: Add appropriate log messages

### 3. For Administrators

#### System Configuration

```bash
# Configure update directory permissions
chmod 755 ~/habitv/plugins/
chmod 644 ~/habitv/plugins/*.jar

# Configure network access
iptables -A OUTPUT -p tcp --dport 80 -d dabiboo.free.fr -j ACCEPT

# Configure proxy if needed
export HTTP_PROXY=http://proxy.company.com:8080
```

#### Monitoring

- **Monitor Update Logs**: Watch for update issues
- **Check Disk Space**: Ensure sufficient space for updates
- **Monitor Network**: Watch for excessive bandwidth usage
- **Backup Plugins**: Regular backup of plugin directory

## Security Considerations

### Repository Security

#### Current Security Model

- **HTTP Only**: Repository uses plain HTTP
- **No Authentication**: No authentication for downloads
- **No Signing**: Plugins not digitally signed
- **Public Access**: Repository publicly accessible

#### Security Risks

- **Man-in-the-Middle**: Potential for interception
- **DNS Spoofing**: Potential for DNS-based attacks
- **Code Injection**: Potential for malicious code
- **Data Tampering**: Potential for data modification

#### Mitigation Strategies

```properties
# Security configuration
HABITV_VERIFY_DOWNLOADS=true               # Verify download integrity
HABITV_CHECK_PLUGIN_SIGNATURES=false       # Check plugin signatures (future)
HABITV_TRUSTED_REPOSITORIES=http://dabiboo.free.fr/repository
HABITV_DOWNLOAD_TIMEOUT=30000              # Download timeout
```

### Future Security Enhancements

#### Planned Improvements

1. **HTTPS Repository**: Secure repository access
2. **Plugin Signing**: Digital signatures for plugins
3. **Certificate Pinning**: Certificate verification
4. **Checksum Verification**: File integrity checking

## External Tool Management

### Overview

The startup update system also manages external tools (binaries) required by habiTv plugins. These tools are automatically downloaded, updated, and maintained by the system.

### Supported External Tools

| Tool | Purpose | Windows Binary | Linux Binary | Update Method |
|------|---------|----------------|--------------|---------------|
| yt-dlp | YouTube and video platform downloads | yt-dlp.exe | yt-dlp | Version check + download |
| ffmpeg | Video processing and conversion | ffmpeg.exe | ffmpeg | Version check + download |
| aria2c | High-speed downloads | aria2c.exe | aria2c | Version check + download |
| rtmpdump | RTMP stream downloads | rtmpdump.exe | rtmpdump | Version check + download |
| curl | HTTP downloads | curl.exe | curl | Version check + download |

### Update Process for External Tools

#### 1. **Startup Check**
```java
// Each updatable plugin checks its binary at startup
public void update(Publisher<UpdatablePluginEvent> updatePublisher, DownloaderPluginHolder downloaders) {
    new ZipExeUpdater(this, downloaders.getBinDir(), FrameworkConf.GROUP_ID, false, updatePublisher, downloaders)
        .update(getFilesToUpdate());
}
```

#### 2. **Version Detection**
```java
// Version is detected by running the binary with --version or -v
public String getCurrentVersion(DownloaderPluginHolder downloaders) {
    String versionOutput = callGetVersionCmd(downloaders);
    Pattern versionPattern = getVersionPattern();
    Matcher matcher = versionPattern.matcher(versionOutput);
    return matcher.find() ? matcher.group(matcher.groupCount()) : null;
}
```

#### 3. **Repository Comparison**
- System checks repository for newer versions
- Compares local version with repository version
- Downloads if newer version is available

#### 4. **Download and Installation**
- Downloads ZIP file from repository
- Extracts binary to bin/ directory
- Sets appropriate permissions (Linux)
- Verifies installation

### Repository Structure for External Tools

```
repository/
├── bin/
│   ├── yt-dlp.exe.zip          # Windows yt-dlp binary
│   ├── yt-dlp.zip              # Linux yt-dlp binary
│   ├── ffmpeg.exe.zip          # Windows ffmpeg binary
│   ├── ffmpeg.zip              # Linux ffmpeg binary
│   ├── aria2c.exe.zip          # Windows aria2c binary
│   ├── aria2c.zip              # Linux aria2c binary
│   ├── rtmpdump.exe.zip        # Windows rtmpdump binary
│   ├── rtmpdump.zip            # Linux rtmpdump binary
│   ├── curl.exe.zip            # Windows curl binary
│   └── curl.zip                # Linux curl binary
├── plugins.txt                 # Plugin list
└── metadata/                   # Version metadata
    ├── yt-dlp.version
    ├── ffmpeg.version
    └── ...
```

### Tool Packaging Guidelines

#### Windows Binaries
- Package as ZIP files with `.exe.zip` extension
- Include the main executable and any dependencies
- Ensure the executable is in the root of the ZIP
- Example: `yt-dlp.exe.zip` contains `yt-dlp.exe`

#### Linux Binaries
- Package as ZIP files with `.zip` extension
- Include the main executable and any dependencies
- Ensure the executable is in the root of the ZIP
- Example: `yt-dlp.zip` contains `yt-dlp`

#### Version Files
Create version files in the metadata directory:
```
metadata/yt-dlp.version
2025.06.09

metadata/ffmpeg.version
6.1.1
```

### Configuration Options

#### Repository URL
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>https://your-repo.com/habitv-tools</repositoryUrl>
</updateConfig>
```

#### Disable Auto-Updates
```xml
<updateConfig>
    <updateOnStartup>false</updateOnStartup>
    <autoriseSnapshot>false</autoriseSnapshot>
</updateConfig>
```

### Event Notifications

External tool updates generate events:

```java
// Update started
new UpdatablePluginEvent("yt-dlp", "2025.06.09", UpdatablePluginStateEnum.DOWNLOADING)

// Update completed
new UpdatablePluginEvent("yt-dlp", "2025.06.09", UpdatablePluginStateEnum.DONE)

// Update failed
new UpdatablePluginEvent("yt-dlp", "2025.06.09", UpdatablePluginStateEnum.ERROR)
```

### Troubleshooting External Tools

#### Common Issues

1. **Binary Not Found**
   ```
   ERROR - Tool yt-dlp not found in bin/ directory
   ```
   - Check if binary exists in bin/
   - Verify download permissions
   - Check repository availability

2. **Version Check Failed**
   ```
   ERROR - Failed to get version for yt-dlp
   ```
   - Binary may be corrupted
   - Permission issues (Linux)
   - Binary not executable

3. **Download Failed**
   ```
   ERROR - Failed to download yt-dlp from repository
   ```
   - Network connectivity issues
   - Repository unavailable
   - Firewall blocking downloads

4. **Extraction Failed**
   ```
   ERROR - Failed to extract yt-dlp.zip
   ```
   - Corrupted download
   - Insufficient disk space
   - Permission issues

#### Debug Commands

```bash
# Check if binary exists and is executable
ls -la bin/yt-dlp*

# Test binary manually
bin/yt-dlp --version

# Check download directory permissions
ls -la bin/

# Verify ZIP file integrity
unzip -t bin/yt-dlp.zip
```

### Security Considerations

#### Repository Security
- Use HTTPS repositories when possible
- Verify repository authenticity
- Monitor for suspicious updates

#### Binary Verification
- Check file checksums when available
- Verify binary signatures
- Monitor for unexpected behavior

#### Network Security
- Use corporate proxies if required
- Configure firewall rules appropriately
- Monitor network traffic

### Best Practices

#### Repository Management
1. **Regular Updates**: Keep tool versions current
2. **Testing**: Test new versions before deployment
3. **Backup**: Maintain previous versions for rollback
4. **Monitoring**: Monitor download statistics and errors

#### Binary Management
1. **Source Verification**: Download from official sources
2. **Version Tracking**: Maintain version history
3. **Dependency Management**: Include all required dependencies
4. **Cross-Platform**: Provide both Windows and Linux versions

#### User Experience
1. **Transparent Updates**: Inform users of updates
2. **Fallback Options**: Provide manual installation instructions
3. **Error Handling**: Clear error messages and solutions
4. **Progress Indication**: Show download and update progress

This document provides comprehensive information about the habiTv startup update system. Understanding this system is crucial for both users and developers working with habiTv plugins.

## Repository Management Challenges

### Manual Update Problem

The current repository-based approach has a significant limitation: **manual update requirements**.

#### Current Process Issues

```
Repository Update Process:
1. New tool version released (e.g., yt-dlp 2025.06.10)
2. Repository maintainer must manually download new version
3. Repository maintainer must manually upload to repository
4. Repository maintainer must manually update version metadata
5. Users receive the update (delayed by manual steps)
```

#### Problems with Manual Updates

- **Delayed Updates**: Users don't get latest versions immediately
- **Maintenance Burden**: Someone must manually maintain the repository
- **Human Error**: Risk of uploading wrong versions or corrupted files
- **Dependency**: Repository availability depends on manual intervention
- **Inconsistency**: Updates may be missed or delayed

#### Impact on Users

- Tools may be outdated for days or weeks
- Security updates are delayed
- New features and bug fixes are not immediately available
- Dependency on repository maintainer availability

### Automated Solutions

#### 1. GitHub Actions Workflow

Automate repository updates using GitHub Actions:

```yaml
# .github/workflows/update-tools.yml
name: Update External Tools

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM
  workflow_dispatch:     # Manual trigger

jobs:
  update-tools:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Check yt-dlp updates
        run: |
          LATEST_VERSION=$(curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | jq -r '.tag_name')
          CURRENT_VERSION=$(cat metadata/yt-dlp.version)
          
          if [ "$LATEST_VERSION" != "$CURRENT_VERSION" ]; then
            echo "NEW_VERSION=$LATEST_VERSION" >> $GITHUB_ENV
            echo "UPDATE_NEEDED=true" >> $GITHUB_ENV
          fi
      
      - name: Download new versions
        if: env.UPDATE_NEEDED == 'true'
        run: |
          curl -L -o "bin/yt-dlp.exe.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
          curl -L -o "bin/yt-dlp.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
          echo "$NEW_VERSION" > "metadata/yt-dlp.version"
      
      - name: Commit and push
        if: env.UPDATE_NEEDED == 'true'
        run: |
          git config --local user.email "action@github.com"
          git config --local user.name "GitHub Action"
          git add .
          git commit -m "Auto-update yt-dlp to $NEW_VERSION"
          git push
```

#### 2. Automated Script

Create a script that runs periodically:

```bash
#!/bin/bash
# auto-update-repository.sh

# Check for new versions daily
while true; do
    echo "Checking for tool updates..."
    
    # Check yt-dlp
    LATEST_VERSION=$(curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | grep '"tag_name"' | cut -d'"' -f4)
    CURRENT_VERSION=$(cat metadata/yt-dlp.version)
    
    if [ "$LATEST_VERSION" != "$CURRENT_VERSION" ]; then
        echo "New yt-dlp version: $LATEST_VERSION"
        
        # Download new version
        curl -L -o "bin/yt-dlp.exe.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
        curl -L -o "bin/yt-dlp.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
        
        # Update metadata
        echo "$LATEST_VERSION" > "metadata/yt-dlp.version"
        
        # Commit and push to repository
        git add .
        git commit -m "Update yt-dlp to $LATEST_VERSION"
        git push
    fi
    
    # Wait 24 hours
    sleep 86400
done
```

#### 3. Hybrid Approach

Combine repository reliability with direct download freshness:

```java
public class HybridToolManager {
    
    public DownloadResult downloadTool(String toolName, String binDir) {
        // Try repository first (reliable)
        try {
            DownloadResult result = downloadFromRepository(toolName, binDir);
            if (result.isSuccess()) {
                return result;
            }
        } catch (Exception e) {
            log.warn("Repository download failed for " + toolName + ": " + e.getMessage());
        }
        
        // Fallback to direct download (latest)
        try {
            log.info("Attempting direct download for " + toolName);
            return downloadFromOfficialSource(toolName, binDir);
        } catch (Exception e) {
            log.error("Direct download also failed for " + toolName + ": " + e.getMessage());
            return new DownloadResult(false, null, null, e.getMessage(), 0);
        }
    }
}
```

#### 4. Smart Version Checking

Implement intelligent version comparison:

```java
public class SmartToolUpdater {
    
    public boolean shouldUpdate(String toolName, String currentVersion) {
        // Check repository version
        String repoVersion = getRepositoryVersion(toolName);
        
        // Check official source version
        String officialVersion = getOfficialVersion(toolName);
        
        // Use the newer version
        String latestVersion = compareVersions(repoVersion, officialVersion);
        
        return isNewer(latestVersion, currentVersion);
    }
    
    private String getOfficialVersion(String toolName) {
        // Use GitHub API or other methods to get latest version
        switch (toolName) {
            case "yt-dlp":
                return getGitHubLatestVersion("yt-dlp/yt-dlp");
            case "ffmpeg":
                return getFFmpegLatestVersion();
            // ... other tools
        }
    }
}
```

### Recommended Solutions

#### For Repository Maintainers

1. **Automated Updates (Recommended)**
   - Set up GitHub Actions for automatic daily checks
   - No manual intervention required
   - Consistent and reliable updates

2. **Scheduled Scripts**
   - Run update scripts on a schedule (cron jobs)
   - Monitor and alert on failures
   - Maintain update logs

#### For Users

1. **Hybrid System**
   - Repository as primary source (reliable)
   - Direct download as fallback (latest)
   - Best of both worlds

2. **Smart Updating**
   - habiTv checks both sources
   - Downloads newest available version
   - Maintains reliability

### Implementation Priority

1. **Immediate**: Set up automated repository updates
2. **Short-term**: Implement hybrid fallback system
3. **Long-term**: Smart version checking and auto-selection

### Benefits of Automation

- ✅ **No Manual Work**: Updates happen automatically
- ✅ **Faster Updates**: Users get latest versions quickly
- ✅ **Consistency**: Regular, predictable updates
- ✅ **Reliability**: Reduced human error
- ✅ **Scalability**: Works for multiple tools and platforms

### Monitoring and Alerts

Set up monitoring for automated updates:

```bash
# Check update status
curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | jq '.tag_name'

# Monitor repository health
curl -I http://dabiboo.free.fr/repository/bin/yt-dlp.exe.zip

# Alert on failures
if [ $? -ne 0 ]; then
    echo "Repository update failed" | mail -s "habiTv Update Alert" admin@example.com
fi
```

This automated approach ensures that habiTv users always have access to the latest tool versions without requiring manual repository maintenance. 
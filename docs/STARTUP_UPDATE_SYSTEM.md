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

This document provides comprehensive information about the habiTv startup update system. Understanding this system is crucial for both users and developers working with habiTv plugins. 
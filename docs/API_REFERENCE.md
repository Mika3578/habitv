# habiTv API Reference

This document provides a complete reference for the habiTv API.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Plugin API

### Core Interfaces

#### PluginProviderInterface

Content provider plugin interface for discovering and listing available content.

```java
public interface PluginProviderInterface {
    /**
     * Returns the name of the plugin.
     * @return plugin name
     */
    String getName();
    
    /**
     * Discovers available categories.
     * @return set of available categories
     * @throws TechnicalException if discovery fails
     */
    Set<CategoryDTO> findCategory() throws TechnicalException;
    
    /**
     * Finds episodes for a specific category.
     * @param category the category to search
     * @return set of episodes in the category
     * @throws TechnicalException if search fails
     */
    Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException;
}
```

#### PluginDownloaderInterface

Downloader plugin interface for handling file downloads.

```java
public interface PluginDownloaderInterface {
    /**
     * Returns the name of the downloader.
     * @return downloader name
     */
    String getName();
    
    /**
     * Downloads content using the specified parameters.
     * @param downloadParam download parameters
     * @param downloaders available downloader plugins
     * @throws DownloadFailedException if download fails
     */
    void download(DownloadParamDTO downloadParam, 
                 DownloaderPluginHolder downloaders) throws DownloadFailedException;
}
```

#### PluginExporterInterface

Export plugin interface for post-processing downloaded content.

```java
public interface PluginExporterInterface {
    /**
     * Returns the name of the exporter.
     * @return exporter name
     */
    String getName();
    
    /**
     * Exports content using the specified parameters.
     * @param exportParam export parameters
     * @throws ExportFailedException if export fails
     */
    void export(ExportParamDTO exportParam) throws ExportFailedException;
}
```

### Data Transfer Objects

#### CategoryDTO

Represents a content category (show, series, etc.).

```java
public class CategoryDTO {
    private String id;                    // Unique category identifier
    private String name;                  // Display name
    private boolean downloadable;         // Can contain episodes
    private Set<CategoryDTO> subCategories; // Child categories
    private String url;                   // Category URL (optional)
    private String extension;             // Default file extension
    
    // Constructors, getters, and setters
}
```

#### EpisodeDTO

Represents an individual episode or video.

```java
public class EpisodeDTO {
    private String id;                    // Unique episode identifier
    private String name;                  // Episode title
    private CategoryDTO category;         // Parent category
    private String url;                   // Download URL
    private String extension;             // File extension
    private Date date;                    // Episode date
    private String description;           // Episode description
    
    // Constructors, getters, and setters
}
```

#### DownloadParamDTO

Parameters for download operations.

```java
public class DownloadParamDTO {
    private String url;                   // Download URL
    private String outputPath;            // Output file path
    private String extension;             // File extension
    private Map<String, String> parameters; // Additional parameters
    
    // Constructors, getters, and setters
}
```

#### ExportParamDTO

Parameters for export operations.

```java
public class ExportParamDTO {
    private String inputPath;             // Input file path
    private String outputPath;            // Output file path
    private Map<String, String> parameters; // Export parameters
    
    // Constructors, getters, and setters
}
```

## Framework API

### Plugin Management

#### PluginLoader

Handles plugin discovery and loading.

```java
public class PluginLoader {
    /**
     * Loads all available content provider plugins.
     * @return list of loaded plugins
     */
    public List<PluginProviderInterface> loadProviders();
    
    /**
     * Loads all available downloader plugins.
     * @return list of loaded plugins
     */
    public List<PluginDownloaderInterface> loadDownloaders();
    
    /**
     * Loads all available exporter plugins.
     * @return list of loaded plugins
     */
    public List<PluginExporterInterface> loadExporters();
    
    /**
     * Loads a specific plugin by class name.
     * @param className fully qualified class name
     * @return loaded plugin instance
     */
    public <T> T loadPlugin(String className, Class<T> type);
}
```

#### DownloaderPluginHolder

Manages available downloader plugins.

```java
public class DownloaderPluginHolder {
    /**
     * Gets a downloader by name.
     * @param name downloader name
     * @return downloader instance or null
     */
    public PluginDownloaderInterface getDownloader(String name);
    
    /**
     * Gets all available downloaders.
     * @return map of downloader names to instances
     */
    public Map<String, PluginDownloaderInterface> getAllDownloaders();
    
    /**
     * Registers a new downloader.
     * @param name downloader name
     * @param downloader downloader instance
     */
    public void registerDownloader(String name, PluginDownloaderInterface downloader);
}
```

### Configuration Management

#### ConfigurationManager

Manages application configuration.

```java
public class ConfigurationManager {
    /**
     * Loads configuration from all sources.
     */
    public void loadConfiguration();
    
    /**
     * Gets a configuration value.
     * @param key configuration key
     * @return configuration value
     */
    public String getProperty(String key);
    
    /**
     * Gets a configuration value with default.
     * @param key configuration key
     * @param defaultValue default value
     * @return configuration value or default
     */
    public String getProperty(String key, String defaultValue);
    
    /**
     * Sets a configuration value.
     * @param key configuration key
     * @param value configuration value
     */
    public void setProperty(String key, String value);
    
    /**
     * Saves configuration to file.
     */
    public void saveConfiguration();
}
```

### Download Management

#### DownloadManager

Orchestrates download operations.

```java
public class DownloadManager {
    /**
     * Starts a download for an episode.
     * @param episode episode to download
     * @return download task
     */
    public DownloadTask startDownload(EpisodeDTO episode);
    
    /**
     * Pauses a download.
     * @param taskId download task ID
     */
    public void pauseDownload(String taskId);
    
    /**
     * Resumes a paused download.
     * @param taskId download task ID
     */
    public void resumeDownload(String taskId);
    
    /**
     * Cancels a download.
     * @param taskId download task ID
     */
    public void cancelDownload(String taskId);
    
    /**
     * Gets download progress.
     * @param taskId download task ID
     * @return download progress (0-100)
     */
    public int getDownloadProgress(String taskId);
    
    /**
     * Gets all active downloads.
     * @return list of active download tasks
     */
    public List<DownloadTask> getActiveDownloads();
}
```

#### DownloadTask

Represents a download operation.

```java
public class DownloadTask {
    private String id;                    // Unique task ID
    private EpisodeDTO episode;           // Episode being downloaded
    private DownloadState state;          // Current state
    private int progress;                 // Progress percentage
    private String errorMessage;          // Error message if failed
    private Date startTime;               // Start time
    private Date endTime;                 // End time
    
    // Getters and setters
}
```

### Export Management

#### ExportManager

Manages export operations.

```java
public class ExportManager {
    /**
     * Exports a downloaded file.
     * @param filePath path to downloaded file
     * @param exportConfig export configuration
     * @return export task
     */
    public ExportTask startExport(String filePath, ExportConfig exportConfig);
    
    /**
     * Gets export progress.
     * @param taskId export task ID
     * @return export progress (0-100)
     */
    public int getExportProgress(String taskId);
    
    /**
     * Gets all active exports.
     * @return list of active export tasks
     */
    public List<ExportTask> getActiveExports();
}
```

## Utility Classes

### HTTP Utilities

#### HttpUtils

HTTP request utilities.

```java
public class HttpUtils {
    /**
     * Performs a GET request.
     * @param url request URL
     * @return response content
     * @throws IOException if request fails
     */
    public static String get(String url) throws IOException;
    
    /**
     * Performs a POST request.
     * @param url request URL
     * @param data POST data
     * @return response content
     * @throws IOException if request fails
     */
    public static String post(String url, String data) throws IOException;
    
    /**
     * Downloads a file.
     * @param url file URL
     * @param outputPath output file path
     * @throws IOException if download fails
     */
    public static void downloadFile(String url, String outputPath) throws IOException;
}
```

### File Utilities

#### FileUtils

File operation utilities.

```java
public class FileUtils {
    /**
     * Creates a directory if it doesn't exist.
     * @param path directory path
     * @return true if created or exists
     */
    public static boolean createDirectory(String path);
    
    /**
     * Deletes a file or directory.
     * @param path file or directory path
     * @return true if deleted
     */
    public static boolean delete(String path);
    
    /**
     * Gets file size in bytes.
     * @param path file path
     * @return file size
     */
    public static long getFileSize(String path);
    
    /**
     * Checks if file exists.
     * @param path file path
     * @return true if exists
     */
    public static boolean exists(String path);
}
```

### String Utilities

#### StringUtils

String manipulation utilities.

```java
public class StringUtils {
    /**
     * Checks if string is empty or null.
     * @param str string to check
     * @return true if empty or null
     */
    public static boolean isEmpty(String str);
    
    /**
     * Truncates string to specified length.
     * @param str string to truncate
     * @param maxLength maximum length
     * @return truncated string
     */
    public static String truncate(String str, int maxLength);
    
    /**
     * Sanitizes filename.
     * @param filename filename to sanitize
     * @return sanitized filename
     */
    public static String sanitizeFilename(String filename);
}
```

## Exception Classes

### TechnicalException

Base exception for technical errors.

```java
public class TechnicalException extends Exception {
    public TechnicalException(String message);
    public TechnicalException(String message, Throwable cause);
}
```

### DownloadFailedException

Exception for download failures.

```java
public class DownloadFailedException extends TechnicalException {
    public DownloadFailedException(String message);
    public DownloadFailedException(String message, Throwable cause);
}
```

### ExportFailedException

Exception for export failures.

```java
public class ExportFailedException extends TechnicalException {
    public ExportFailedException(String message);
    public ExportFailedException(String message, Throwable cause);
}
```

## Configuration API

### Environment Variables

Supported environment variables:

```properties
# Application configuration
HABITV_HOME              // Application home directory
HABITV_VERSION           // Application version
HABITV_LOG_LEVEL         // Logging level (DEBUG, INFO, WARN, ERROR)
HABITV_DEBUG             // Enable debug mode (true/false)
HABITV_DEV_MODE          // Enable development mode (true/false)

# Download configuration
HABITV_DOWNLOAD_OUTPUT   // Download output directory
HABITV_MAX_ATTEMPTS      // Maximum download attempts
HABITV_DAEMON_CHECK_TIME // Daemon check time in seconds
HABITV_FILENAME_CUT_SIZE // Maximum filename length

# Plugin directories
HABITV_PLUGIN_DIR        // Main plugin directory
HABITV_PROVIDER_PLUGIN_DIR // Provider plugins directory
HABITV_DOWNLOADER_PLUGIN_DIR // Downloader plugins directory
HABITV_EXPORTER_PLUGIN_DIR // Exporter plugins directory

# Update configuration
HABITV_UPDATE_ON_STARTUP // Check for updates on startup (true/false)
HABITV_AUTORISE_SNAPSHOT // Allow snapshot updates (true/false)

# Proxy configuration
HTTP_PROXY_HOST          // HTTP proxy host
HTTP_PROXY_PORT          // HTTP proxy port
HTTP_PROXY_USER          // HTTP proxy username
HTTP_PROXY_PASSWORD      // HTTP proxy password

# Logging configuration
HABITV_LOG_FILE          // Log file path
HABITV_LOG_MAX_FILE_SIZE // Maximum log file size
HABITV_LOG_MAX_BACKUP_INDEX // Maximum backup log files

# Java configuration
JAVA_HOME                // Java installation directory
JAVA_OPTS                // Java VM options
```

### Configuration Files

#### config.xml

Main application configuration file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <download>
        <outputDirectory>/path/to/downloads</outputDirectory>
        <maxAttempts>3</maxAttempts>
        <daemonCheckTime>30</daemonCheckTime>
        <filenameCutSize>100</filenameCutSize>
    </download>
    
    <logging>
        <level>INFO</level>
        <file>/path/to/logs/habiTv.log</file>
        <maxFileSize>10MB</maxFileSize>
        <maxBackupIndex>5</maxBackupIndex>
    </logging>
    
    <update>
        <checkOnStartup>true</checkOnStartup>
        <allowSnapshots>false</allowSnapshots>
    </update>
    
    <proxy>
        <host>proxy.example.com</host>
        <port>8080</port>
        <username>user</username>
        <password>pass</password>
    </proxy>
</config>
```

#### grabconfig.xml

Show selection and monitoring configuration.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<grabconfig>
    <category id="show-1" name="My Show" provider="arte">
        <episode id="episode-1" name="Episode 1" url="http://example.com/ep1.mp4"/>
        <episode id="episode-2" name="Episode 2" url="http://example.com/ep2.mp4"/>
    </category>
    
    <category id="show-2" name="Another Show" provider="canalPlus">
        <episode id="episode-3" name="Episode 1" url="http://example.com/ep3.mp4"/>
    </category>
</grabconfig>
```

## Logging API

### Logger Configuration

```java
import org.apache.log4j.Logger;

public class MyClass {
    private static final Logger LOG = Logger.getLogger(MyClass.class);
    
    public void doSomething() {
        LOG.debug("Debug message");
        LOG.info("Info message");
        LOG.warn("Warning message");
        LOG.error("Error message", exception);
    }
}
```

### Log Levels

- **DEBUG**: Detailed debugging information
- **INFO**: General information about operations
- **WARN**: Warning messages for potential issues
- **ERROR**: Error messages for problems

## Plugin Development Examples

### Simple Content Provider

```java
public class SimpleProvider implements PluginProviderInterface {
    
    @Override
    public String getName() {
        return "SimpleProvider";
    }
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        Set<CategoryDTO> categories = new HashSet<>();
        
        CategoryDTO category = new CategoryDTO();
        category.setId("simple-category");
        category.setName("Simple Category");
        category.setDownloadable(true);
        
        categories.add(category);
        return categories;
    }
    
    @Override
    public Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException {
        Set<EpisodeDTO> episodes = new HashSet<>();
        
        EpisodeDTO episode = new EpisodeDTO();
        episode.setId("simple-episode");
        episode.setName("Simple Episode");
        episode.setCategory(category);
        episode.setUrl("http://example.com/video.mp4");
        
        episodes.add(episode);
        return episodes;
    }
}
```

### HTTP Downloader

```java
public class HttpDownloader implements PluginDownloaderInterface {
    
    @Override
    public String getName() {
        return "HTTP";
    }
    
    @Override
    public void download(DownloadParamDTO downloadParam, 
                        DownloaderPluginHolder downloaders) throws DownloadFailedException {
        
        String url = downloadParam.getUrl();
        String outputPath = downloadParam.getOutputPath();
        
        try {
            HttpUtils.downloadFile(url, outputPath);
        } catch (IOException e) {
            throw new DownloadFailedException("Download failed: " + url, e);
        }
    }
}
```

### FFmpeg Exporter

```java
public class FFmpegExporter implements PluginExporterInterface {
    
    @Override
    public String getName() {
        return "FFmpeg";
    }
    
    @Override
    public void export(ExportParamDTO exportParam) throws ExportFailedException {
        
        String inputPath = exportParam.getInputPath();
        String outputPath = exportParam.getOutputPath();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", inputPath, "-c:v", "libx264", outputPath
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new ExportFailedException("FFmpeg failed with exit code: " + exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            throw new ExportFailedException("Export failed", e);
        }
    }
}
```

This API reference provides comprehensive documentation for the habiTv framework. For more specific examples and advanced usage, refer to the existing plugins in the `plugins/` directory. 
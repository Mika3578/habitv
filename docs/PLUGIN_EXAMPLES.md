# habiTv Plugin Examples

This document provides practical examples of habiTv plugins to help developers understand the plugin system and create their own plugins.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Basic Plugin Structure

### Minimal Plugin Example

Here's the most basic plugin structure:

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

@HabiTvPluginInfo(
    name = "Example Plugin",
    version = "1.0.0",
    description = "A simple example plugin"
)
public class ExamplePlugin implements HabiTvPlugin {
    
    @Override
    public void initialize() {
        // Plugin initialization code
        System.out.println("Example plugin initialized");
    }
    
    @Override
    public void shutdown() {
        // Plugin cleanup code
        System.out.println("Example plugin shutdown");
    }
}
```

### Plugin with Configuration

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvPluginConfig;

@HabiTvPluginInfo(
    name = "Configurable Plugin",
    version = "1.0.0",
    description = "Plugin with configuration options"
)
public class ConfigurablePlugin implements HabiTvPlugin {
    
    private HabiTvPluginConfig config;
    
    @Override
    public void initialize() {
        // Load configuration
        config = new HabiTvPluginConfig();
        config.setString("api.key", "default-key");
        config.setInt("timeout", 30);
        config.setBoolean("debug", false);
        
        System.out.println("API Key: " + config.getString("api.key"));
        System.out.println("Timeout: " + config.getInt("timeout"));
        System.out.println("Debug: " + config.getBoolean("debug"));
    }
    
    @Override
    public void shutdown() {
        // Save configuration
        config.save();
    }
}
```

## Content Provider Plugins

### Simple Video Provider

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvVideoProvider;

import java.util.List;
import java.util.ArrayList;

@HabiTvPluginInfo(
    name = "Simple Video Provider",
    version = "1.0.0",
    description = "Provides videos from a simple source"
)
public class SimpleVideoProvider implements HabiTvVideoProvider {
    
    @Override
    public List<HabiTvVideo> getVideos() {
        List<HabiTvVideo> videos = new ArrayList<>();
        
        HabiTvVideo video = new HabiTvVideo();
        video.setTitle("Example Video");
        video.setUrl("http://example.com/video.mp4");
        video.setDuration(3600); // 1 hour in seconds
        video.setDescription("An example video");
        
        videos.add(video);
        return videos;
    }
    
    @Override
    public String getProviderName() {
        return "Simple Provider";
    }
}
```

### Web Scraping Video Provider

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvVideoProvider;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

@HabiTvPluginInfo(
    name = "Web Scraping Provider",
    version = "1.0.0",
    description = "Scrapes videos from a website"
)
public class WebScrapingProvider implements HabiTvVideoProvider {
    
    private static final String BASE_URL = "http://example.com/videos";
    
    @Override
    public List<HabiTvVideo> getVideos() {
        List<HabiTvVideo> videos = new ArrayList<>();
        
        try {
            // Fetch webpage content
            String content = fetchWebPage(BASE_URL);
            
            // Parse HTML content (simplified example)
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.contains("video-url")) {
                    HabiTvVideo video = parseVideoFromLine(line);
                    if (video != null) {
                        videos.add(video);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching videos: " + e.getMessage());
        }
        
        return videos;
    }
    
    private String fetchWebPage(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "habiTv/1.0");
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream())
        );
        
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        
        return content.toString();
    }
    
    private HabiTvVideo parseVideoFromLine(String line) {
        // Simplified parsing - in real implementation, use proper HTML parser
        if (line.contains("title=") && line.contains("url=")) {
            HabiTvVideo video = new HabiTvVideo();
            
            // Extract title
            int titleStart = line.indexOf("title=\"") + 7;
            int titleEnd = line.indexOf("\"", titleStart);
            String title = line.substring(titleStart, titleEnd);
            video.setTitle(title);
            
            // Extract URL
            int urlStart = line.indexOf("url=\"") + 5;
            int urlEnd = line.indexOf("\"", urlStart);
            String url = line.substring(urlStart, urlEnd);
            video.setUrl(url);
            
            return video;
        }
        return null;
    }
    
    @Override
    public String getProviderName() {
        return "Web Scraping Provider";
    }
}
```

### JSON API Video Provider

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvVideoProvider;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

@HabiTvPluginInfo(
    name = "JSON API Provider",
    version = "1.0.0",
    description = "Fetches videos from JSON API"
)
public class JsonApiProvider implements HabiTvVideoProvider {
    
    private static final String API_URL = "http://api.example.com/videos";
    
    @Override
    public List<HabiTvVideo> getVideos() {
        List<HabiTvVideo> videos = new ArrayList<>();
        
        try {
            // Fetch JSON data
            String jsonData = fetchJsonData(API_URL);
            
            // Parse JSON
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray videosArray = jsonObject.getJSONArray("videos");
            
            for (int i = 0; i < videosArray.length(); i++) {
                JSONObject videoJson = videosArray.getJSONObject(i);
                HabiTvVideo video = parseVideoFromJson(videoJson);
                videos.add(video);
            }
        } catch (Exception e) {
            System.err.println("Error fetching videos: " + e.getMessage());
        }
        
        return videos;
    }
    
    private String fetchJsonData(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream())
        );
        
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        
        return content.toString();
    }
    
    private HabiTvVideo parseVideoFromJson(JSONObject videoJson) {
        HabiTvVideo video = new HabiTvVideo();
        
        video.setTitle(videoJson.getString("title"));
        video.setUrl(videoJson.getString("url"));
        video.setDescription(videoJson.optString("description", ""));
        video.setDuration(videoJson.optInt("duration", 0));
        
        if (videoJson.has("thumbnail")) {
            video.setThumbnailUrl(videoJson.getString("thumbnail"));
        }
        
        return video;
    }
    
    @Override
    public String getProviderName() {
        return "JSON API Provider";
    }
}
```

## Export Plugins

### Simple File Export

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvExportPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@HabiTvPluginInfo(
    name = "Simple Export Plugin",
    version = "1.0.0",
    description = "Exports videos to simple text file"
)
public class SimpleExportPlugin implements HabiTvExportPlugin {
    
    @Override
    public void exportVideo(HabiTvVideo video, String outputPath) throws Exception {
        File outputFile = new File(outputPath);
        FileWriter writer = new FileWriter(outputFile);
        
        try {
            writer.write("Title: " + video.getTitle() + "\n");
            writer.write("URL: " + video.getUrl() + "\n");
            writer.write("Description: " + video.getDescription() + "\n");
            writer.write("Duration: " + video.getDuration() + " seconds\n");
            
            if (video.getThumbnailUrl() != null) {
                writer.write("Thumbnail: " + video.getThumbnailUrl() + "\n");
            }
        } finally {
            writer.close();
        }
    }
    
    @Override
    public String getExportFormat() {
        return "txt";
    }
    
    @Override
    public String getExportDescription() {
        return "Simple text file export";
    }
}
```

### JSON Export Plugin

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvExportPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

@HabiTvPluginInfo(
    name = "JSON Export Plugin",
    version = "1.0.0",
    description = "Exports videos to JSON format"
)
public class JsonExportPlugin implements HabiTvExportPlugin {
    
    @Override
    public void exportVideo(HabiTvVideo video, String outputPath) throws Exception {
        JSONObject videoJson = new JSONObject();
        
        videoJson.put("title", video.getTitle());
        videoJson.put("url", video.getUrl());
        videoJson.put("description", video.getDescription());
        videoJson.put("duration", video.getDuration());
        
        if (video.getThumbnailUrl() != null) {
            videoJson.put("thumbnail", video.getThumbnailUrl());
        }
        
        // Add metadata
        JSONObject metadata = new JSONObject();
        metadata.put("exported_at", System.currentTimeMillis());
        metadata.put("exporter", "habiTv JSON Export Plugin");
        videoJson.put("metadata", metadata);
        
        // Write to file
        File outputFile = new File(outputPath);
        FileWriter writer = new FileWriter(outputFile);
        
        try {
            writer.write(videoJson.toString(2)); // Pretty print with 2 spaces
        } finally {
            writer.close();
        }
    }
    
    @Override
    public String getExportFormat() {
        return "json";
    }
    
    @Override
    public String getExportDescription() {
        return "JSON format export with metadata";
    }
}
```

## Utility Plugins

### Logging Plugin

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@HabiTvPluginInfo(
    name = "Logging Plugin",
    version = "1.0.0",
    description = "Logs video operations to file"
)
public class LoggingPlugin implements HabiTvPlugin {
    
    private FileWriter logWriter;
    private SimpleDateFormat dateFormat;
    
    @Override
    public void initialize() {
        try {
            logWriter = new FileWriter("video_operations.log", true);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            log("Logging plugin initialized");
        } catch (IOException e) {
            System.err.println("Failed to initialize logging plugin: " + e.getMessage());
        }
    }
    
    @Override
    public void shutdown() {
        try {
            if (logWriter != null) {
                log("Logging plugin shutdown");
                logWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing log file: " + e.getMessage());
        }
    }
    
    public void logVideoDownload(HabiTvVideo video) {
        log("Video downloaded: " + video.getTitle() + " (" + video.getUrl() + ")");
    }
    
    public void logVideoExport(HabiTvVideo video, String format) {
        log("Video exported: " + video.getTitle() + " to " + format + " format");
    }
    
    private void log(String message) {
        try {
            String timestamp = dateFormat.format(new Date());
            logWriter.write("[" + timestamp + "] " + message + "\n");
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }
    }
}
```

### Statistics Plugin

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvVideo;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@HabiTvPluginInfo(
    name = "Statistics Plugin",
    version = "1.0.0",
    description = "Tracks video download and export statistics"
)
public class StatisticsPlugin implements HabiTvPlugin {
    
    private AtomicLong totalDownloads;
    private AtomicLong totalExports;
    private Map<String, Long> downloadsByProvider;
    private Map<String, Long> exportsByFormat;
    
    @Override
    public void initialize() {
        totalDownloads = new AtomicLong(0);
        totalExports = new AtomicLong(0);
        downloadsByProvider = new ConcurrentHashMap<>();
        exportsByFormat = new ConcurrentHashMap<>();
        
        System.out.println("Statistics plugin initialized");
    }
    
    @Override
    public void shutdown() {
        printStatistics();
    }
    
    public void recordDownload(HabiTvVideo video) {
        totalDownloads.incrementAndGet();
        
        String provider = video.getProviderName();
        if (provider != null) {
            downloadsByProvider.merge(provider, 1L, Long::sum);
        }
    }
    
    public void recordExport(HabiTvVideo video, String format) {
        totalExports.incrementAndGet();
        exportsByFormat.merge(format, 1L, Long::sum);
    }
    
    public long getTotalDownloads() {
        return totalDownloads.get();
    }
    
    public long getTotalExports() {
        return totalExports.get();
    }
    
    public Map<String, Long> getDownloadsByProvider() {
        return new ConcurrentHashMap<>(downloadsByProvider);
    }
    
    public Map<String, Long> getExportsByFormat() {
        return new ConcurrentHashMap<>(exportsByFormat);
    }
    
    private void printStatistics() {
        System.out.println("=== Statistics Plugin Report ===");
        System.out.println("Total Downloads: " + totalDownloads.get());
        System.out.println("Total Exports: " + totalExports.get());
        
        System.out.println("\nDownloads by Provider:");
        downloadsByProvider.forEach((provider, count) -> 
            System.out.println("  " + provider + ": " + count));
        
        System.out.println("\nExports by Format:");
        exportsByFormat.forEach((format, count) -> 
            System.out.println("  " + format + ": " + count));
    }
}
```

## Advanced Plugin Examples

### Plugin with Dependencies

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvPluginConfig;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;

@HabiTvPluginInfo(
    name = "Advanced HTTP Plugin",
    version = "1.0.0",
    description = "Plugin using Apache HttpClient"
)
public class AdvancedHttpPlugin implements HabiTvPlugin {
    
    private HttpClient httpClient;
    private HabiTvPluginConfig config;
    
    @Override
    public void initialize() {
        // Initialize HTTP client
        httpClient = HttpClients.createDefault();
        
        // Load configuration
        config = new HabiTvPluginConfig();
        config.setString("api.base_url", "http://api.example.com");
        config.setInt("connection.timeout", 30000);
        config.setInt("socket.timeout", 60000);
        
        System.out.println("Advanced HTTP plugin initialized");
    }
    
    @Override
    public void shutdown() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing HTTP client: " + e.getMessage());
        }
    }
    
    public String makeHttpRequest(String endpoint) throws Exception {
        String baseUrl = config.getString("api.base_url");
        String url = baseUrl + endpoint;
        
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", "habiTv/1.0");
        
        HttpResponse response = httpClient.execute(request);
        
        // Process response...
        return "Response from " + url;
    }
}
```

### Plugin with Threading

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@HabiTvPluginInfo(
    name = "Threading Plugin",
    version = "1.0.0",
    description = "Plugin demonstrating threading capabilities"
)
public class ThreadingPlugin implements HabiTvPlugin {
    
    private ExecutorService executor;
    private volatile boolean running;
    
    @Override
    public void initialize() {
        executor = Executors.newFixedThreadPool(4);
        running = true;
        
        // Start background task
        executor.submit(this::backgroundTask);
        
        System.out.println("Threading plugin initialized");
    }
    
    @Override
    public void shutdown() {
        running = false;
        
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void backgroundTask() {
        while (running) {
            try {
                // Perform background work
                System.out.println("Background task running...");
                Thread.sleep(5000); // Sleep for 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public Future<String> submitTask(Runnable task) {
        return executor.submit(() -> {
            task.run();
            return "Task completed";
        });
    }
}
```

## Plugin Configuration Examples

### Configuration File Example

```properties
# plugin.properties
plugin.name=Example Plugin
plugin.version=1.0.0
plugin.description=An example plugin

# API Configuration
api.base_url=http://api.example.com
api.timeout=30000
api.retry_count=3

# Feature Flags
feature.debug.enabled=false
feature.cache.enabled=true
feature.logging.enabled=true

# Performance Settings
performance.thread_pool_size=4
performance.buffer_size=8192
performance.max_connections=10
```

### Loading Configuration

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;
import fr.mika3578.habitv.api.HabiTvPluginConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@HabiTvPluginInfo(
    name = "Configurable Plugin",
    version = "1.0.0",
    description = "Plugin with external configuration"
)
public class ConfigurablePlugin implements HabiTvPlugin {
    
    private Properties config;
    
    @Override
    public void initialize() {
        config = new Properties();
        
        try {
            // Load configuration from file
            FileInputStream fis = new FileInputStream("plugin.properties");
            config.load(fis);
            fis.close();
            
            System.out.println("Configuration loaded:");
            System.out.println("API Base URL: " + config.getProperty("api.base_url"));
            System.out.println("Timeout: " + config.getProperty("api.timeout"));
            System.out.println("Debug Enabled: " + config.getProperty("feature.debug.enabled"));
            
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            // Use default values
            setDefaultConfiguration();
        }
    }
    
    private void setDefaultConfiguration() {
        config.setProperty("api.base_url", "http://default.example.com");
        config.setProperty("api.timeout", "30000");
        config.setProperty("feature.debug.enabled", "false");
    }
    
    @Override
    public void shutdown() {
        // Save configuration if needed
        System.out.println("Configurable plugin shutdown");
    }
}
```

## Best Practices

### Error Handling

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

@HabiTvPluginInfo(
    name = "Error Handling Example",
    version = "1.0.0",
    description = "Demonstrates proper error handling"
)
public class ErrorHandlingPlugin implements HabiTvPlugin {
    
    @Override
    public void initialize() {
        try {
            // Plugin initialization
            performInitialization();
        } catch (Exception e) {
            System.err.println("Failed to initialize plugin: " + e.getMessage());
            // Don't throw - let plugin load with reduced functionality
        }
    }
    
    private void performInitialization() throws Exception {
        // Simulate initialization that might fail
        if (Math.random() < 0.5) {
            throw new Exception("Random initialization failure");
        }
        
        System.out.println("Plugin initialized successfully");
    }
    
    @Override
    public void shutdown() {
        try {
            // Cleanup code
            performCleanup();
        } catch (Exception e) {
            System.err.println("Error during plugin shutdown: " + e.getMessage());
        }
    }
    
    private void performCleanup() throws Exception {
        // Simulate cleanup that might fail
        System.out.println("Plugin cleanup completed");
    }
}
```

### Resource Management

```java
package com.example.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

@HabiTvPluginInfo(
    name = "Resource Management Example",
    version = "1.0.0",
    description = "Demonstrates proper resource management"
)
public class ResourceManagementPlugin implements HabiTvPlugin {
    
    private List<Closeable> resources;
    
    @Override
    public void initialize() {
        resources = new ArrayList<>();
        
        try {
            // Initialize resources
            FileWriter writer = new FileWriter("plugin.log");
            resources.add(writer);
            
            // More resource initialization...
            
        } catch (Exception e) {
            // Clean up any resources that were created
            cleanupResources();
            throw new RuntimeException("Failed to initialize plugin", e);
        }
    }
    
    @Override
    public void shutdown() {
        cleanupResources();
    }
    
    private void cleanupResources() {
        if (resources != null) {
            for (Closeable resource : resources) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
            resources.clear();
        }
    }
}
```

These examples demonstrate various aspects of habiTv plugin development, from basic structure to advanced features. Use them as starting points for your own plugins and adapt them to your specific needs. 
# habiTv Plugin Development Guide

This guide explains how to develop plugins for the habiTv application.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Overview

habiTv uses a plugin-based architecture where content providers are implemented as separate plugins. This allows for easy extension and maintenance of the application.

### Plugin Types

- **Content Provider Plugins**: Provide video content from specific sources
- **Export Plugins**: Handle video export and post-processing
- **Utility Plugins**: Provide additional functionality

## Plugin API

### Core Interfaces

#### HabiTvPlugin

Base interface for all plugins:

```java
public interface HabiTvPlugin {
    void initialize();
    void shutdown();
}
```

#### HabiTvVideoProvider

Interface for content provider plugins:

```java
public interface HabiTvVideoProvider extends HabiTvPlugin {
    List<HabiTvVideo> getVideos();
    String getProviderName();
}
```

#### HabiTvExportPlugin

Interface for export plugins:

```java
public interface HabiTvExportPlugin extends HabiTvPlugin {
    void exportVideo(HabiTvVideo video, String outputPath);
    String getExportFormat();
    String getExportDescription();
}
```

### Data Transfer Objects

#### CategoryDTO

Represents a content category:

```java
public class CategoryDTO {
    private String name;
    private String url;
    private List<EpisodeDTO> episodes;
    
    // Getters and setters
}
```

#### EpisodeDTO

Represents a video episode:

```java
public class EpisodeDTO {
    private String title;
    private String url;
    private String description;
    private int duration;
    private String thumbnailUrl;
    
    // Getters and setters
}
```

## Plugin Development

### Basic Structure

Create a new Maven module for your plugin:

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>fr.mika3578.habitv</groupId>
    <artifactId>my-plugin</artifactId>
    <version>1.0.0</version>
    
    <dependencies>
        <dependency>
            <groupId>fr.mika3578.habitv</groupId>
            <artifactId>api</artifactId>
            <version>4.1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

### Plugin Implementation

#### Basic Plugin

```java
package fr.mika3578.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

@HabiTvPluginInfo(
    name = "My Plugin",
    version = "1.0.0",
    description = "My custom plugin"
)
public class MyPlugin implements HabiTvPlugin {
    
    @Override
    public void initialize() {
        System.out.println("My plugin initialized");
    }
    
    @Override
    public void shutdown() {
        System.out.println("My plugin shutdown");
    }
}
```

#### Content Provider Plugin

```java
package fr.mika3578.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvVideoProvider;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

import java.util.List;
import java.util.ArrayList;

@HabiTvPluginInfo(
    name = "My Content Provider",
    version = "1.0.0",
    description = "Provides content from my source"
)
public class MyContentProvider implements HabiTvVideoProvider {
    
    @Override
    public void initialize() {
        // Initialize connection to content source
    }
    
    @Override
    public void shutdown() {
        // Clean up resources
    }
    
    @Override
    public List<HabiTvVideo> getVideos() {
        List<HabiTvVideo> videos = new ArrayList<>();
        
        // Fetch videos from your source
        HabiTvVideo video = new HabiTvVideo();
        video.setTitle("Example Video");
        video.setUrl("http://example.com/video.mp4");
        video.setDescription("Example video description");
        video.setDuration(3600); // 1 hour in seconds
        
        videos.add(video);
        return videos;
    }
    
    @Override
    public String getProviderName() {
        return "My Provider";
    }
}
```

#### Export Plugin

```java
package fr.mika3578.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvExportPlugin;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@HabiTvPluginInfo(
    name = "My Export Plugin",
    version = "1.0.0",
    description = "Exports videos in custom format"
)
public class MyExportPlugin implements HabiTvExportPlugin {
    
    @Override
    public void initialize() {
        // Initialize export functionality
    }
    
    @Override
    public void shutdown() {
        // Clean up resources
    }
    
    @Override
    public void exportVideo(HabiTvVideo video, String outputPath) 
            throws Exception {
        File outputFile = new File(outputPath);
        FileWriter writer = new FileWriter(outputFile);
        
        try {
            writer.write("Title: " + video.getTitle() + "\n");
            writer.write("URL: " + video.getUrl() + "\n");
            writer.write("Description: " + video.getDescription() + "\n");
            writer.write("Duration: " + video.getDuration() + " seconds\n");
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
        return "Simple text export";
    }
}
```

### Web Scraping Example

Here's an example of a plugin that scrapes content from a website:

```java
package fr.mika3578.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvVideoProvider;
import fr.mika3578.habitv.api.HabiTvVideo;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

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
    public void initialize() {
        // Initialize HTTP client or other resources
    }
    
    @Override
    public void shutdown() {
        // Clean up resources
    }
    
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
        HttpURLConnection connection = 
            (HttpURLConnection) url.openConnection();
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

## Testing

### Unit Testing

#### Test Structure

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

class MyPluginTest {
    
    private MyPlugin plugin;
    
    @BeforeEach
    void setUp() {
        plugin = new MyPlugin();
    }
    
    @AfterEach
    void tearDown() {
        plugin.shutdown();
    }
    
    @Test
    void testInitialization() {
        assertDoesNotThrow(() -> plugin.initialize());
    }
    
    @Test
    void testShutdown() {
        plugin.initialize();
        assertDoesNotThrow(() -> plugin.shutdown());
    }
}
```

#### Test with Main Application

```bash
# Build your plugin
mvn clean package

# Copy to habiTv plugins directory
cp target/my-plugin-1.0.0.jar ../habitv/plugins/

# Run habiTv and test your plugin
java -jar ../habitv/target/habiTv.jar
```

#### Test Plugin Loading

1. Copy your JAR to the plugin directory
2. Start habiTv application
3. Check logs for plugin loading messages
4. Verify plugin appears in plugin list

## Deployment

### Repository Information

The habiTv plugin repository is hosted at:

- **URL**: `http://dabiboo.free.fr/repository/habitv/`
- **Structure**: Organized by plugin name and version
- **Access**: Public read access, write access by request

### Deployment Process

#### Step 1: Build the Plugin

```bash
# Build your plugin
mvn clean package

# Verify JAR contents
jar -tf target/my-plugin-1.0.0.jar
```

#### Step 2: Update plugins.txt

Add your plugin to the plugins.txt file:

```
my-plugin
```

#### Step 3: Upload the JAR

Upload your JAR file to the repository:

```bash
# Upload to repository
curl -T target/my-plugin-1.0.0.jar \
  http://dabiboo.free.fr/repository/habitv/plugins/my-plugin/my-plugin-1.0.0.jar
```

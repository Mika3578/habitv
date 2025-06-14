# habiTv Plugin Development Guide

This guide explains how to develop plugins for the habiTv application.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 14, 2025

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

#### Step 4: Verify Deployment

Test your plugin deployment:

```bash
# Test repository access
curl -I http://dabiboo.free.fr/repository/habitv/plugins/my-plugin/my-plugin-1.0.0.jar

# Check plugins.txt
curl http://dabiboo.free.fr/repository/habitv/plugins.txt
```

### Version Management

- **Next Startup**: All habiTv clients will automatically download and install your plugin
- **Version Updates**: Increment version number and repeat deployment process
- **Rollback**: Keep previous versions available for rollback

## Plugin Configuration

### Configuration Properties

#### Required Properties

- **NAME**: Unique plugin identifier
- **VERSION**: Plugin version (semantic versioning)
- **DESCRIPTION**: Human-readable description

#### Optional Properties

- **ENCODING**: Character encoding (default: UTF-8)
- **DEBUG**: Enable debug mode (default: false)
- **TIMEOUT**: Request timeout in milliseconds (default: 30000)

#### Exception Handling

```java
@Override
public List<HabiTvVideo> getVideos() {
    try {
        // Fetch videos
        return fetchVideosFromSource();
    } catch (Exception e) {
        logger.error("Failed to fetch videos", e);
        return new ArrayList<>(); // Return empty list instead of throwing
    }
}
```

#### Logging

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPlugin implements HabiTvPlugin {
    private static final Logger logger = 
        LoggerFactory.getLogger(MyPlugin.class);
    
    @Override
    public void initialize() {
        logger.info("Initializing MyPlugin");
        // Initialization code
    }
}
```

## Best Practices

### Code Quality

- **Follow Naming Conventions**: Use consistent naming for classes, methods, and variables
- **Handle Exceptions Gracefully**: Don't let exceptions crash the application
- **Use Logging**: Log important events and errors
- **Validate Input**: Always validate external input

### Testing

- **Unit Tests**: Write tests for all plugin functionality
- **Integration Tests**: Test plugin with main application
- **Error Handling**: Test error conditions and edge cases
- **Performance**: Test with large datasets

### Version Management

- **Semantic Versioning**: Use MAJOR.MINOR.PATCH format
- **Backward Compatibility**: Maintain compatibility when possible
- **Changelog**: Document changes between versions
- **Deprecation**: Mark deprecated features clearly

### Deployment

- **Staged Rollout**: Consider gradual deployment for major changes
- **Testing**: Test thoroughly before deployment
- **Documentation**: Update documentation with new features
- **Monitoring**: Monitor plugin performance after deployment

### Updates

- **Regular Updates**: Keep plugins updated with latest API changes
- **Security Updates**: Apply security patches promptly
- **Performance**: Monitor and optimize performance
- **User Feedback**: Incorporate user feedback into updates

### Monitoring

- **Error Logs**: Monitor for errors and exceptions
- **Performance Metrics**: Track response times and resource usage
- **User Reports**: Monitor user feedback and bug reports
- **Usage Statistics**: Track plugin usage patterns

## Troubleshooting

### Common Issues

#### Plugin Not Loading

1. **Check JAR**: Verify the JAR file is valid and contains required classes
2. **Check Dependencies**: Ensure all dependencies are included
3. **Check Logs**: Review application logs for error messages
4. **Check Permissions**: Verify file permissions are correct

#### Plugin Not Working

1. **Check Website**: Verify the target website is accessible
2. **Check Selectors**: Verify HTML selectors are still valid
3. **Check API**: Verify API endpoints and authentication
4. **Check Network**: Verify network connectivity and firewalls

#### Deployment Issues

1. **Check Access**: Verify repository access and permissions
2. **Check URLs**: Verify JAR URLs are accessible
3. **Check plugins.txt**: Verify plugin is listed correctly
4. **Check Version**: Verify version numbers are correct

### Debug Mode

#### Enable Debug Logging

```xml
<Logger name="fr.mika3578.habitv.plugin" level="DEBUG"/>
```

#### Test Locally

```bash
# Run with debug logging
java -Dlog.level=DEBUG -jar habiTv.jar

# Check debug output
tail -f logs/habitv.log | grep DEBUG
```

#### Check JAR Contents

```bash
# List JAR contents
jar -tf my-plugin-1.0.0.jar

# Extract and examine
jar -xf my-plugin-1.0.0.jar
ls -la META-INF/
```

## Planned Improvements

1. **Plugin Marketplace**: Web interface for plugin discovery and management
2. **Plugin Signing**: Digital signatures for plugin verification
3. **Plugin Sandboxing**: Isolated execution environment for plugins
4. **Plugin Dependencies**: Support for plugin dependencies and conflicts
5. **Plugin Versioning**: Better version management and compatibility checking

### Development Tools

1. **Plugin Generator**: Tool to generate plugin templates
2. **Plugin Validator**: Tool to validate plugin structure and configuration
3. **Plugin Simulator**: Tool to test plugins without deployment
4. **Plugin Documentation**: Automated documentation generation

This guide provides comprehensive information for developing plugins for habiTv. Follow these guidelines to create robust, maintainable plugins that integrate seamlessly with the application. 
# habiTv Plugin Development Guide

This guide covers developing plugins for habiTv, including content providers, downloaders, and exporters.

## Plugin Architecture Overview

habiTv uses a modular plugin system with three main types:

### Plugin Types

1. **Content Provider Plugins**: Discover and list available content
2. **Downloader Plugins**: Handle actual file downloads
3. **Export Plugins**: Post-process downloaded content

### Plugin Structure

```
plugins/
├── myplugin/
│   ├── src/
│   │   └── com/dabi/habitv/plugin/myplugin/
│   │       ├── MyPluginProvider.java
│   │       ├── MyPluginDownloader.java
│   │       └── MyPluginExporter.java
│   ├── test/
│   │   └── com/dabi/habitv/plugin/myplugin/
│   │       └── TestMyPlugin.java
│   └── pom.xml
```

## Content Provider Plugin Development

### Basic Structure

```java
package com.dabi.habitv.plugin.myplugin;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;

public class MyPluginProvider implements PluginProviderInterface {
    
    @Override
    public String getName() {
        return "MyPlugin";
    }
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        // Return available categories
    }
    
    @Override
    public Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException {
        // Return episodes for the given category
    }
}
```

### Key Components

#### CategoryDTO
Represents a content category (show, series, etc.):

```java
CategoryDTO category = new CategoryDTO();
category.setId("unique-category-id");
category.setName("Category Name");
category.setDownloadable(true); // Can contain episodes
category.setSubCategories(new HashSet<>()); // Child categories
```

#### EpisodeDTO
Represents an individual episode or video:

```java
EpisodeDTO episode = new EpisodeDTO();
episode.setId("unique-episode-id");
episode.setName("Episode Title");
episode.setCategory(category);
episode.setUrl("http://example.com/video.mp4");
```

### Implementation Example

```java
public class YouTubeProvider implements PluginProviderInterface {
    
    private static final String BASE_URL = "https://www.youtube.com";
    
    @Override
    public String getName() {
        return "YouTube";
    }
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        Set<CategoryDTO> categories = new HashSet<>();
        
        // Create main category
        CategoryDTO mainCategory = new CategoryDTO();
        mainCategory.setId("youtube-main");
        mainCategory.setName("YouTube");
        mainCategory.setDownloadable(false);
        
        // Add subcategories (channels, playlists, etc.)
        Set<CategoryDTO> subCategories = new HashSet<>();
        // ... populate subcategories
        
        mainCategory.setSubCategories(subCategories);
        categories.add(mainCategory);
        
        return categories;
    }
    
    @Override
    public Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException {
        Set<EpisodeDTO> episodes = new HashSet<>();
        
        try {
            // Fetch episodes from YouTube API or web scraping
            Document doc = Jsoup.connect(category.getUrl()).get();
            
            // Parse video elements
            Elements videos = doc.select(".video-item");
            for (Element video : videos) {
                EpisodeDTO episode = new EpisodeDTO();
                episode.setId(video.attr("data-video-id"));
                episode.setName(video.select(".title").text());
                episode.setCategory(category);
                episode.setUrl(BASE_URL + video.attr("href"));
                
                episodes.add(episode);
            }
        } catch (IOException e) {
            throw new TechnicalException("Failed to fetch episodes", e);
        }
        
        return episodes;
    }
}
```

## Downloader Plugin Development

### Basic Structure

```java
package com.dabi.habitv.plugin.myplugin;

import com.dabi.habitv.api.plugin.api.PluginDownloaderInterface;
import com.dabi.habitv.api.plugin.dto.DownloadParamDTO;
import com.dabi.habitv.api.plugin.exception.DownloadFailedException;

public class MyPluginDownloader implements PluginDownloaderInterface {
    
    @Override
    public String getName() {
        return "MyDownloader";
    }
    
    @Override
    public void download(DownloadParamDTO downloadParam, 
                        DownloaderPluginHolder downloaders) throws DownloadFailedException {
        // Implement download logic
    }
}
```

### Download Implementation

```java
public class HTTPDownloader implements PluginDownloaderInterface {
    
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
            // Use curl or other HTTP client
            ProcessBuilder pb = new ProcessBuilder("curl", "-L", "-o", outputPath, url);
            Process process = pb.start();
            
            // Monitor download progress
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new DownloadFailedException("Download failed with exit code: " + exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            throw new DownloadFailedException("Download failed", e);
        }
    }
}
```

## Export Plugin Development

### Basic Structure

```java
package com.dabi.habitv.plugin.myplugin;

import com.dabi.habitv.api.plugin.api.PluginExporterInterface;
import com.dabi.habitv.api.plugin.dto.ExportParamDTO;
import com.dabi.habitv.api.plugin.exception.ExportFailedException;

public class MyPluginExporter implements PluginExporterInterface {
    
    @Override
    public String getName() {
        return "MyExporter";
    }
    
    @Override
    public void export(ExportParamDTO exportParam) throws ExportFailedException {
        // Implement export logic
    }
}
```

### Export Implementation

```java
public class FFmpegExporter implements PluginExporterInterface {
    
    @Override
    public String getName() {
        return "FFmpeg";
    }
    
    @Override
    public void export(ExportParamDTO exportParam) throws ExportFailedException {
        
        String inputFile = exportParam.getInputPath();
        String outputFile = exportParam.getOutputPath();
        
        try {
            // Build FFmpeg command
            List<String> command = Arrays.asList(
                "ffmpeg", "-i", inputFile,
                "-c:v", "libx264",
                "-c:a", "aac",
                outputFile
            );
            
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            
            // Monitor export progress
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new ExportFailedException("Export failed with exit code: " + exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            throw new ExportFailedException("Export failed", e);
        }
    }
}
```

## Plugin Configuration

### Maven Configuration

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.dabi.habitv</groupId>
        <artifactId>parent</artifactId>
        <version>4.1.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>myplugin</artifactId>
    <packaging>jar</packaging>
    
    <dependencies>
        <dependency>
            <groupId>com.dabi.habitv</groupId>
            <artifactId>api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.dabi.habitv</groupId>
            <artifactId>framework</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### Plugin Manifest

Create a `plugin.properties` file in your plugin's resources:

```properties
plugin.name=MyPlugin
plugin.version=1.0.0
plugin.description=My custom plugin for habiTv
plugin.author=Your Name
plugin.website=https://github.com/your-repo/myplugin
```

## Testing Your Plugin

### Unit Testing

```java
package com.dabi.habitv.plugin.myplugin;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class TestMyPlugin {
    
    private MyPluginProvider provider;
    
    @Before
    public void setUp() {
        provider = new MyPluginProvider();
    }
    
    @Test
    public void testFindCategories() throws TechnicalException {
        Set<CategoryDTO> categories = provider.findCategory();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }
    
    @Test
    public void testFindEpisodes() throws TechnicalException {
        CategoryDTO category = new CategoryDTO();
        category.setId("test-category");
        
        Set<EpisodeDTO> episodes = provider.findEpisode(category);
        assertNotNull(episodes);
    }
}
```

### Integration Testing

Use the plugin tester framework:

```java
public class TestMyPluginIntegration extends BasePluginProviderTester {
    
    @Test
    public void testPluginProvider() throws Exception {
        testPluginProvider(MyPluginProvider.class, false);
    }
}
```

## Best Practices

### Error Handling

```java
public class RobustPluginProvider implements PluginProviderInterface {
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        try {
            // Implementation
        } catch (IOException e) {
            throw new TechnicalException("Network error while fetching categories", e);
        } catch (ParseException e) {
            throw new TechnicalException("Failed to parse category data", e);
        } catch (Exception e) {
            throw new TechnicalException("Unexpected error", e);
        }
    }
}
```

### Logging

```java
import org.apache.log4j.Logger;

public class LoggedPluginProvider implements PluginProviderInterface {
    
    private static final Logger LOG = Logger.getLogger(LoggedPluginProvider.class);
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        LOG.info("Starting category discovery");
        
        try {
            Set<CategoryDTO> categories = fetchCategories();
            LOG.info("Found " + categories.size() + " categories");
            return categories;
        } catch (Exception e) {
            LOG.error("Failed to fetch categories", e);
            throw new TechnicalException("Category discovery failed", e);
        }
    }
}
```

### Configuration

```java
public class ConfigurablePluginProvider implements PluginProviderInterface {
    
    private final String baseUrl;
    private final int timeout;
    
    public ConfigurablePluginProvider() {
        // Load from configuration
        this.baseUrl = System.getProperty("myplugin.baseUrl", "https://default.com");
        this.timeout = Integer.parseInt(System.getProperty("myplugin.timeout", "30000"));
    }
}
```

## Advanced Features

### Caching

```java
public class CachedPluginProvider implements PluginProviderInterface {
    
    private final Map<String, Set<EpisodeDTO>> episodeCache = new ConcurrentHashMap<>();
    private final long cacheTimeout = 300000; // 5 minutes
    
    @Override
    public Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException {
        String cacheKey = category.getId();
        
        // Check cache
        Set<EpisodeDTO> cached = episodeCache.get(cacheKey);
        if (cached != null && !isCacheExpired(cacheKey)) {
            return cached;
        }
        
        // Fetch fresh data
        Set<EpisodeDTO> episodes = fetchEpisodes(category);
        episodeCache.put(cacheKey, episodes);
        
        return episodes;
    }
}
```

### Authentication

```java
public class AuthenticatedPluginProvider implements PluginProviderInterface {
    
    private String authToken;
    
    private void authenticate() throws TechnicalException {
        try {
            // Perform authentication
            Document loginPage = Jsoup.connect("https://example.com/login")
                .data("username", getUsername())
                .data("password", getPassword())
                .post();
            
            // Extract auth token
            this.authToken = loginPage.select("input[name=token]").val();
            
        } catch (IOException e) {
            throw new TechnicalException("Authentication failed", e);
        }
    }
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        if (authToken == null) {
            authenticate();
        }
        
        // Use auth token in requests
        // ...
    }
}
```

## Deployment

### Building Your Plugin

```bash
# Build the plugin
mvn clean package

# The JAR will be in target/myplugin-1.0.0.jar
```

### Installing Your Plugin

1. **Copy JAR**: Place your plugin JAR in the plugins directory
2. **Restart habiTv**: Restart the application to load the plugin
3. **Verify**: Check that your plugin appears in the available plugins list

### Distribution

- **GitHub Release**: Create a release with your plugin JAR
- **Maven Repository**: Publish to a Maven repository
- **Direct Distribution**: Share the JAR file directly

## Troubleshooting

### Common Issues

1. **Plugin Not Loading**: Check JAR file is in the correct location
2. **Class Not Found**: Ensure all dependencies are included
3. **Configuration Errors**: Verify plugin.properties file
4. **Network Issues**: Check internet connectivity and proxy settings

### Debugging

```java
// Enable debug logging
System.setProperty("log4j.logger.com.dabi.habitv.plugin.myplugin", "DEBUG");

// Add debug output
LOG.debug("Processing category: " + category.getName());
LOG.debug("Found episodes: " + episodes.size());
```

### Getting Help

- **Check Logs**: Review habiTv logs for error messages
- **Test Isolation**: Test your plugin in isolation first
- **Community**: Ask questions in the habiTv community
- **Documentation**: Refer to the API documentation

## Example Plugins

### Simple RSS Plugin

```java
public class RSSPluginProvider implements PluginProviderInterface {
    
    @Override
    public String getName() {
        return "RSS";
    }
    
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        Set<CategoryDTO> categories = new HashSet<>();
        
        // Create RSS category
        CategoryDTO rssCategory = new CategoryDTO();
        rssCategory.setId("rss-feed");
        rssCategory.setName("RSS Feed");
        rssCategory.setDownloadable(true);
        rssCategory.setUrl("https://example.com/feed.xml");
        
        categories.add(rssCategory);
        return categories;
    }
    
    @Override
    public Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException {
        Set<EpisodeDTO> episodes = new HashSet<>();
        
        try {
            Document doc = Jsoup.connect(category.getUrl()).get();
            Elements items = doc.select("item");
            
            for (Element item : items) {
                EpisodeDTO episode = new EpisodeDTO();
                episode.setId(item.select("guid").text());
                episode.setName(item.select("title").text());
                episode.setCategory(category);
                episode.setUrl(item.select("link").text());
                
                episodes.add(episode);
            }
        } catch (IOException e) {
            throw new TechnicalException("Failed to parse RSS feed", e);
        }
        
        return episodes;
    }
}
```

This guide provides a comprehensive overview of plugin development for habiTv. For more specific examples and advanced topics, refer to the existing plugins in the `plugins/` directory. 
# habiTv Developer Guide

This guide provides information for developers who want to understand, modify, or extend the habiTv application.

**Last Updated**: June 14, 2025

## Architecture Overview

### Project Structure

```
habitv/
├── application/              # Main application modules
│   ├── core/                # Core business logic
│   │   ├── src/            # Source code
│   │   ├── test/           # Unit tests
│   │   └── pom.xml         # Module POM
│   ├── habiTv/             # Main application
│   ├── habiTv-linux/       # Linux packaging
│   ├── habiTv-windows/     # Windows packaging
│   ├── consoleView/        # CLI interface
│   ├── trayView/           # GUI interface
│   └── pom.xml             # Application POM
├── fwk/                     # Framework modules
│   ├── api/                # Plugin API definitions
│   ├── framework/          # Core framework implementation
│   └── pom.xml             # Framework POM
├── plugins/                 # Content provider plugins
│   ├── arte/              # Arte plugin
│   ├── canalPlus/         # Canal+ plugin
│   ├── ffmpeg/            # FFmpeg export plugin
│   └── ...                # Other plugins
├── docs/                   # Documentation
├── pom.xml                 # Parent POM
└── README.md              # Project overview
```

### Core Components

#### 1. Application Layer
- **Main Application**: Entry point and application lifecycle
- **GUI Interface**: System tray and window management
- **CLI Interface**: Command-line interface
- **Configuration**: Settings and preferences management

#### 2. Core Business Logic
- **Content Management**: Episode discovery and metadata
- **Download Engine**: File download orchestration
- **Plugin System**: Plugin loading and management
- **Export System**: Post-processing and file operations

#### 3. Framework Layer
- **Plugin API**: Interfaces and contracts
- **Common Utilities**: Shared functionality
- **Exception Handling**: Error management
- **Logging**: Application logging

#### 4. Plugin Layer
- **Content Providers**: Content discovery plugins
- **Downloaders**: Download method plugins
- **Exporters**: Post-processing plugins

## Development Environment Setup

### Prerequisites

```bash
# Required tools
Java 8+ (OpenJDK or Oracle)
Maven 3.6+
Git
IDE (IntelliJ IDEA, Eclipse, or VS Code)
```

### IDE Configuration

#### IntelliJ IDEA
1. **Import Project**: Open the root `pom.xml` as a Maven project
2. **Configure SDK**: Set Java 8 as project SDK
3. **Import Maven Projects**: Let IntelliJ import all modules
4. **Run Configuration**: Create run configuration for main class

#### Eclipse
1. **Import Maven Projects**: File → Import → Maven → Existing Maven Projects
2. **Select Root Directory**: Choose the habitv root directory
3. **Configure Java**: Set Java 8 as project JRE
4. **Update Project**: Right-click project → Maven → Update Project

#### VS Code
1. **Install Extensions**: Java Extension Pack, Maven for Java
2. **Open Workspace**: Open the habitv root directory
3. **Configure Java**: Set Java 8 as default runtime
4. **Maven Integration**: Use Maven sidebar for project management

### Build Configuration

#### Maven Settings
```xml
<!-- Parent POM configuration -->
<properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

#### Build Commands
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Install to local repository
mvn install

# Run specific module
cd application/core && mvn exec:java
```

## Core Architecture

### Plugin System

#### Plugin Types

1. **Content Provider Plugins**
   ```java
   public interface PluginProviderInterface {
       String getName();
       Set<CategoryDTO> findCategory() throws TechnicalException;
       Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException;
   }
   ```

2. **Downloader Plugins**
   ```java
   public interface PluginDownloaderInterface {
       String getName();
       void download(DownloadParamDTO downloadParam, 
                    DownloaderPluginHolder downloaders) throws DownloadFailedException;
   }
   ```

3. **Export Plugins**
   ```java
   public interface PluginExporterInterface {
       String getName();
       void export(ExportParamDTO exportParam) throws ExportFailedException;
   }
   ```

#### Plugin Loading
```java
// Plugin discovery and loading
PluginLoader loader = new PluginLoader();
List<PluginProviderInterface> providers = loader.loadProviders();
List<PluginDownloaderInterface> downloaders = loader.loadDownloaders();
List<PluginExporterInterface> exporters = loader.loadExporters();
```

### Configuration Management

#### Configuration Hierarchy
1. **Environment Variables**: Highest priority
2. **System Properties**: JVM arguments
3. **Configuration Files**: XML configuration
4. **Default Values**: Hardcoded defaults

#### Configuration Loading
```java
public class ConfigurationManager {
    public void loadConfiguration() {
        // Load from multiple sources
        loadEnvironmentVariables();
        loadSystemProperties();
        loadConfigurationFiles();
        applyDefaults();
    }
}
```

### Download Engine

#### Download Pipeline
1. **Episode Discovery**: Find available episodes
2. **Download Planning**: Determine download method
3. **Download Execution**: Execute download
4. **Post-Processing**: Apply export operations
5. **Notification**: Notify completion

#### Download States
```java
public enum DownloadState {
    PENDING,      // Queued for download
    DOWNLOADING,  // Currently downloading
    PAUSED,       // Temporarily paused
    COMPLETED,    // Successfully completed
    FAILED,       // Download failed
    CANCELLED     // Manually cancelled
}
```

## Key Classes and Interfaces

### Core Classes

#### HabiTvLauncher
Main application entry point:
```java
public class HabiTvLauncher {
    public static void main(String[] args) {
        // Initialize application
        // Load configuration
        // Start appropriate interface
    }
}
```

#### DownloadManager
Orchestrates download operations:
```java
public class DownloadManager {
    public void startDownload(EpisodeDTO episode) {
        // Find appropriate downloader
        // Execute download
        // Handle completion
    }
}
```

#### PluginManager
Manages plugin lifecycle:
```java
public class PluginManager {
    public void loadPlugins() {
        // Discover plugins
        // Load plugin classes
        // Initialize plugins
    }
}
```

### Data Transfer Objects

#### CategoryDTO
Represents content categories:
```java
public class CategoryDTO {
    private String id;
    private String name;
    private boolean downloadable;
    private Set<CategoryDTO> subCategories;
    // getters and setters
}
```

#### EpisodeDTO
Represents individual episodes:
```java
public class EpisodeDTO {
    private String id;
    private String name;
    private CategoryDTO category;
    private String url;
    // getters and setters
}
```

#### DownloadParamDTO
Download parameters:
```java
public class DownloadParamDTO {
    private String url;
    private String outputPath;
    private String extension;
    // getters and setters
}
```

## Testing Framework

### Unit Testing

#### Test Structure
```java
public class DownloadManagerTest {
    
    @Before
    public void setUp() {
        // Initialize test environment
    }
    
    @Test
    public void testDownloadSuccess() {
        // Test successful download
    }
    
    @Test
    public void testDownloadFailure() {
        // Test download failure
    }
}
```

#### Mocking
```java
@Mock
private PluginDownloaderInterface mockDownloader;

@Test
public void testWithMock() {
    when(mockDownloader.getName()).thenReturn("TestDownloader");
    // Test implementation
}
```

### Integration Testing

#### Plugin Testing
```java
public class PluginIntegrationTest extends BasePluginProviderTester {
    
    @Test
    public void testPluginProvider() throws Exception {
        testPluginProvider(MyPluginProvider.class, false);
    }
}
```

### Test Utilities

#### Test Data Builders
```java
public class TestDataBuilder {
    public static CategoryDTO createTestCategory() {
        CategoryDTO category = new CategoryDTO();
        category.setId("test-category");
        category.setName("Test Category");
        category.setDownloadable(true);
        return category;
    }
}
```

## Build and Deployment

### Maven Configuration

#### Parent POM
```xml
<project>
    <groupId>com.dabi.habitv</groupId>
    <artifactId>parent</artifactId>
    <version>4.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>fwk/api</module>
        <module>fwk/framework</module>
        <module>application/core</module>
        <!-- other modules -->
    </modules>
</project>
```

#### Module POM
```xml
<project>
    <parent>
        <groupId>com.dabi.habitv</groupId>
        <artifactId>parent</artifactId>
        <version>4.1.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>my-module</artifactId>
    <packaging>jar</packaging>
    
    <dependencies>
        <dependency>
            <groupId>com.dabi.habitv</groupId>
            <artifactId>api</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Packaging

#### JAR Creation
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.dabi.habitv.HabiTvLauncher</mainClass>
                <addClasspath>true</addClasspath>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

#### Native Packaging
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <target>
                    <!-- Native packaging configuration -->
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Performance Optimization

### Memory Management

#### Object Pooling
```java
public class ObjectPool<T> {
    private final Queue<T> pool;
    private final Supplier<T> factory;
    
    public T borrow() {
        T obj = pool.poll();
        return obj != null ? obj : factory.get();
    }
    
    public void returnObject(T obj) {
        pool.offer(obj);
    }
}
```

#### Resource Management
```java
public class ResourceManager implements AutoCloseable {
    private final List<AutoCloseable> resources = new ArrayList<>();
    
    public <T extends AutoCloseable> T register(T resource) {
        resources.add(resource);
        return resource;
    }
    
    @Override
    public void close() throws Exception {
        for (AutoCloseable resource : resources) {
            resource.close();
        }
    }
}
```

### Concurrency

#### Thread Pool Management
```java
public class DownloadExecutor {
    private final ExecutorService executor;
    
    public DownloadExecutor(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }
    
    public Future<DownloadResult> submitDownload(DownloadTask task) {
        return executor.submit(task);
    }
}
```

#### Concurrent Collections
```java
public class DownloadQueue {
    private final ConcurrentLinkedQueue<DownloadTask> queue;
    private final Map<String, DownloadState> states;
    
    public DownloadQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.states = new ConcurrentHashMap<>();
    }
}
```

## Debugging and Troubleshooting

### Logging Configuration

#### Log4j Configuration
```xml
<log4j:configuration>
    <appender name="CONSOLE" class="org.apache.log4j.ConsoleAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"/>
        </layout>
    </appender>
    
    <appender name="FILE" class="org.apache.log4j.RollingFileAppender">
        <param name="File" value="${user.home}/habitv/habiTv.log"/>
        <param name="MaxFileSize" value="10MB"/>
        <param name="MaxBackupIndex" value="5"/>
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"/>
        </layout>
    </appender>
    
    <root>
        <priority value="INFO"/>
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</log4j:configuration>
```

### Debug Mode

#### Debug Configuration
```java
public class DebugConfig {
    public static void enableDebugMode() {
        System.setProperty("log4j.logger.com.dabi.habitv", "DEBUG");
        System.setProperty("habitv.debug", "true");
    }
}
```

#### Debug Utilities
```java
public class DebugUtils {
    public static void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        LOG.info("Memory usage: " + usedMemory / 1024 / 1024 + "MB");
    }
}
```

## Security Considerations

### Input Validation

#### URL Validation
```java
public class UrlValidator {
    public static boolean isValidUrl(String url) {
        try {
            new URI(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### File Path Validation
```java
public class PathValidator {
    public static boolean isValidPath(String path) {
        try {
            Path normalized = Paths.get(path).normalize();
            return !normalized.startsWith(Paths.get(".."));
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Secure Configuration

#### Sensitive Data Handling
```java
public class SecureConfig {
    public static String getPassword() {
        // Read from environment variable or secure storage
        return System.getenv("HABITV_PASSWORD");
    }
}
```

## Best Practices

### Code Organization

#### Package Structure
```
com.dabi.habitv.plugin.myplugin/
├── MyPluginProvider.java      # Main plugin class
├── MyPluginDownloader.java    # Downloader implementation
├── MyPluginExporter.java      # Exporter implementation
├── config/                    # Configuration classes
├── model/                     # Data models
├── service/                   # Business logic
└── util/                      # Utility classes
```

#### Naming Conventions
- **Classes**: PascalCase (e.g., `MyPluginProvider`)
- **Methods**: camelCase (e.g., `findCategory()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.dabi.habitv.plugin`)

### Error Handling

#### Exception Hierarchy
```java
public class HabiTvException extends Exception {
    // Base exception for all habiTv errors
}

public class TechnicalException extends HabiTvException {
    // Technical/implementation errors
}

public class DownloadFailedException extends HabiTvException {
    // Download-specific errors
}
```

#### Graceful Degradation
```java
public class RobustPluginProvider implements PluginProviderInterface {
    @Override
    public Set<CategoryDTO> findCategory() throws TechnicalException {
        try {
            return fetchCategories();
        } catch (NetworkException e) {
            LOG.warn("Network error, using cached data", e);
            return getCachedCategories();
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            throw new TechnicalException("Category discovery failed", e);
        }
    }
}
```

This developer guide provides a comprehensive overview of the habiTv codebase. For specific implementation details, refer to the source code and API documentation. 
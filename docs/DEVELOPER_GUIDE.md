# habiTv Developer Guide

This guide provides comprehensive information for developers working on the habiTv project.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Table of Contents

- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Build System](#build-system)
- [Plugin System](#plugin-system)
- [Configuration System](#configuration-system)
- [Download System](#download-system)
- [Core Classes](#core-classes)
- [Testing](#testing)
- [Performance Optimization](#performance-optimization)
- [Debugging](#debugging)
- [Code Quality](#code-quality)

## Getting Started

### Prerequisites

- **Java 8 or higher** (OpenJDK or Oracle JDK)
- **Maven 3.6+** for building
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code)

### Development Environment Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Mika3578/habitv.git
   cd habitv
   ```

2. **Build the Project**:
   ```bash
   mvn clean install
   ```

3. **Run Tests**:
   ```bash
   mvn test
   ```

4. **Launch Application**:
   ```bash
   java -jar target/habiTv.jar
   ```

## Project Structure

### Directory Layout

```
habitv/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── fr/mika3578/habitv/
│   │   │       ├── api/           # Plugin API
│   │   │       ├── core/          # Core application logic
│   │   │       ├── gui/           # GUI components
│   │   │       ├── plugin/        # Plugin management
│   │   │       └── util/          # Utility classes
│   │   └── resources/
│   │       ├── config/            # Configuration files
│   │       └── log4j.xml          # Logging configuration
│   └── test/
│       └── java/                  # Test classes
├── plugins/                       # Plugin modules
│   ├── arte/                      # Arte plugin
│   ├── canalplus/                 # Canal+ plugin
│   └── pluzz/                     # Pluzz plugin
├── docs/                          # Documentation
├── pom.xml                        # Parent POM
└── README.md
```

### Module Structure

Each module follows this structure:

```
module-name/
├── src/
│   ├── main/
│   │   ├── java/                  # Source code
│   │   └── resources/             # Resources
│   └── test/
│       └── java/                  # Test code
├── pom.xml                        # Module POM
└── README.md                      # Module documentation
```

## Build System

### Version Management

#### Module Versioning
The project uses a multi-module structure where each module can have its own version number. This is normal and acceptable for the following reasons:

- **Independent Versioning**: Each module can have its own version number, especially if they are developed or updated independently
- **Backward Compatibility**: Version differences (e.g., 4.1.0 vs 4.1.1) indicate minor updates to specific modules. Since they share the same major version (4.1), they maintain compatibility
- **Parent Version**: The parent project version (4.1.0-SNAPSHOT) serves as a reference point, while child modules can have their own versions
- **Dependency Management**: As long as dependencies between modules are properly managed (using exact versions), version differences won't cause issues

Current version examples:
- Most modules: 4.1.0-SNAPSHOT
- Some modules with updates:
  - beinsport: 4.1.1-SNAPSHOT
  - ffmpeg: 4.1.2-SNAPSHOT
  - footyroom: 4.1.1-SNAPSHOT
  - pluzz: 4.1.1-SNAPSHOT

#### Version Guidelines
- All modules should share the same major version (4.1.x)
- Minor version differences (4.1.0 vs 4.1.1) are acceptable for module-specific updates
- Use exact versions in dependencies rather than version ranges
- Keep the parent version as a reference point
- Document version changes in module-specific README files

### Maven Configuration

The project uses Maven for build management with a multi-module structure.

#### Parent POM

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>fr.mika3578</groupId>
    <artifactId>habitv</artifactId>
    <version>4.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>habiTv</module>
        <module>consoleView</module>
        <module>plugins/arte</module>
        <module>plugins/canalplus</module>
        <module>plugins/pluzz</module>
    </modules>
    
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

#### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JARs
mvn package

# Install to local repository
mvn install

# Skip tests
mvn install -DskipTests
```

## Deployment Workflow

### Overview

The habiTv project uses a local Maven deployment workflow that publishes artifacts to GitHub Pages for distribution. This approach eliminates the need for external FTP/HTTP repositories and provides a secure, reliable distribution mechanism.

### Workflow Steps

1. **Local Maven Deploy**: Run `mvn deploy` to generate all Maven artifacts locally
2. **Artifact Copying**: Copy updated artifacts from `~/.m2/repository` to `docs/repository`
3. **Git Commit**: Commit the updated artifacts to the repository
4. **GitHub Pages**: Push to GitHub Pages for public distribution

### Automated Scripts

The project includes automated scripts to streamline the deployment process:

#### PowerShell Scripts (Windows)

```powershell
# Combined deploy and publish
.\scripts\deploy-and-publish.ps1

# Post-deploy only (after manual mvn deploy)
.\scripts\post-deploy.ps1

# With custom commit message
.\scripts\deploy-and-publish.ps1 -CommitMessage "Release version 4.1.0"
```

#### Bash Scripts (Linux/macOS)

```bash
# Combined deploy and publish
./scripts/deploy-and-publish.sh

# Post-deploy only (after manual mvn deploy)
./scripts/post-deploy.sh

# With custom commit message
./scripts/deploy-and-publish.sh --commit-message "Release version 4.1.0"
```

### Manual Workflow

If you prefer to run the steps manually:

```bash
# Step 1: Deploy to local repository
mvn clean deploy

# Step 2: Copy artifacts to docs/repository
cp -r ~/.m2/repository/com/dabi/habitv/* docs/repository/com/dabi/habitv/

# Step 3: Commit and push
git add docs/repository/com/dabi/habitv/
git commit -m "Deploy Maven artifacts to GitHub Pages"
git push
```

### Configuration

#### Repository Structure

The artifacts are published to:
- **Local Repository**: `~/.m2/repository/com/dabi/habitv/`
- **GitHub Pages**: `https://mika3578.github.io/habitv/repository/com/dabi/habitv/`

#### Maven Configuration

The project has been configured to:
- Remove FTP/HTTP deployment repositories from `pom.xml`
- Remove FTP wagon extensions
- Use local deployment only
- Maintain GitHub Pages repository as the distribution point

### Benefits

- **Security**: HTTPS-only distribution through GitHub Pages
- **Reliability**: GitHub's high-availability infrastructure
- **Version Control**: Git-based artifact management
- **Automation**: Scripted deployment process
- **No External Dependencies**: No need for FTP servers or external repositories

### Troubleshooting

#### Common Issues

1. **Artifacts Not Found**: Ensure `mvn deploy` completed successfully
2. **Git Push Fails**: Check repository permissions and network connectivity
3. **Script Permissions**: On Linux/macOS, ensure scripts are executable (`chmod +x scripts/*.sh`)

#### Verification

After deployment, verify artifacts are available at:
```
https://mika3578.github.io/habitv/repository/com/dabi/habitv/
```

## Plugin Development

### Plugin Structure

```java
package fr.mika3578.habitv.plugin;

import fr.mika3578.habitv.api.HabiTvPlugin;
import fr.mika3578.habitv.api.HabiTvPluginInfo;

@HabiTvPluginInfo(
    name = "Example Plugin",
    version = "1.0.0",
    description = "Example plugin"
)
public class ExamplePlugin implements HabiTvPlugin {
    
    @Override
    public void initialize() {
        // Plugin initialization
    }
    
    @Override
    public void shutdown() {
        // Plugin cleanup
    }
}
```

#### Plugin Loading

```java
// Plugin loading mechanism
PluginManager pluginManager = new PluginManager();
pluginManager.loadPlugins("plugins/");
List<HabiTvPlugin> plugins = pluginManager.getLoadedPlugins();
```

## Configuration System

### Configuration Hierarchy

The configuration system follows this precedence order:

1. **Environment Variables**: Override all other settings
2. **Java System Properties**: Override file-based configuration
3. **Configuration Files**: Application and plugin config files
4. **Default Values**: Built-in application defaults

#### Configuration Loading

```java
// Configuration loading order
Configuration config = new Configuration();

// 1. Load defaults
config.loadDefaults();

// 2. Load configuration files
config.loadFromFile("config.properties");

// 3. Load system properties
config.loadFromSystemProperties();

// 4. Load environment variables
config.loadFromEnvironment();
```

## Download System

### Download Pipeline

The download system follows this pipeline:

1. **Episode Discovery**: Find available episodes
2. **URL Extraction**: Extract download URLs
3. **Download Queue**: Add to download queue
4. **Download Execution**: Execute downloads
5. **Post-Processing**: Apply post-processing

#### Download States

```java
public enum DownloadState {
    PENDING,        // Waiting in queue
    DOWNLOADING,    // Currently downloading
    COMPLETED,      // Download finished
    FAILED,         // Download failed
    CANCELLED       // Download cancelled
}
```

## Core Classes

### HabiTvLauncher

Main application launcher class.

```java
public class HabiTvLauncher {
    public static void main(String[] args) {
        // Initialize application
        HabiTvApplication app = new HabiTvApplication();
        app.start(args);
    }
}
```

#### DownloadManager

Manages download operations.

```java
public class DownloadManager {
    private Queue<DownloadTask> downloadQueue;
    private ExecutorService executor;
    
    public void addDownload(DownloadTask task) {
        downloadQueue.offer(task);
    }
    
    public void startDownload(DownloadTask task) {
        executor.submit(() -> downloadTask(task));
    }
}
```

#### PluginManager

Manages plugin loading and lifecycle.

```java
public class PluginManager {
    private List<HabiTvPlugin> loadedPlugins;
    private Map<String, PluginInfo> pluginInfo;
    
    public void loadPlugins(String pluginDirectory) {
        // Load plugins from directory
    }
    
    public void initializePlugins() {
        // Initialize all loaded plugins
    }
}
```

### Data Transfer Objects

#### CategoryDTO

Represents a content category.

```java
public class CategoryDTO {
    private String name;
    private String url;
    private List<EpisodeDTO> episodes;
    
    // Getters and setters
}
```

#### EpisodeDTO

Represents a video episode.

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

#### DownloadParamDTO

Represents download parameters.

```java
public class DownloadParamDTO {
    private String outputPath;
    private String format;
    private int quality;
    private boolean includeSubtitles;
    
    // Getters and setters
}
```

## Testing

### Test Structure

```java
@ExtendWith(MockitoExtension.class)
class DownloadManagerTest {
    
    @Mock
    private PluginManager pluginManager;
    
    @InjectMocks
    private DownloadManager downloadManager;
    
    @Test
    void testAddDownload() {
        // Test implementation
    }
}
```

#### Mocking

```java
// Mock plugin behavior
when(pluginManager.getPlugin("arte"))
    .thenReturn(mockArtePlugin);

// Verify interactions
verify(pluginManager).loadPlugins(anyString());
```

#### Plugin Testing

```java
@Test
void testArtePlugin() {
    ArtePlugin plugin = new ArtePlugin();
    plugin.initialize();
    
    List<EpisodeDTO> episodes = plugin.getEpisodes();
    assertNotNull(episodes);
    assertFalse(episodes.isEmpty());
}
```

#### Test Data Builders

```java
public class EpisodeDTOBuilder {
    private String title = "Test Episode";
    private String url = "http://example.com/video";
    private String description = "Test description";
    private int duration = 3600;
    
    public EpisodeDTOBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public EpisodeDTO build() {
        EpisodeDTO episode = new EpisodeDTO();
        episode.setTitle(title);
        episode.setUrl(url);
        episode.setDescription(description);
        episode.setDuration(duration);
        return episode;
    }
}
```

### Maven Test Configuration

#### Parent POM

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.8.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>4.5.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Module POM

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M5</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### JAR Creation

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.4</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>fr.mika3578.habitv.HabiTvLauncher</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Native Packaging

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jlink-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <launcher>
            <name>habitv</name>
            <mainClass>fr.mika3578.habitv.HabiTvLauncher</mainClass>
        </launcher>
    </configuration>
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
    
    public void return(T obj) {
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
            try {
                resource.close();
            } catch (Exception e) {
                // Log error but continue closing other resources
            }
        }
    }
}
```

#### Thread Pool Management

```java
public class ThreadPoolManager {
    private final ExecutorService executor;
    
    public ThreadPoolManager(int coreThreads, int maxThreads) {
        executor = new ThreadPoolExecutor(
            coreThreads, maxThreads,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

#### Concurrent Collections

```java
public class DownloadQueue {
    private final ConcurrentLinkedQueue<DownloadTask> queue = 
        new ConcurrentLinkedQueue<>();
    private final Map<String, DownloadState> states = 
        new ConcurrentHashMap<>();
    
    public void addTask(DownloadTask task) {
        queue.offer(task);
        states.put(task.getId(), DownloadState.PENDING);
    }
    
    public DownloadTask getNextTask() {
        return queue.poll();
    }
}
```

### Logging Configuration

#### Log4j Configuration

```xml
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <File name="File" fileName="logs/habitv.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="File"/>
        </Root>
    </Loggers>
</Configuration>
```

## Debugging

### Debug Configuration

```java
public class DebugConfig {
    public static final boolean DEBUG_MODE = 
        Boolean.getBoolean("habitv.debug");
    
    public static void logDebug(String message) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG] " + message);
        }
    }
}
```

#### Debug Utilities

```java
public class DebugUtils {
    public static void dumpThreadInfo() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
        
        for (ThreadInfo info : threadInfos) {
            System.out.println("Thread: " + info.getThreadName());
            System.out.println("State: " + info.getThreadState());
        }
    }
}
```

### Validation Utilities

#### URL Validation

```java
public class ValidationUtils {
    public static boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    public static boolean isAccessibleUrl(String url) {
        try {
            HttpURLConnection connection = 
                (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### File Path Validation

```java
public class FileUtils {
    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }
    
    public static boolean isWritableDirectory(String path) {
        File dir = new File(path);
        return dir.exists() && dir.isDirectory() && dir.canWrite();
    }
}
```

#### Sensitive Data Handling

```java
public class SecurityUtils {
    public static String maskPassword(String password) {
        if (password == null || password.length() <= 2) {
            return "***";
        }
        return password.charAt(0) + 
               "*".repeat(password.length() - 2) + 
               password.charAt(password.length() - 1);
    }
    
    public static String sanitizeUrl(String url) {
        // Remove sensitive parameters
        return url.replaceAll("password=[^&]*", "password=***");
    }
}
```

## Code Quality

### Package Structure

```
fr.mika3578.habitv/
├── api/                    # Public API interfaces
├── core/                   # Core application logic
│   ├── download/           # Download management
│   ├── plugin/             # Plugin system
│   └── config/             # Configuration
├── gui/                    # User interface
├── util/                   # Utility classes
└── exception/              # Custom exceptions
```

#### Naming Conventions

- **Classes**: PascalCase (e.g., `DownloadManager`)
- **Methods**: camelCase (e.g., `startDownload`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase (e.g., `fr.mika3578.habitv.core`)

#### Exception Hierarchy

```java
public class HabiTvException extends Exception {
    public HabiTvException(String message) {
        super(message);
    }
    
    public HabiTvException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class PluginLoadException extends HabiTvException {
    public PluginLoadException(String pluginName, Throwable cause) {
        super("Failed to load plugin: " + pluginName, cause);
    }
}

public class DownloadException extends HabiTvException {
    public DownloadException(String url, Throwable cause) {
        super("Download failed for: " + url, cause);
    }
}
```

#### Graceful Degradation

```java
public class PluginManager {
    public void loadPlugins(String directory) {
        try {
            // Load plugins
            loadPluginFiles(directory);
        } catch (Exception e) {
            logger.warn("Failed to load plugins from: " + directory, e);
            // Continue with available plugins
        }
    }
    
    private void loadPluginFiles(String directory) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("Plugin directory does not exist: " + directory);
            return;
        }
        
        // Load plugin files
    }
}
```

This developer guide provides comprehensive information for working with the habiTv project. Follow these guidelines to maintain code quality and contribute effectively to the project. 
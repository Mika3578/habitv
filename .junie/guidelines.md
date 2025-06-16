# habiTv Development Guidelines

This document provides essential information for developers working on the habiTv project. It includes build/configuration instructions, testing information, and additional development details specific to this project.

## Build/Configuration Instructions

### Building from Source

#### Prerequisites
- Java 8 or higher
- Maven 3.6+
- Git (for cloning the repository)

#### Using Build Scripts (Recommended)

The easiest way to build the executable JAR is to use the provided build scripts:

**Windows:**
```cmd
build_jar.bat
```

**Linux/Unix:**
```bash
chmod +x build_jar.sh
./build_jar.sh
```

These scripts will:
1. Build the executable JAR
2. Copy it to the target directory
3. Display instructions on how to run it

#### Manual Build

Alternatively, you can manually build the project:

```bash
# Clone the repository (if not already done)
git clone https://github.com/mika3578/habitv.git
cd habitv

# Build with Maven
mvn clean compile

# Run tests
mvn test

# Create executable JAR
mvn package

# Build only the executable JAR
cd application\habiTv
mvn clean package
```

The executable JAR will be created at:
`application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar`

### Project Structure

```
habitv/
├── application/          # Main application modules
│   ├── core/            # Core business logic
│   ├── habiTv/          # Main application
│   ├── habiTv-linux/    # Linux packaging
│   ├── habiTv-windows/  # Windows packaging
│   ├── consoleView/     # CLI interface
│   └── trayView/        # GUI interface
├── fwk/                 # Framework modules
│   ├── api/             # Plugin API
│   └── framework/       # Core framework
├── plugins/             # Content provider plugins
│   ├── arte/           # Arte plugin
│   ├── canalPlus/      # Canal+ plugin
│   ├── ffmpeg/         # FFmpeg export plugin
│   └── ...             # Other plugins
├── docs/               # Documentation
└── pom.xml             # Maven parent POM
```

### Configuration

#### Environment Variables

habiTv supports environment variables for configuration. Key variables include:

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_HOME` | Application home directory | `${user.home}/habitv` | `/custom/path/habitv` |
| `HABITV_DOWNLOAD_OUTPUT` | Download output directory | `${HABITV_HOME}/downloads` | `D:\Videos\TV` |
| `HABITV_LOG_LEVEL` | Logging level | `INFO` | `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `HABITV_DEBUG` | Enable debug mode | `false` | `true` |
| `HABITV_DEV_MODE` | Enable development mode | `false` | `true` |

For a complete list of environment variables, see `ENVIRONMENT_VARIABLES.md` in the project root.

#### Configuration Files

- `config.xml`: Main application configuration
- `grabconfig.xml`: Show selection and monitoring configuration

## Testing Information

### Test Structure

Tests in habiTv follow the standard Maven/JUnit structure:

- Tests are located in `test` directories within each module
- The main pom.xml configures `testSourceDirectory` as `${project.basedir}/test`
- JUnit 4 is used for testing (version 4.13.2)

### Running Tests

#### Running All Tests

To run all tests in the project:

```bash
mvn test
```

#### Running Tests for a Specific Module

To run tests for a specific module:

```bash
cd application/core
mvn test
```

#### Running a Specific Test Class

To run a specific test class:

```bash
mvn test -Dtest=FileUtilsTest
```

#### Running a Specific Test Method

To run a specific test method:

```bash
mvn test -Dtest=FileUtilsTest#testSanitizeFilename
```

### Writing Tests

#### Test Base Classes

The project includes several base classes for testing:

- `BasePluginProviderTester`: Base class for testing provider plugins
- `BasePluginUpdateTester`: Base class for testing update functionality

#### Example Test

Here's a simple example of a test class:

```java
package com.dabi.habitv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Simple test class to demonstrate testing in habiTv.
 */
public class SimpleFileUtilsTest {

    /**
     * Test that the sanitizeFilename method correctly handles special characters.
     */
    @Test
    public void testSanitizeFilenameWithSpecialChars() {
        // Test with special characters
        assertEquals("Test_file_name", FileUtils.sanitizeFilename("Test:file/name"));
        
        // Test with spaces
        assertEquals("Test_file_name", FileUtils.sanitizeFilename("Test file name"));
        
        // Test with multiple special characters in a row
        assertEquals("Test__file__name", FileUtils.sanitizeFilename("Test:::file///name"));
        
        // Test with empty string
        assertEquals("", FileUtils.sanitizeFilename(""));
        
        // Test with null (would throw NullPointerException)
        try {
            FileUtils.sanitizeFilename(null);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }
}
```

### Test Configuration

The project uses the Maven Surefire Plugin for running tests with the following configuration:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.2.5</version>
  <configuration>
    <forkedProcessTimeoutInSeconds>30</forkedProcessTimeoutInSeconds>
    <testFailureIgnore>true</testFailureIgnore>
  </configuration>
</plugin>
```

Key settings:
- `forkedProcessTimeoutInSeconds`: 30-second timeout for tests
- `testFailureIgnore`: Tests continue even if some fail

## Additional Development Information

### Code Style

The project follows standard Java code style conventions:

- 4-space indentation
- Camel case for method and variable names
- Pascal case for class names
- Constants in ALL_CAPS
- Use of final for immutable variables

### Logging

The project uses log4j 1.2.15 with a centralized configuration:

- Log levels: DEBUG, INFO, WARN, ERROR
- Default log file: `${user.home}/habitv/habiTv.log`
- Console output: INFO level and above
- File output: DEBUG level and above

To enable debug logging:
1. Set `log4j.rootLogger=DEBUG, console, file` in `log4j.properties`
2. Or set environment variable: `HABITV_LOG_LEVEL=DEBUG`

### Plugin System

habiTv uses a modular plugin architecture:

- **Content Provider Plugins**: Handle episode discovery and metadata
- **Downloader Plugins**: Encapsulate external download tools
- **Export Plugins**: Post-process downloaded videos

When developing new plugins:
1. Implement the appropriate interface from the API module
2. Follow the existing plugin structure
3. Add comprehensive tests
4. See `docs/PLUGIN_DEVELOPMENT.md` for detailed plugin development guide

### External Tools

The application automatically manages several external tools:

- **rtmpDump**: For RTMP stream downloads
- **curl**: For HTTP downloads
- **aria2c**: For high-speed downloads
- **yt-dlp**: For YouTube and other platforms
- **ffmpeg**: For video processing

These tools are downloaded from the configured repository at startup if not already present.

### Debugging Tips

1. **Enable Debug Logging**: Set `HABITV_LOG_LEVEL=DEBUG`
2. **Check Log Files**: Examine `${user.home}/habitv/habiTv.log`
3. **Use JVM Debug Options**: 
   ```
   java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -jar habiTv.jar
   ```
4. **Test Individual Components**: Use unit tests to isolate issues
5. **Verify External Tools**: Check that external tools are correctly installed and accessible

### Common Issues and Solutions

1. **Java Version**: Ensure Java 8+ is installed and in PATH
2. **Download Failures**: Check internet connection and provider availability
3. **Plugin Errors**: Verify external tools (ffmpeg, rtmpDump) are installed
4. **Configuration**: Check `config.xml` and `grabconfig.xml` syntax
5. **Environment Variables**: Verify environment variable configuration
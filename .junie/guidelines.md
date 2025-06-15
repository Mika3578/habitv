# habiTv Development Guidelines

This document provides essential information for developers working on the habiTv project.

## Build/Configuration Instructions

### Prerequisites
- Java 8 or higher
- Maven 3.6 or higher
- Git (for source control)

### Building the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/mika3578/habitv.git
   cd habitv
   ```

2. Build the entire project:
   ```bash
   mvn clean install
   ```

3. Build specific modules:
   ```bash
   mvn clean install -pl fwk,application,plugins
   ```

### Project Structure
- **fwk**: Framework modules including API and core framework
- **application**: Application modules including core, console view, and tray view
- **plugins**: Various plugins for content providers and downloaders
- **repository**: Local Maven repository for dependencies
- **scripts**: Utility scripts for the project

### Configuration
The application uses XML configuration files for various settings:
- Plugin configurations
- Download settings
- Export configurations

## Testing Information

### Running Tests
1. Run all tests:
   ```bash
   mvn test
   ```

2. Run tests for a specific module:
   ```bash
   mvn test -pl fwk/framework
   ```

3. Run a specific test class:
   ```bash
   mvn test -pl fwk/framework -Dtest=OSUtilsTest
   ```

### Writing Tests
1. Test classes should be placed in the `test` directory of the respective module
2. Use JUnit 4 for writing tests
3. Use the `@Test` annotation for test methods
4. Use assertions from `org.junit.Assert` for validations

### Test Base Classes
The project provides base classes for testing different components:
- `BasePluginProviderTester`: Base class for testing provider plugins
- `BasePluginUpdateTester`: Base class for testing updatable plugins

### Example Test
Here's a simple test for the `OSUtils` class:

```java
package com.dabi.habitv.framework.plugin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class OSUtilsTest {
    @Test
    public void testIsWindows() {
        boolean isWindows = OSUtils.isWindows();
        String osName = System.getProperty("os.name").toLowerCase();
        boolean expectedResult = osName.contains("win");
        
        assertEquals("isWindows() should return " + expectedResult + " on " + osName, 
                     expectedResult, isWindows);
    }
}
```

### Testing Plugins
To test a plugin provider:

```java
public class MyPluginTest extends BasePluginProviderTester {
    @Test
    public void testMyPlugin() throws Exception {
        testPluginProvider(MyPlugin.class, true);
    }
}
```

## Additional Development Information

### Code Style
- Use Java 8 features where appropriate
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public classes and methods

### Logging
The project uses Log4j for logging:
- Use appropriate log levels (ERROR, WARN, INFO, DEBUG)
- Include relevant context in log messages
- Configure logging in `log4j.properties`

### Plugin Development
To create a new plugin:
1. Create a new module in the `plugins` directory
2. Implement the appropriate interface:
   - `PluginProviderInterface` for content providers
   - `PluginDownloaderInterface` for downloaders
3. Add the plugin to the parent POM
4. Write tests extending the appropriate base test class

### Debugging
- Use the `[DEBUG_LOG]` prefix for debug messages in tests
- Enable debug logging in `log4j.properties`
- Use the console view for easier debugging

### Deployment
- The project is deployed to GitHub Pages
- Use the `deploy-to-gh-pages.bat` or `deploy-to-gh-pages.ps1` script for deployment
- Version numbers follow semantic versioning (MAJOR.MINOR.PATCH)

### Common Issues
- Ensure external tools (ffmpeg, curl, etc.) are properly configured
- Check proxy settings if downloading fails
- Verify that the plugin is compatible with the current version of the content provider's website
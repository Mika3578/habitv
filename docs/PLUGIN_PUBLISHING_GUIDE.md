# habiTv Plugin Publishing Guide

This guide explains how to publish your habiTv plugin to the official repository so users can automatically download and use it.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Overview

Once you've developed and tested your plugin, you can publish it to the official habiTv repository. This allows all habiTv users to automatically download and use your plugin without manual installation.

## Repository Information

### Repository Details
- **URL**: `http://dabiboo.free.fr/repository`
- **Protocol**: HTTP (plain text, no encryption)
- **Access**: FTP/SFTP upload (credentials required)
- **Contact**: Repository administrator for access

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
            ├── myplugin/
            │   └── 1.0.0/
            │       └── myplugin-1.0.0.jar
            └── pluzz/
                └── 4.1.0-SNAPSHOT/
                    └── pluzz-4.1.0-SNAPSHOT.jar
```

## Prerequisites

### 1. Repository Access
You need write access to the repository server:
- **Contact**: Repository administrator
- **Credentials**: FTP/SFTP username and password
- **Permissions**: Upload to specific plugin directories

### 2. Plugin Requirements
Your plugin must meet these requirements:
- **Compiles Successfully**: No compilation errors
- **Passes Tests**: All unit and integration tests pass
- **Follows Naming**: Uses correct package structure
- **Proper Versioning**: Uses semantic versioning
- **Documentation**: Includes basic documentation

### 3. Development Tools
```bash
# Required tools
Java 8+
Maven 3.6+
Git
FTP/SFTP client (FileZilla, WinSCP, etc.)
```

## Publishing Process

### Step 1: Prepare Your Plugin

#### Build the Plugin
```bash
# Navigate to your plugin directory
cd plugins/myplugin

# Clean and build
mvn clean package -DskipTests

# Verify the JAR was created
ls -la target/myplugin-1.0.0.jar
```

#### Verify JAR Contents
```bash
# Check JAR contents
jar tf target/myplugin-1.0.0.jar

# Should include:
# - META-INF/MANIFEST.MF
# - com/dabi/habitv/plugin/myplugin/
# - plugin.properties (if included)
```

#### Test Locally
```bash
# Copy to local plugins directory
cp target/myplugin-1.0.0.jar ~/habitv/plugins/

# Test with application
java -jar habiTv-4.1.0-SNAPSHOT.jar

# Verify plugin loads correctly
```

### Step 2: Update Repository Files

#### Update plugins.txt
Add your plugin name to `http://dabiboo.free.fr/repository/plugins.txt`:

```text
arte
canalPlus
myplugin
pluzz
```

**Important**: 
- Add your plugin name on a new line
- Use lowercase plugin name
- Don't include version numbers
- Keep alphabetical order if possible

#### Create Plugin Directory Structure
```bash
# Connect to repository server
ftp dabiboo.free.fr
# or
sftp user@dabiboo.free.fr

# Navigate to repository
cd repository

# Create plugin directory structure
mkdir -p com/dabi/habitv/myplugin/1.0.0
```

### Step 3: Upload Plugin Files

#### Upload JAR File
```bash
# Navigate to version directory
cd com/dabi/habitv/myplugin/1.0.0

# Upload the JAR file
put target/myplugin-1.0.0.jar myplugin-1.0.0.jar

# Verify upload
ls -la myplugin-1.0.0.jar
```

#### Verify Accessibility
```bash
# Test HTTP access
curl -I "http://dabiboo.free.fr/repository/com/dabi/habitv/myplugin/1.0.0/myplugin-1.0.0.jar"

# Should return:
# HTTP/1.1 200 OK
# Content-Type: application/java-archive
# Content-Length: [file size]
```

### Step 4: Test Deployment

#### Test with Clean Installation
```bash
# Remove local plugin
rm ~/habitv/plugins/myplugin-1.0.0.jar

# Clear plugin cache (if any)
rm -rf ~/habitv/plugins/cache/

# Start application
java -jar habiTv-4.1.0-SNAPSHOT.jar

# Verify plugin downloads automatically
```

#### Verify Plugin Functionality
1. **Check Plugin List**: Plugin should appear in available plugins
2. **Test Category Discovery**: Should find categories from your plugin
3. **Test Episode Discovery**: Should find episodes in categories
4. **Test Downloads**: Should be able to download episodes

## Version Management

### Project Versioning
The habiTv project uses a multi-module structure with independent versioning:
- Parent version serves as a reference point (currently 4.1.0-SNAPSHOT)
- Individual modules can have their own versions (e.g., 4.1.0-SNAPSHOT, 4.1.1-SNAPSHOT)
- All modules share the same major version for compatibility
- Version differences indicate module-specific updates and improvements

### Semantic Versioning
Use semantic versioning for your plugin:
- **Major.Minor.Patch** (e.g., 1.0.0, 1.1.0, 1.1.1)
- **Major**: Breaking changes
- **Minor**: New features, backward compatible
- **Patch**: Bug fixes, backward compatible

### Version Directory Structure
```text
com/dabi/habitv/myplugin/
├── 1.0.0/
│   └── myplugin-1.0.0.jar
├── 1.1.0/
│   └── myplugin-1.1.0.jar
└── 1.1.1/
    └── myplugin-1.1.1.jar
```

### Updating Existing Plugins

#### For New Versions
1. **Build New Version**: `mvn clean package -DskipTests`
2. **Create Version Directory**: `mkdir com/dabi/habitv/myplugin/1.1.0`
3. **Upload New JAR**: `put target/myplugin-1.1.0.jar 1.1.0/`
4. **Test Deployment**: Verify new version downloads

#### For Bug Fixes
1. **Increment Patch Version**: 1.0.0 → 1.0.1
2. **Build and Upload**: Same process as new version
3. **Keep Old Versions**: Don't delete previous versions

## Configuration

### Plugin Properties
Include a `plugin.properties` file in your JAR:

```properties
plugin.name=MyPlugin
plugin.version=1.0.0
plugin.description=My custom plugin for habiTv
plugin.author=Your Name
plugin.website=https://github.com/your-repo/myplugin
plugin.license=MIT
plugin.category=content-provider
plugin.dependencies=
plugin.minHabiTvVersion=4.1.0
```

### Maven Configuration
```xml
<project>
    <groupId>com.dabi.habitv</groupId>
    <artifactId>myplugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>plugin.properties</include>
                </includes>
            </resource>
        </resources>
    </build>
</project>
```

## Best Practices

### 1. Development

#### Code Quality
- **Follow Naming Conventions**: Use consistent naming
- **Error Handling**: Implement proper exception handling
- **Logging**: Add appropriate log messages
- **Documentation**: Comment complex logic

#### Testing
- **Unit Tests**: Write tests for all functionality
- **Integration Tests**: Test with real websites
- **Edge Cases**: Test error conditions
- **Performance**: Ensure reasonable performance

### 2. Publishing

#### Version Management
- **Semantic Versioning**: Use proper version numbers
- **Changelog**: Document changes between versions
- **Backward Compatibility**: Maintain compatibility
- **Testing**: Test thoroughly before publishing

#### Deployment
- **Staged Rollout**: Consider deploying to test group first
- **Monitoring**: Monitor for issues after deployment
- **Rollback Plan**: Have a plan to quickly rollback
- **Communication**: Notify users of major changes

### 3. Maintenance

#### Updates
- **Regular Updates**: Keep plugins updated
- **Bug Fixes**: Respond quickly to issues
- **Feature Requests**: Consider user feedback
- **Documentation**: Keep documentation updated

#### Monitoring
- **Error Logs**: Monitor for plugin errors
- **Performance**: Watch for performance issues
- **User Feedback**: Collect and respond to feedback
- **Website Changes**: Monitor source websites

## Troubleshooting

### Common Issues

#### Plugin Not Appearing
1. **Check plugins.txt**: Verify plugin name is listed
2. **Check Directory**: Verify correct directory structure
3. **Check JAR**: Verify JAR file is accessible
4. **Check Logs**: Review application logs

#### Plugin Not Downloading
1. **Check URL**: Verify JAR URL is correct
2. **Check Permissions**: Verify file permissions
3. **Check Network**: Verify network connectivity
4. **Check Cache**: Clear application cache

#### Plugin Not Working
1. **Check Dependencies**: Verify all dependencies included
2. **Check Version**: Verify version compatibility
3. **Check Configuration**: Verify plugin configuration
4. **Check Logs**: Review plugin-specific logs

### Debugging

#### Enable Debug Logging
```xml
<!-- In log4j.properties -->
log4j.logger.com.dabi.habitv.provider.myplugin=DEBUG
log4j.logger.com.dabi.habitv.plugin.update=DEBUG
```

#### Test Repository Access
```bash
# Test HTTP access
curl -I "http://dabiboo.free.fr/repository/plugins.txt"
curl -I "http://dabiboo.free.fr/repository/com/dabi/habitv/myplugin/1.0.0/myplugin-1.0.0.jar"

# Test FTP access
ftp dabiboo.free.fr
ls repository/com/dabi/habitv/myplugin/
```

#### Check Application Logs
```bash
# View application logs
tail -f ~/habitv/habiTv.log

# Look for plugin-related messages
grep -i myplugin ~/habitv/habiTv.log
grep -i "plugin.*download" ~/habitv/habiTv.log
```

## Security Considerations

### Repository Security
- **Plain HTTP**: Repository uses HTTP (not HTTPS)
- **No Authentication**: No authentication for downloads
- **Public Access**: Repository is publicly accessible
- **No Signing**: Plugins are not digitally signed

### Plugin Security
- **Code Review**: Review plugin code before publishing
- **Dependencies**: Verify all dependencies are safe
- **Permissions**: Limit plugin permissions when possible
- **Updates**: Keep plugins updated for security

### Best Practices
- **Trusted Sources**: Only publish plugins from trusted sources
- **Code Review**: Review all plugin code
- **Testing**: Test plugins thoroughly
- **Monitoring**: Monitor for security issues

## Future Enhancements

### Planned Improvements
1. **Plugin Marketplace**: Web interface for plugin discovery
2. **Automated Testing**: CI/CD pipeline for plugin testing
3. **Plugin Signing**: Digital signatures for plugin verification
4. **Update Notifications**: Notify users of plugin updates
5. **Plugin Metrics**: Track plugin usage and performance

### Development Tools
1. **Plugin Generator**: Tool to generate plugin templates
2. **Plugin Validator**: Tool to validate plugin structure
3. **Plugin Simulator**: Tool to test plugins without deployment
4. **Plugin Documentation**: Automated documentation generation

## Support and Resources

### Getting Help
- **Repository Issues**: Contact repository administrator
- **Plugin Development**: Refer to plugin development guide
- **Community**: Ask questions in habiTv community
- **Documentation**: Review all documentation

### Useful Links
- **Plugin Development Guide**: [docs/PLUGIN_DEVELOPMENT.md](PLUGIN_DEVELOPMENT.md)
- **Plugin Examples**: [docs/PLUGIN_EXAMPLES.md](PLUGIN_EXAMPLES.md)
- **API Reference**: [docs/API_REFERENCE.md](API_REFERENCE.md)
- **Repository**: http://dabiboo.free.fr/repository

This guide provides comprehensive information for publishing habiTv plugins. Follow these steps carefully to ensure successful deployment and distribution of your plugins. 
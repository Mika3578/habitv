# habiTv Troubleshooting Guide

This guide helps you resolve common issues and problems with habiTv.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 14, 2025

## Common Issues

### Application Won't Start

#### Java Version Issues

**Problem**: Application fails to start with Java version errors.

**Symptoms**:
- Error messages about Java version
- "UnsupportedClassVersionError" exceptions
- Application exits immediately

**Solutions**:

1. **Check Java Version**:
   ```bash
   java -version
   ```

2. **Install Correct Java Version**:
   - habiTv requires Java 8 or higher
   - Download from Oracle or OpenJDK

3. **Set JAVA_HOME**:
   ```bash
   # Windows
   set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx
   
   # Linux/macOS
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
   ```

#### Missing Dependencies

**Problem**: Application fails due to missing libraries.

**Symptoms**:
- "ClassNotFoundException" errors
- "NoClassDefFoundError" exceptions
- Missing JAR file errors

**Solutions**:

1. **Reinstall Application**:
   ```bash
   # Clean and rebuild
   mvn clean install
   ```

2. **Check JAR Integrity**:
   ```bash
   # Verify JAR contents
   jar -tf habiTv.jar
   ```

3. **Manual Dependency Installation**:
   - Download missing dependencies
   - Place in lib/ directory

### Plugin Issues

#### Plugin Not Loading

**Problem**: Plugins fail to load or are not recognized.

**Symptoms**:
- Plugin not appearing in list
- "PluginLoadException" errors
- Missing plugin functionality

**Solutions**:

1. **Check Plugin Directory**:
   ```bash
   # Verify plugin location
   ls -la plugins/
   ```

2. **Check Plugin Permissions**:
   ```bash
   # Set correct permissions
   chmod 644 plugins/*.jar
   ```

3. **Validate Plugin Structure**:
   ```bash
   # Check JAR contents
   jar -tf plugin-name.jar
   ```

4. **Check Plugin Dependencies**:
   - Ensure all required libraries are available
   - Check for version conflicts

#### Plugin Update Failures

**Problem**: Plugins fail to update automatically.

**Symptoms**:
- Update errors in logs
- Outdated plugin versions
- Network connection issues

**Solutions**:

1. **Check Network Connection**:
   ```bash
   # Test repository access
   curl -I http://mika3578.free.fr/habitv/
   ```

2. **Clear Plugin Cache**:
   ```bash
   # Remove cached plugins
   rm -rf plugins/cache/
   ```

3. **Manual Plugin Update**:
   - Download latest plugin version
   - Replace existing plugin file

4. **Check Repository Status**:
   - Verify repository is accessible
   - Check plugins.txt file

#### Plugin Compatibility Issues

**Problem**: Plugins work incorrectly or cause errors.

**Symptoms**:
- Unexpected behavior
- Application crashes
- Error messages from plugins

**Solutions**:

1. **Check Plugin Version**:
   - Ensure plugin version is compatible
   - Update to latest version

2. **Review Plugin Logs**:
   ```bash
   # Check application logs
   tail -f logs/habiTv.log
   ```

3. **Disable Problematic Plugin**:
   ```bash
   # Rename plugin to disable
   mv plugin.jar plugin.jar.disabled
   ```

4. **Report Plugin Issues**:
   - Contact plugin developer
   - Check for known issues

### Download Issues

#### Downloads Fail to Start

**Problem**: Downloads don't begin or fail immediately.

**Symptoms**:
- Download queue empty
- Immediate failure messages
- No download progress

**Solutions**:

1. **Check Internet Connection**:
   ```bash
   # Test connectivity
   ping google.com
   ```

2. **Verify URL Accessibility**:
   ```bash
   # Test URL directly
   curl -I "video-url"
   ```

3. **Check Download Directory**:
   ```bash
   # Verify directory exists and is writable
   ls -la downloads/
   ```

4. **Check Disk Space**:
   ```bash
   # Check available space
   df -h
   ```

#### Downloads Stop or Fail

**Problem**: Downloads start but fail or stop unexpectedly.

**Symptoms**:
- Partial downloads
- Network timeout errors
- Incomplete files

**Solutions**:

1. **Check Network Stability**:
   - Test with different network
   - Check for firewall issues

2. **Increase Timeout Values**:
   ```properties
   # In config.properties
   download.timeout=300000
   connection.timeout=60000
   ```

3. **Resume Downloads**:
   - Restart application
   - Retry failed downloads

4. **Check File Permissions**:
   ```bash
   # Ensure write permissions
   chmod 755 downloads/
   ```

#### Slow Download Speeds

**Problem**: Downloads are very slow or take too long.

**Symptoms**:
- Very slow progress
- Low bandwidth utilization
- Timeout issues

**Solutions**:

1. **Check Bandwidth**:
   ```bash
   # Test download speed
   curl -o /dev/null -s -w "%{speed_download}\n" "test-url"
   ```

2. **Optimize Network Settings**:
   ```properties
   # In config.properties
   download.threads=4
   download.buffer.size=8192
   ```

3. **Use Different Network**:
   - Try different connection
   - Check for bandwidth limits

4. **Check Server Performance**:
   - Try downloading at different times
   - Check if server is overloaded

### GUI Issues

#### System Tray Not Working

**Problem**: System tray icon doesn't appear or work.

**Symptoms**:
- No tray icon visible
- Right-click menu doesn't work
- Application not accessible

**Solutions**:

1. **Check System Tray Support**:
   - Verify OS supports system tray
   - Check for tray icon settings

2. **Restart Application**:
   ```bash
   # Kill and restart
   pkill -f habiTv
   java -jar habiTv.jar
   ```

3. **Check Display Settings**:
   - Ensure tray is visible
   - Check for hidden icons

4. **Alternative Launch**:
   ```bash
   # Launch with GUI disabled
   java -jar habiTv.jar --no-gui
   ```

#### GUI Freezes or Crashes

**Problem**: GUI becomes unresponsive or crashes.

**Symptoms**:
- Interface freezes
- No response to clicks
- Application crashes

**Solutions**:

1. **Check Memory Usage**:
   ```bash
   # Monitor memory
   jps -l
   jstat -gc <pid>
   ```

2. **Increase Memory Allocation**:
   ```bash
   # Launch with more memory
   java -Xmx2g -jar habiTv.jar
   ```

3. **Check for Resource Leaks**:
   - Monitor system resources
   - Check for memory leaks

4. **Use CLI Alternative**:
   ```bash
   # Use command-line interface
   java -jar consoleView.jar
   ```

### Configuration Issues

#### Configuration Not Loading

**Problem**: Application doesn't use custom configuration.

**Symptoms**:
- Default settings always used
- Configuration changes ignored
- Missing configuration file

**Solutions**:

1. **Check Configuration File**:
   ```bash
   # Verify config file exists
   ls -la config.properties
   ```

2. **Validate Configuration Format**:
   ```properties
   # Example valid configuration
   download.directory=./downloads
   gui.enabled=true
   log.level=INFO
   ```

3. **Check File Permissions**:
   ```bash
   # Ensure readable permissions
   chmod 644 config.properties
   ```

4. **Use Command Line Options**:
   ```bash
   # Override with command line
   java -jar habiTv.jar --config=myconfig.properties
   ```

#### Environment Variables Not Working

**Problem**: Environment variables are not recognized.

**Symptoms**:
- Environment settings ignored
- Default values used
- Configuration errors

**Solutions**:

1. **Check Environment Variables**:
   ```bash
   # List environment variables
   env | grep HABITV
   ```

2. **Set Variables Correctly**:
   ```bash
   # Windows
   set HABITV_DOWNLOAD_DIR=C:\Downloads
   
   # Linux/macOS
   export HABITV_DOWNLOAD_DIR=/home/user/downloads
   ```

3. **Restart Application**:
   - Environment changes require restart
   - Verify variables are set

4. **Check Variable Names**:
   - Use correct variable names
   - Check for typos

### Performance Issues

#### High Memory Usage

**Problem**: Application uses too much memory.

**Symptoms**:
- Slow performance
- Out of memory errors
- System becomes unresponsive

**Solutions**:

1. **Monitor Memory Usage**:
   ```bash
   # Check memory usage
   jps -l
   jstat -gc <pid>
   ```

2. **Adjust Memory Settings**:
   ```bash
   # Limit memory usage
   java -Xmx512m -jar habiTv.jar
   ```

3. **Optimize Configuration**:
   ```properties
   # Reduce memory usage
   download.buffer.size=4096
   cache.size=100
   ```

4. **Check for Memory Leaks**:
   - Monitor over time
   - Restart if necessary

#### Slow Application Performance

**Problem**: Application is slow or unresponsive.

**Symptoms**:
- Slow startup
- Delayed responses
- High CPU usage

**Solutions**:

1. **Check System Resources**:
   ```bash
   # Monitor system resources
   top
   htop
   ```

2. **Optimize Configuration**:
   ```properties
   # Performance settings
   download.threads=2
   gui.update.interval=1000
   ```

3. **Reduce Logging**:
   ```properties
   # Minimize logging
   log.level=WARN
   log.file.enabled=false
   ```

4. **Check for Resource Conflicts**:
   - Close other applications
   - Check for antivirus interference

### Network Issues

#### Connection Problems

**Problem**: Application can't connect to network resources.

**Symptoms**:
- Network timeout errors
- Connection refused errors
- No internet access

**Solutions**:

1. **Check Network Connectivity**:
   ```bash
   # Test basic connectivity
   ping google.com
   curl -I http://example.com
   ```

2. **Check Firewall Settings**:
   - Allow application through firewall
   - Check antivirus settings

3. **Configure Proxy**:
   ```properties
   # Proxy configuration
   proxy.host=proxy.example.com
   proxy.port=8080
   proxy.username=user
   proxy.password=pass
   ```

4. **Check DNS Settings**:
   ```bash
   # Test DNS resolution
   nslookup example.com
   ```

#### SSL/TLS Issues

**Problem**: SSL certificate or TLS connection problems.

**Symptoms**:
- SSL handshake errors
- Certificate validation failures
- TLS version errors

**Solutions**:

1. **Update Java Version**:
   - Use latest Java version
   - Ensure TLS 1.2+ support

2. **Configure SSL Settings**:
   ```properties
   # SSL configuration
   ssl.truststore.path=/path/to/truststore
   ssl.truststore.password=password
   ```

3. **Bypass Certificate Validation** (Development only):
   ```properties
   # Disable certificate validation
   ssl.verify=false
   ```

4. **Update Certificates**:
   - Update system certificates
   - Import required certificates

### Logging and Debugging

#### Enable Debug Logging

**Problem**: Need more detailed information for troubleshooting.

**Solutions**:

1. **Set Debug Log Level**:
   ```properties
   # Enable debug logging
   log.level=DEBUG
   log.file.enabled=true
   ```

2. **Enable Plugin Debugging**:
   ```properties
   # Plugin debug settings
   plugin.debug.enabled=true
   plugin.log.level=DEBUG
   ```

3. **Enable Network Debugging**:
   ```properties
   # Network debug settings
   network.debug.enabled=true
   http.log.enabled=true
   ```

4. **View Logs**:
   ```bash
   # Monitor logs in real-time
   tail -f logs/habiTv.log
   ```

#### Common Error Messages

**Problem**: Understanding error messages and their solutions.

**Common Errors**:

1. **"NoClassDefFoundError"**:
   - Missing dependency
   - Corrupted JAR file
   - Classpath issues

2. **"OutOfMemoryError"**:
   - Increase heap size
   - Reduce memory usage
   - Check for memory leaks

3. **"ConnectionTimeoutException"**:
   - Network connectivity issues
   - Firewall blocking
   - Server unavailable

4. **"FileNotFoundException"**:
   - Missing file or directory
   - Permission issues
   - Path configuration errors

### Getting Help

#### Self-Help Resources

1. **Check Documentation**:
   - Read user guide
   - Review API reference
   - Check plugin documentation

2. **Search Issues**:
   - Look for similar problems
   - Check known issues
   - Review troubleshooting guides

3. **Test in Isolation**:
   - Test with minimal configuration
   - Disable plugins
   - Use different environment

#### Community Support

1. **GitHub Issues**:
   - Search existing issues
   - Create new issue with details
   - Provide logs and configuration

2. **Developer Documentation**:
   - Review developer guide
   - Check contributing guidelines
   - Read plugin development guide

3. **Contact Information**:
   - GitHub repository
   - Developer contact
   - Community forums

#### Reporting Issues

When reporting issues, include:

1. **System Information**:
   - Operating system and version
   - Java version
   - habiTv version

2. **Error Details**:
   - Complete error messages
   - Stack traces
   - Log files

3. **Reproduction Steps**:
   - Detailed steps to reproduce
   - Configuration used
   - Expected vs actual behavior

4. **Additional Context**:
   - Plugins installed
   - Network environment
   - Recent changes

This troubleshooting guide covers the most common issues and their solutions. For additional help, refer to the documentation or contact the development team. 
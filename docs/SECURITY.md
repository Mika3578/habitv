# habiTv Security Guide

This document outlines security policies, practices, and considerations for the habiTv application.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Security Overview

habiTv is designed with security in mind, but users should be aware of the security implications of downloading content from various sources and running third-party plugins.

## Security Architecture

### Application Security

#### Code Execution
- **Java Security Manager**: Not currently implemented
- **Plugin Isolation**: Plugins run in the same JVM as the main application
- **File System Access**: Plugins have access to the local file system
- **Network Access**: Plugins can make network requests

#### Data Protection
- **Configuration Files**: Stored in plain text
- **Log Files**: May contain sensitive information
- **Download History**: Stored locally without encryption
- **Plugin Data**: Plugin-specific data stored locally

### Network Security

#### Repository Access
```text
Repository URL: http://dabiboo.free.fr/repository
Protocol: HTTP (plain text, no encryption)
Authentication: None for downloads
Upload Access: FTP/SFTP with credentials
```

#### Content Downloads
- **HTTP/HTTPS**: Supports both protocols
- **No Certificate Validation**: Basic SSL/TLS validation
- **Proxy Support**: HTTP proxy configuration available
- **User Agent**: Configurable user agent string

#### Plugin Downloads
```bash
# Plugin download URL format
http://dabiboo.free.fr/repository/com/dabi/habitv/{plugin}/{version}/{plugin}-{version}.jar

# Example
http://dabiboo.free.fr/repository/com/dabi/habitv/arte/4.1.0-SNAPSHOT/arte-4.1.0-SNAPSHOT.jar
```

## Security Risks

### 1. Plugin Security

#### Risk Level: **HIGH**
- **Code Execution**: Plugins run arbitrary Java code
- **File System Access**: Plugins can read/write files
- **Network Access**: Plugins can make network requests
- **No Sandboxing**: Plugins run with full application permissions

#### Mitigation Strategies
```java
// Example: Validate plugin before loading
public class PluginSecurityValidator {
    
    public boolean validatePlugin(File pluginJar) {
        // Check JAR signature (if implemented)
        if (!verifySignature(pluginJar)) {
            return false;
        }
        
        // Check for suspicious classes
        if (containsSuspiciousClasses(pluginJar)) {
            return false;
        }
        
        // Check file permissions
        if (!hasSafePermissions(pluginJar)) {
            return false;
        }
        
        return true;
    }
}
```

### 2. Network Security

#### Risk Level: **MEDIUM**
- **Plain HTTP**: Repository uses HTTP without encryption
- **No Authentication**: No authentication for downloads
- **Man-in-the-Middle**: Potential for interception
- **DNS Spoofing**: Potential for DNS-based attacks

#### Mitigation Strategies
```properties
# Environment variables for secure configuration
HABITV_VERIFY_SSL=true
HABITV_CHECKSUM_VERIFICATION=true
HABITV_TRUSTED_REPOSITORIES=http://dabiboo.free.fr/repository
```

### 3. File System Security

#### Risk Level: **MEDIUM**
- **Local Storage**: All data stored locally
- **No Encryption**: Configuration and data not encrypted
- **Permission Issues**: May create files with unsafe permissions
- **Path Traversal**: Potential for path traversal attacks

#### Mitigation Strategies
```java
// Example: Safe file path validation
public class PathSecurityValidator {
    
    public boolean isValidPath(String path) {
        try {
            Path normalized = Paths.get(path).normalize();
            
            // Prevent path traversal
            if (normalized.startsWith(Paths.get(".."))) {
                return false;
            }
            
            // Check for suspicious patterns
            if (path.contains("..") || path.contains("~")) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Security Configuration

### Environment Variables

#### Security-Related Variables
```properties
# SSL/TLS Configuration
HABITV_VERIFY_SSL=true                    # Enable SSL certificate verification
HABITV_TRUST_ALL_CERTS=false             # Don't trust all certificates
HABITV_SSL_PROTOCOLS=TLSv1.2,TLSv1.3     # Allowed SSL protocols

# Plugin Security
HABITV_PLUGIN_SIGNING_REQUIRED=false     # Require plugin signatures
HABITV_PLUGIN_SANDBOX=false              # Enable plugin sandboxing
HABITV_PLUGIN_NETWORK_ACCESS=true        # Allow plugin network access
HABITV_PLUGIN_FILE_ACCESS=true           # Allow plugin file access

# Repository Security
HABITV_REPOSITORY_URL=http://dabiboo.free.fr/repository
HABITV_REPOSITORY_VERIFICATION=true      # Verify repository integrity
HABITV_CHECKSUM_VERIFICATION=true        # Verify file checksums

# Logging Security
HABITV_LOG_SENSITIVE_DATA=false          # Don't log sensitive data
HABITV_LOG_LEVEL=INFO                    # Appropriate log level
HABITV_LOG_FILE_PERMISSIONS=600          # Secure log file permissions
```

### Configuration Files

#### Security Settings in config.xml
```xml
<configuration>
    <security>
        <ssl>
            <verifyCertificates>true</verifyCertificates>
            <trustAllCertificates>false</trustAllCertificates>
            <allowedProtocols>TLSv1.2,TLSv1.3</allowedProtocols>
        </ssl>
        
        <plugins>
            <signingRequired>false</signingRequired>
            <sandboxEnabled>false</sandboxEnabled>
            <networkAccess>true</networkAccess>
            <fileAccess>true</fileAccess>
        </plugins>
        
        <repository>
            <verificationEnabled>true</verificationEnabled>
            <checksumVerification>true</checksumVerification>
        </repository>
        
        <logging>
            <logSensitiveData>false</logSensitiveData>
            <logLevel>INFO</logLevel>
            <secureFilePermissions>true</secureFilePermissions>
        </logging>
    </security>
</configuration>
```

## Best Practices

### 1. For Users

#### Plugin Safety
- **Trusted Sources**: Only use plugins from trusted sources
- **Code Review**: Review plugin code if possible
- **Regular Updates**: Keep plugins updated
- **Monitor Activity**: Watch for unusual behavior

#### Network Safety
- **Use HTTPS**: Prefer HTTPS when available
- **Verify URLs**: Double-check download URLs
- **Use VPN**: Consider using VPN for additional security
- **Monitor Traffic**: Watch for unusual network activity

#### File System Safety
- **Secure Permissions**: Set appropriate file permissions
- **Regular Backups**: Backup important data
- **Monitor Changes**: Watch for unexpected file changes
- **Use Antivirus**: Run antivirus software

### 2. For Developers

#### Plugin Development
```java
// Example: Secure plugin implementation
public class SecurePluginProvider implements PluginProviderInterface {
    
    private final PathSecurityValidator pathValidator;
    private final NetworkSecurityValidator networkValidator;
    
    public SecurePluginProvider() {
        this.pathValidator = new PathSecurityValidator();
        this.networkValidator = new NetworkSecurityValidator();
    }
    
    @Override
    public Set<EpisodeDTO> findEpisode(CategoryDTO category) throws TechnicalException {
        // Validate input
        if (!pathValidator.isValidPath(category.getUrl())) {
            throw new TechnicalException("Invalid URL path");
        }
        
        // Validate network access
        if (!networkValidator.isAllowedUrl(category.getUrl())) {
            throw new TechnicalException("URL not allowed");
        }
        
        // Implement secure episode discovery
        return fetchEpisodesSecurely(category);
    }
}
```

#### Code Security
- **Input Validation**: Validate all inputs
- **Output Encoding**: Encode all outputs
- **Error Handling**: Don't expose sensitive information
- **Logging**: Don't log sensitive data

### 3. For Administrators

#### System Configuration
```bash
# Secure file permissions
chmod 600 ~/habitv/config.xml
chmod 600 ~/habitv/grabconfig.xml
chmod 755 ~/habitv/plugins/
chmod 644 ~/habitv/plugins/*.jar

# Secure log files
chmod 600 ~/habitv/habiTv.log
chmod 600 ~/habitv/update.log

# Secure download directory
chmod 755 ~/habitv/downloads/
```

#### Network Configuration
```bash
# Configure firewall rules
iptables -A OUTPUT -p tcp --dport 80 -d dabiboo.free.fr -j ACCEPT
iptables -A OUTPUT -p tcp --dport 443 -j ACCEPT
iptables -A OUTPUT -p tcp --dport 21 -j DROP  # Block FTP
iptables -A OUTPUT -p tcp --dport 22 -j DROP  # Block SSH

# Configure proxy if needed
export HTTP_PROXY=http://proxy.company.com:8080
export HTTPS_PROXY=http://proxy.company.com:8080
```

## Incident Response

### Security Incident Types

#### 1. Malicious Plugin
**Symptoms:**
- Unexpected file creation
- Unusual network activity
- System performance issues
- Unauthorized data access

**Response:**
```bash
# Immediate actions
1. Stop habiTv application
2. Remove suspicious plugin JAR
3. Check system for unauthorized changes
4. Review application logs
5. Scan system with antivirus
6. Report incident to repository administrator
```

#### 2. Network Compromise
**Symptoms:**
- Failed downloads
- SSL certificate errors
- Unusual network traffic
- DNS resolution issues

**Response:**
```bash
# Immediate actions
1. Check network connectivity
2. Verify repository URL
3. Check DNS resolution
4. Review firewall rules
5. Contact network administrator
6. Consider using VPN
```

#### 3. Data Breach
**Symptoms:**
- Unauthorized file access
- Modified configuration files
- Unexpected log entries
- System account changes

**Response:**
```bash
# Immediate actions
1. Stop all applications
2. Disconnect from network
3. Preserve evidence
4. Review access logs
5. Change passwords
6. Contact security team
```

### Reporting Security Issues

#### Contact Information
- **Repository Issues**: Repository administrator
- **Application Issues**: habiTv development team
- **Plugin Issues**: Plugin developer
- **Security Vulnerabilities**: Security team

#### Information to Include
- **Description**: Clear description of the issue
- **Steps to Reproduce**: Detailed reproduction steps
- **Environment**: Operating system, Java version, habiTv version
- **Logs**: Relevant log entries
- **Impact**: Potential security impact
- **Suggested Fix**: If you have suggestions

## Security Updates

### Update Process

#### Automatic Updates
- **Plugin Updates**: Automatic on application startup
- **Application Updates**: Manual download required
- **Security Patches**: Included in regular updates
- **Vulnerability Fixes**: Prioritized in updates

#### Manual Updates
```bash
# Download latest version
wget https://github.com/habitv/habitv/releases/latest/download/habiTv.jar

# Verify checksum
sha256sum habiTv.jar
# Compare with published checksum

# Backup current version
cp habiTv.jar habiTv.jar.backup

# Install new version
mv habiTv.jar ~/habitv/
```

### Security Notifications

#### Notification Channels
- **GitHub Issues**: Security advisories
- **Release Notes**: Security-related changes
- **Email Alerts**: Critical security issues
- **Community Forums**: Security discussions

#### Stay Informed
- **Watch Repository**: GitHub notifications
- **Join Community**: User forums and discussions
- **Follow Updates**: Regular update checks
- **Security News**: General security awareness

## Compliance

### Data Protection

#### Personal Data
- **No Collection**: habiTv doesn't collect personal data
- **Local Storage**: All data stored locally
- **No Transmission**: No data transmitted to external servers
- **User Control**: Users control all their data

#### Privacy
- **No Tracking**: No user tracking or analytics
- **No Profiling**: No user profiling
- **No Sharing**: No data sharing with third parties
- **Transparency**: Open source code for transparency

### Regulatory Compliance

#### GDPR (EU)
- **Data Minimization**: Minimal data collection
- **User Rights**: Full user control over data
- **Transparency**: Clear data handling practices
- **Security**: Appropriate security measures

#### CCPA (California)
- **No Selling**: No data selling
- **User Rights**: Right to know and delete
- **Transparency**: Clear privacy practices
- **Security**: Reasonable security measures

## Future Security Enhancements

### Planned Improvements

#### 1. Plugin Security
- **Digital Signatures**: Plugin signing and verification
- **Sandboxing**: Plugin isolation and sandboxing
- **Permission System**: Granular permission controls
- **Code Review**: Automated code analysis

#### 2. Network Security
- **HTTPS Repository**: Secure repository access
- **Certificate Pinning**: Certificate verification
- **Encrypted Communication**: End-to-end encryption
- **Secure Protocols**: Modern security protocols

#### 3. Application Security
- **Security Manager**: Java Security Manager implementation
- **Input Validation**: Enhanced input validation
- **Output Encoding**: Comprehensive output encoding
- **Secure Logging**: Secure logging practices

#### 4. System Security
- **File Encryption**: Encrypted configuration storage
- **Secure Permissions**: Enhanced file permissions
- **Access Controls**: User access controls
- **Audit Logging**: Comprehensive audit logging

This security guide provides comprehensive information about habiTv security. Follow these guidelines to ensure secure usage and development of the application. 
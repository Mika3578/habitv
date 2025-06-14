# habiTv GitHub Pages HTTPS Mirror

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Overview

This document describes the GitHub Pages HTTPS mirror of the habiTv external tools repository, providing a secure and reliable alternative to the original HTTP repository.

## Repository Information

### Primary Repository (Original)
- **URL**: `http://dabiboo.free.fr/repository`
- **Protocol**: HTTP (unencrypted)
- **Status**: Original repository

### GitHub Pages Mirror (New)
- **URL**: `https://mika3578.github.io/habitv/repository/`
- **Protocol**: HTTPS (encrypted)
- **Status**: Secure mirror with automatic updates
- **Type**: Maven repository mirror
- **Structure**: Standard Maven repository layout with `com/dabi/habitv` group ID

## Benefits of GitHub Pages Mirror

### Security Improvements
- **HTTPS Encryption**: All downloads are encrypted
- **Certificate Validation**: Automatic SSL certificate management
- **Integrity Protection**: Reduced risk of tampering
- **Modern Standards**: Complies with security best practices

### Reliability Improvements
- **GitHub Infrastructure**: High availability and global CDN
- **Automatic Scaling**: Handles traffic spikes automatically
- **Geographic Distribution**: Multiple data centers worldwide
- **Uptime Guarantee**: GitHub's 99.9% uptime SLA

### Maintenance Benefits
- **Version Control**: Git-based repository management
- **Automation**: GitHub Actions integration for auto-updates
- **Collaboration**: Multiple maintainers can contribute
- **Backup**: Automatic backup and version history

## Repository Structure

### Maven Artifacts
The repository contains Maven artifacts organized by group ID and artifact ID:

```
https://mika3578.github.io/habitv/repository/
├── com/dabi/habitv/        # Group ID: com.dabi.habitv
│   ├── arte/               # Arte plugin
│   │   └── 4.1.0-SNAPSHOT/ # Version directory
│   ├── youtube/            # YouTube plugin
│   │   └── 4.1.0-SNAPSHOT/ # Version directory
│   └── ...                 # Other plugins
├── bin/                    # External tool binaries
├── metadata/               # Version metadata
└── plugins.txt             # Tool list
```

### External Tools
External tools are stored in the `bin/` directory:

```
bin/
├── yt-dlp.exe.zip         # Windows yt-dlp
├── yt-dlp.zip             # Linux yt-dlp
├── ffmpeg.exe.zip         # Windows ffmpeg
├── ffmpeg.zip             # Linux ffmpeg
├── aria2c.exe.zip         # Windows aria2c
├── aria2c.zip             # Linux aria2c
├── curl.exe.zip           # Windows curl
├── curl.zip               # Linux curl
├── rtmpdump.exe.zip       # Windows rtmpdump
└── rtmpdump.zip           # Linux rtmpdump
```

### Required Directory Structure
```
repository/
├── bin/                    # Tool binaries
│   ├── yt-dlp.exe.zip     # Windows yt-dlp
│   ├── yt-dlp.zip         # Linux yt-dlp
│   ├── ffmpeg.exe.zip     # Windows ffmpeg
│   ├── ffmpeg.zip         # Linux ffmpeg
│   ├── aria2c.exe.zip     # Windows aria2c
│   ├── aria2c.zip         # Linux aria2c
│   ├── curl.exe.zip       # Windows curl
│   ├── curl.zip           # Linux curl
│   ├── rtmpdump.exe.zip   # Windows rtmpdump
│   └── rtmpdump.zip       # Linux rtmpdump
├── metadata/              # Version metadata
│   ├── yt-dlp.version
│   ├── ffmpeg.version
│   ├── aria2c.version
│   ├── curl.version
│   └── rtmpdump.version
├── plugins.txt            # Tool list
├── repository.conf        # Repository configuration
└── README.md             # Documentation
```

### GitHub Pages Configuration

#### Enable GitHub Pages
1. Go to repository Settings
2. Navigate to Pages section
3. Select source: "Deploy from a branch"
4. Choose branch: `main` or `master`
5. Select folder: `/ (root)`
6. Click Save

#### Custom Domain (Optional)
If you have a custom domain:
1. Add custom domain in Pages settings
2. Configure DNS records
3. Enable HTTPS enforcement

## Configuration Updates

### Update habiTv Configuration

Users can now configure habiTv to use the HTTPS mirror:

```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
</updateConfig>
```

### Environment Variable Configuration

```bash
# Set environment variable for repository URL
export HABITV_REPOSITORY_URL="https://mika3578.github.io/habitv/repository/"
```

### Configuration File Example

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:configuration xmlns:ns2="http://www.dabi.com/habitv/configuration/entities">
    <updateConfig>
        <updateOnStartup>true</updateOnStartup>
        <autoriseSnapshot>true</autoriseSnapshot>
        <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
        <timeout>30000</timeout>
        <retryAttempts>3</retryAttempts>
    </updateConfig>
    <!-- ... other configuration ... -->
</ns2:configuration>
```

## Automated Updates

### GitHub Actions Workflow

Create `.github/workflows/update-repository.yml`:

```yaml
name: Update habiTv Repository

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM
  workflow_dispatch:     # Manual trigger
  push:
    branches: [ main ]

jobs:
  update-tools:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Check yt-dlp updates
        run: |
          LATEST_VERSION=$(curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | jq -r '.tag_name')
          CURRENT_VERSION=$(cat metadata/yt-dlp.version)
          
          if [ "$LATEST_VERSION" != "$CURRENT_VERSION" ]; then
            echo "NEW_VERSION=$LATEST_VERSION" >> $GITHUB_ENV
            echo "UPDATE_NEEDED=true" >> $GITHUB_ENV
          fi
      
      - name: Download new versions
        if: env.UPDATE_NEEDED == 'true'
        run: |
          curl -L -o "bin/yt-dlp.exe.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
          curl -L -o "bin/yt-dlp.zip" "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
          echo "$NEW_VERSION" > "metadata/yt-dlp.version"
          
          # Add more tools as needed
      
      - name: Commit and push
        if: env.UPDATE_NEEDED == 'true'
        run: |
          git config --local user.email "action@github.com"
          git config --local user.name "GitHub Action"
          git add .
          git commit -m "Auto-update tools to latest versions"
          git push
```

### Manual Update Script

Create `update-repository.sh`:

```bash
#!/bin/bash

# Update habiTv repository from original source
# This script syncs the GitHub Pages mirror with the original repository

REPO_URL="http://dabiboo.free.fr/repository"
LOCAL_DIR="."

echo "Updating habiTv repository mirror..."

# Download plugins.txt
curl -L -o "plugins.txt" "$REPO_URL/plugins.txt"

# Download tool binaries
for tool in yt-dlp ffmpeg aria2c curl rtmpdump; do
    echo "Downloading $tool..."
    
    # Windows version
    curl -L -o "bin/${tool}.exe.zip" "$REPO_URL/bin/${tool}.exe.zip"
    
    # Linux version
    curl -L -o "bin/${tool}.zip" "$REPO_URL/bin/${tool}.zip"
    
    # Version metadata
    curl -L -o "metadata/${tool}.version" "$REPO_URL/metadata/${tool}.version"
done

# Commit changes
git add .
git commit -m "Sync repository with original source"
git push

echo "Repository mirror updated successfully!"
```

## Migration Guide

### For Users

#### Option 1: Update Configuration File
1. Locate your habiTv configuration file
2. Update the `repositoryUrl` to the new HTTPS mirror
3. Restart habiTv

#### Option 2: Environment Variable
```bash
export HABITV_REPOSITORY_URL="https://mika3578.github.io/habitv/repository/"
java -jar habiTv-4.1.0-SNAPSHOT.jar
```

#### Option 3: Command Line
```bash
java -jar habiTv-4.1.0-SNAPSHOT.jar --repository-url="https://mika3578.github.io/habitv/repository/"
```

### For Developers

#### Update Default Configuration
Update the default repository URL in the codebase:

```java
// In FrameworkConf.java
String UPDATE_URL = "https://mika3578.github.io/habitv/repository/";
```

#### Update Documentation
Update all documentation references to use the new HTTPS URL.

## Security Considerations

### HTTPS Benefits
- **Encryption**: All data is encrypted in transit
- **Authentication**: Server identity is verified
- **Integrity**: Data cannot be tampered with
- **Privacy**: Downloads are private and secure

### Certificate Management
- **Automatic**: GitHub handles SSL certificates
- **Renewal**: Certificates are automatically renewed
- **Validation**: Certificates are properly validated
- **Trust**: Certificates are trusted by all browsers

### Best Practices
- **Always use HTTPS**: Never fall back to HTTP
- **Verify URLs**: Ensure correct repository URL
- **Monitor updates**: Check for security updates
- **Backup configuration**: Keep configuration backups

## Monitoring and Maintenance

### Health Checks

#### Repository Availability
```bash
# Check if repository is accessible
curl -I https://mika3578.github.io/habitv/repository/plugins.txt

# Check specific tool availability
curl -I https://mika3578.github.io/habitv/repository/bin/yt-dlp.exe.zip
```

#### Version Monitoring
```bash
# Check current versions
curl -s https://mika3578.github.io/habitv/repository/metadata/yt-dlp.version

# Compare with latest
LATEST=$(curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | jq -r '.tag_name')
CURRENT=$(curl -s https://mika3578.github.io/habitv/repository/metadata/yt-dlp.version)
echo "Latest: $LATEST, Current: $CURRENT"
```

### Automated Monitoring

#### GitHub Actions Monitoring
```yaml
name: Monitor Repository Health

on:
  schedule:
    - cron: '0 */6 * * *'  # Every 6 hours

jobs:
  health-check:
    runs-on: ubuntu-latest
    steps:
      - name: Check repository availability
        run: |
          if ! curl -f -I https://mika3578.github.io/habitv/repository/plugins.txt; then
            echo "Repository is not accessible"
            exit 1
          fi
      
      - name: Check tool versions
        run: |
          for tool in yt-dlp ffmpeg aria2c curl rtmpdump; do
            if ! curl -f -s https://mika3578.github.io/habitv/repository/metadata/${tool}.version; then
              echo "Missing version file for $tool"
              exit 1
            fi
          done
```

## Troubleshooting

### Common Issues

#### Repository Not Accessible
```bash
# Check if GitHub Pages is enabled
curl -I https://mika3578.github.io/habitv/repository/

# Check repository settings
# Go to Settings > Pages in GitHub repository
```

#### SSL Certificate Issues
```bash
# Verify SSL certificate
openssl s_client -connect mika3578.github.io:443 -servername mika3578.github.io

# Check certificate validity
echo | openssl s_client -connect mika3578.github.io:443 -servername mika3578.github.io 2>/dev/null | openssl x509 -noout -dates
```

#### Download Failures
```bash
# Test specific file downloads
curl -L -o test.zip https://mika3578.github.io/habitv/repository/bin/yt-dlp.exe.zip

# Check file integrity
unzip -t test.zip
```

### Debug Commands

#### Network Diagnostics
```bash
# Check DNS resolution
nslookup mika3578.github.io

# Check connectivity
ping mika3578.github.io

# Check HTTP response
curl -v https://mika3578.github.io/habitv/repository/
```

#### Repository Diagnostics
```bash
# List available files
curl -s https://mika3578.github.io/habitv/repository/ | grep -o 'href="[^"]*"' | cut -d'"' -f2

# Check file sizes
curl -I https://mika3578.github.io/habitv/repository/bin/yt-dlp.exe.zip | grep Content-Length
```

## Performance Considerations

### CDN Benefits
- **Global Distribution**: Files served from nearest location
- **Caching**: Automatic caching for faster downloads
- **Compression**: Automatic gzip compression
- **Bandwidth**: High bandwidth availability

### Optimization
- **File Compression**: Use compressed formats when possible
- **Caching Headers**: Set appropriate cache headers
- **Minimal Updates**: Only update when necessary
- **Efficient Downloads**: Use range requests for large files

## Future Enhancements

### Planned Improvements
- **Automated Sync**: Regular sync with original repository
- **Version Tracking**: Better version comparison
- **Health Monitoring**: Automated health checks
- **User Notifications**: Notify users of updates

### Integration Ideas
- **GitHub Releases**: Use GitHub Releases for versioning
- **Webhooks**: Trigger updates on original repository changes
- **API Integration**: Use GitHub API for automation
- **Community Contributions**: Allow community updates

## Conclusion

The GitHub Pages HTTPS mirror provides a secure, reliable, and maintainable alternative to the original HTTP repository. It addresses security concerns while providing better infrastructure and automation capabilities.

### Key Benefits
- ✅ **Security**: HTTPS encryption and certificate validation
- ✅ **Reliability**: GitHub's high-availability infrastructure
- ✅ **Automation**: GitHub Actions integration for updates
- ✅ **Maintenance**: Git-based version control and collaboration
- ✅ **Performance**: Global CDN and caching

### Next Steps
1. **Set up the mirror**: Configure GitHub Pages
2. **Update configurations**: Point habiTv to the new URL
3. **Test thoroughly**: Verify all downloads work
4. **Monitor health**: Set up automated monitoring
5. **Document changes**: Update user documentation

---

**Related Documents:**
- [STARTUP_UPDATE_SYSTEM.md](STARTUP_UPDATE_SYSTEM.md)
- [REPOSITORY_MANAGEMENT.md](REPOSITORY_MANAGEMENT.md)
- [INSTALLATION.md](INSTALLATION.md)

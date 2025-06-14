# habiTv Repository Management

This document describes how to manage external tool repositories for habiTv, including setup, configuration, and maintenance.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Overview

habiTv requires external tools for video downloading and processing. These tools are managed through a repository system that provides:

- **Automatic Downloads**: Tools are downloaded automatically when needed
- **Version Management**: Tools are updated to the latest compatible versions
- **Cross-Platform Support**: Different versions for Windows and Linux
- **Security**: HTTPS downloads with integrity verification

## Repository Structure

### Standard Repository Layout
```
repository/
├── com/dabi/habitv/         # Maven artifacts (group ID structure)
│   ├── arte/                # Arte plugin
│   │   └── 4.1.0-SNAPSHOT/  # Version directory
│   │       └── 4.1.0-SNAPSHOT/  # Version directory
│   ├── youtube/             # YouTube plugin
│   │   └── 4.1.0-SNAPSHOT/  # Version directory
│   └── ...                  # Other plugins
├── bin/                     # Tool binaries
│   ├── yt-dlp.exe.zip      # Windows yt-dlp
│   ├── yt-dlp.zip          # Linux yt-dlp
│   ├── ffmpeg.exe.zip      # Windows ffmpeg
│   ├── ffmpeg.zip          # Linux ffmpeg
│   ├── aria2c.exe.zip      # Windows aria2c
│   ├── aria2c.zip          # Linux aria2c
│   ├── curl.exe.zip        # Windows curl
│   ├── curl.zip            # Linux curl
│   ├── rtmpdump.exe.zip    # Windows rtmpdump
│   └── rtmpdump.zip        # Linux rtmpdump
├── metadata/               # Version metadata
│   ├── yt-dlp.version
│   ├── ffmpeg.version
│   ├── aria2c.version
│   ├── curl.version
│   └── rtmpdump.version
├── plugins.txt             # Tool list
├── repository.conf         # Repository configuration
└── README.md              # Documentation
```

### Maven Repository Structure
The repository follows standard Maven repository conventions:

```
https://mika3578.github.io/habitv/repository/
├── com/dabi/habitv/        # Group ID: com.dabi.habitv
│   ├── arte/               # Artifact ID: arte
│   │   └── 4.1.0-SNAPSHOT/ # Version
│   │       ├── arte-4.1.0-SNAPSHOT.jar
│   │       ├── arte-4.1.0-SNAPSHOT.pom
│   │       └── maven-metadata.xml
│   ├── youtube/            # Artifact ID: youtube
│   │   └── 4.1.0-SNAPSHOT/ # Version
│   │       ├── youtube-4.1.0-SNAPSHOT.jar
│   │       ├── youtube-4.1.0-SNAPSHOT.pom
│   │       └── maven-metadata.xml
│   └── ...                 # Other plugins
└── maven-metadata.xml      # Repository metadata
```

### Required Tools

#### Core Download Tools
- **yt-dlp**: YouTube and general video downloader
- **ffmpeg**: Video processing and conversion
- **aria2c**: High-speed download manager
- **curl**: HTTP download utility
- **rtmpdump**: RTMP stream downloader

#### Tool Versions
Each tool has specific version requirements:
- **yt-dlp**: Latest stable version
- **ffmpeg**: Version 4.0 or higher
- **aria2c**: Version 1.30 or higher
- **curl**: Version 7.50 or higher
- **rtmpdump**: Version 2.4 or higher

## Repository Configuration

### Default Repository URLs

#### Primary Repository (HTTP)
```
http://dabiboo.free.fr/repository
```
- **Protocol**: HTTP (unencrypted)
- **Status**: Original repository
- **Use Case**: Fallback when HTTPS is unavailable

#### GitHub Pages Mirror (HTTPS - Recommended)
```
https://mika3578.github.io/habitv/repository/
```
- **Protocol**: HTTPS (encrypted)
- **Status**: Secure mirror
- **Use Case**: Primary repository for security
- **Type**: Maven repository mirror

### Configuration Methods

#### 1. Configuration File
```xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
    <timeout>30000</timeout>
    <retryAttempts>3</retryAttempts>
</updateConfig>
```

#### 2. Environment Variable
```bash
export HABITV_REPOSITORY_URL="https://mika3578.github.io/habitv/repository/"
```

#### 3. Command Line Parameter
```bash
java -jar habiTv.jar --repository-url="https://mika3578.github.io/habitv/repository/"
```

## Repository Management

### Setting Up a New Repository

#### 1. Create Repository Structure
```bash
mkdir -p repository/{bin,metadata}
touch repository/plugins.txt
touch repository/repository.conf
```

#### 2. Add Tool Binaries
```bash
# Download and package tools
cd repository/bin

# Windows tools
wget -O yt-dlp.exe.zip "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
wget -O ffmpeg.exe.zip "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"

# Linux tools
wget -O yt-dlp.zip "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
wget -O ffmpeg.zip "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"
```

#### 3. Create Version Metadata
```bash
cd repository/metadata

# Get latest versions
echo "2023.12.18" > yt-dlp.version
echo "6.1" > ffmpeg.version
echo "1.36.0" > aria2c.version
echo "8.5.0" > curl.version
echo "2.4" > rtmpdump.version
```

#### 4. Create plugins.txt
```bash
cd repository
cat > plugins.txt << EOF
yt-dlp
ffmpeg
aria2c
curl
rtmpdump
EOF
```

### Repository Configuration File

#### repository.conf
```ini
[repository]
name=habiTv External Tools Repository
version=1.0
description=External tools for habiTv video downloader

[settings]
auto_update=true
check_interval=86400
timeout=30000
retry_attempts=3

[tools]
yt-dlp=required
ffmpeg=required
aria2c=optional
curl=required
rtmpdump=optional

[security]
verify_checksums=true
require_https=true
allowed_domains=mika3578.github.io,dabiboo.free.fr
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
      - uses: actions/checkout@v4
      
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

## Security Considerations

### HTTPS vs HTTP
- **Always prefer HTTPS**: Encrypted downloads prevent tampering
- **Certificate validation**: Verify SSL certificates
- **Fallback strategy**: Use HTTP only as last resort

### Integrity Verification
```bash
# Verify file checksums
sha256sum bin/yt-dlp.exe.zip
sha256sum bin/yt-dlp.zip

# Compare with published checksums
curl -s https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe.sha256
```

### Access Control
- **Repository access**: Control who can upload to repository
- **Version control**: Track all changes to repository
- **Backup strategy**: Maintain backup of repository

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

## Best Practices

### Repository Management
1. **Use HTTPS**: Always prefer encrypted repositories
2. **Version Control**: Track all repository changes
3. **Automated Updates**: Use GitHub Actions for updates
4. **Health Monitoring**: Monitor repository availability
5. **Backup Strategy**: Maintain backup repositories

### Security
1. **Verify Checksums**: Always verify file integrity
2. **Certificate Validation**: Validate SSL certificates
3. **Access Control**: Control repository access
4. **Audit Trail**: Log all repository changes

### Performance
1. **CDN Usage**: Use CDN for global distribution
2. **Compression**: Compress large files
3. **Caching**: Implement appropriate caching
4. **Monitoring**: Monitor download performance

## Conclusion

Proper repository management is essential for habiTv's reliable operation. By following these guidelines, you can ensure:

- **Security**: Encrypted downloads and integrity verification
- **Reliability**: High availability and automated updates
- **Performance**: Fast downloads and global distribution
- **Maintainability**: Easy updates and monitoring

For more information, see:
- [GitHub Pages Mirror Guide](GITHUB_PAGES_MIRROR.md)
- [Installation Guide](INSTALLATION.md)
- [Troubleshooting Guide](TROUBLESHOOTING.md)

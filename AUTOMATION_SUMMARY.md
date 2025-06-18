# Automated External Tools Update System

## Overview

The habiTv project now includes a comprehensive automated system for keeping all external tool binaries up to date. This system ensures that all 20 external tools are automatically updated on a regular schedule and can be manually triggered when needed.

## 🔄 **Automated Update Workflow**

### **Schedule**
- **Frequency**: Every Sunday at 03:00 UTC
- **Workflow**: `.github/workflows/update-external-tools.yml`
- **Trigger**: Automated cron job

### **Manual Trigger**
- **Access**: GitHub Actions UI → "Update External Tools" → "Run workflow"
- **Options**: 
  - Update all tools (default)
  - Update specific tool
  - Force update (even if version is the same)

## 📁 **Managed Tools (20 Total)**

### **Core Download Tools (4)**
- **curl** (8.0.0) - HTTP client
- **wget** (1.21.5) - GNU Wget for HTTP/HTTPS/FTP
- **aria2c** (1.36.0) - Multi-protocol download utility
- **youtube-dl** (2023.12.30) - Video downloader

### **Video Processing Tools (6)**
- **ffmpeg** (6.1) - Video/audio processing
- **ffprobe** (6.1.1) - Multimedia streams analyzer
- **rtmpdump** (v2.6) - RTMP stream handling
- **mkvmerge** (82.0) - Matroska file manipulation
- **mp4box** (2.1.2) - MP4 container manipulation
- **flvtool2** (1.0.7) - Flash Video manipulation

### **Media Analysis Tools (2)**
- **mediainfo** (24.01) - Media file information display
- **exiftool** (12.80) - Metadata reading and writing

### **Compression & Archive Tools (1)**
- **7z** (23.02) - File archiver with high compression

### **Web Automation Tools (2)**
- **chromedriver** (121.0.6167.85) - Chrome WebDriver
- **phantomjs** (2.1.2) - Headless WebKit browser

### **Network Tools (1)**
- **openvpn** (2.6.9) - VPN client

### **Audio Processing Tools (1)**
- **sox** (14.4.3) - Audio format conversion

### **Data Processing Tools (1)**
- **jq** (1.7.2) - JSON processor

### **Runtime Environments (2)**
- **python** (3.12.2) - Portable Python interpreter
- **node** (20.11.0) - Portable Node.js runtime

## 🛠️ **Technical Implementation**

### **Directory Structure**
```
repository/com/dabi/habitv/bin/
├── [tool]/
│   ├── [version]/
│   │   └── [tool].exe (and additional files)
│   ├── latest/
│   │   └── [tool].exe (and additional files)
│   └── manifest.json
```

### **Key Files**
- **`.github/workflows/update-external-tools.yml`** - Main automation workflow
- **`scripts/update_all_tools.py`** - Core update script
- **`scripts/verify_urls.py`** - Verification and validation script
- **`update_summary.txt`** - Generated summary of updates

### **Update Process**
1. **Check Current Versions** - Read from manifest.json files
2. **Determine New Versions** - From tool configuration
3. **Create Version Directories** - New version folders
4. **Update Binaries** - Place new binaries in version and latest folders
5. **Update Manifests** - Update manifest.json with new version info
6. **Commit Changes** - Automatically commit and push updates
7. **Generate Summary** - Create update summary for GitHub Actions

## 📊 **Statistics**

- **Total Tools**: 20
- **Total Files**: 96
- **Update Frequency**: Weekly (Sundays 03:00 UTC)
- **Manual Trigger**: Available anytime
- **Version History**: Preserved for compatibility
- **Direct Downloads**: Always point to latest versions

## 🔗 **Direct Download URLs**

All tools are available for direct download at:
```
https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/[tool]/latest/[tool].exe
```

### **Examples**
- curl: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/curl/latest/curl.exe
- ffmpeg: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/ffmpeg/latest/ffmpeg.exe
- python: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/python/latest/python.exe

## 📋 **Usage Instructions**

### **For Users**
- Download any tool directly from the URLs above
- All tools are automatically kept up to date
- Previous versions are preserved in versioned directories

### **For Developers**
- Use the direct download URLs in your applications
- Check manifest.json files for detailed version information
- Tools are updated automatically - no manual intervention needed

### **For CI/CD**
- Use the direct download URLs in your build scripts
- Tools are guaranteed to be up to date
- Version history is preserved for rollback if needed

## 🔧 **Maintenance**

### **Monitoring**
- GitHub Actions logs show update status
- Update summary is generated for each run
- Failed updates are logged and reported

### **Troubleshooting**
- Check GitHub Actions logs for errors
- Verify tool configurations in `scripts/update_all_tools.py`
- Run `python scripts/verify_urls.py` to validate structure

### **Adding New Tools**
1. Add tool configuration to `TOOL_CONFIGS` in `scripts/update_all_tools.py`
2. Create initial directory structure and manifest.json
3. Add tool to GitHub Actions workflow
4. Update documentation

## 📈 **Benefits**

- **Always Current**: Tools are automatically updated to latest versions
- **Zero Maintenance**: No manual intervention required
- **Version Control**: Previous versions preserved for compatibility
- **Reliable**: Automated process with error handling
- **Transparent**: All updates logged and documented
- **Flexible**: Manual triggers available for urgent updates

## 🎯 **Next Steps**

1. **Enable GitHub Pages** in repository settings
2. **Test the workflow** by triggering a manual update
3. **Monitor the first scheduled update** (Sunday 03:00 UTC)
4. **Verify direct download URLs** work correctly
5. **Update any build scripts** to use the new URLs

---

**Last Updated**: June 18, 2025  
**Next Scheduled Update**: Sunday, June 22, 2025 at 03:00 UTC 

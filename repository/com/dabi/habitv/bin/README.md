# External Tool Binaries

This directory contains centralized, versioned external tool binaries used by habiTv. All binaries are organized by tool name and version, with a `latest` directory always pointing to the most recent version.

## 🔄 **Automated Updates**

All tools in this directory are **automatically updated** by GitHub Actions:

- **Schedule**: Every Sunday at 03:00 UTC
- **Manual Trigger**: Available from GitHub Actions UI
- **Version Control**: Previous versions are preserved
- **Always Current**: Direct download URLs point to latest versions

## Structure

```
BIN/
├── [tool]/
│   ├── [version]/
│   │   └── [tool].exe
│   ├── latest/
│   │   └── [tool].exe
│   └── manifest.json
```

## Available Tools

### Core Download Tools

#### curl
- **Latest Version**: 8.0.0
- **Description**: Command line tool for transferring data with URLs
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/curl/latest/curl.exe
- **Homepage**: https://curl.se/

#### wget
- **Latest Version**: 1.21.5
- **Description**: GNU Wget for retrieving files via HTTP, HTTPS, FTP
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/wget/latest/wget.exe
- **Homepage**: https://www.gnu.org/software/wget/

#### aria2c
- **Latest Version**: 1.36.0
- **Description**: Lightweight multi-protocol & multi-source command-line download utility
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/aria2c/latest/aria2c.exe
- **Homepage**: https://aria2.github.io/

#### youtube-dl
- **Latest Version**: 2023.12.30
- **Description**: Command-line program to download videos from YouTube.com and a few more sites
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/youtube-dl/latest/youtube-dl.exe
- **Homepage**: https://youtube-dl.org/

### Video Processing Tools

#### ffmpeg
- **Latest Version**: 6.1
- **Description**: Complete, cross-platform solution to record, convert and stream audio and video
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/ffmpeg/latest/ffmpeg.exe
- **Homepage**: https://ffmpeg.org/

#### ffprobe
- **Latest Version**: 6.1.1
- **Description**: Multimedia streams analyzer from the FFmpeg project
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/ffprobe/latest/ffprobe.exe
- **Homepage**: https://ffmpeg.org/

#### rtmpdump
- **Latest Version**: v2.6
- **Description**: Toolkit for RTMP streams. All forms of RTMP are supported
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/rtmpdump/latest/rtmpdump.exe
- **Homepage**: https://rtmpdump.mplayerhq.hu/
- **Additional Files**: librtmp.dll

#### mkvmerge
- **Latest Version**: 82.0
- **Description**: Part of MKVToolNix for creating Matroska files from other formats
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/mkvmerge/latest/mkvmerge.exe
- **Homepage**: https://mkvtoolnix.download/

#### mp4box
- **Latest Version**: 2.1.2
- **Description**: Multimedia container box manipulation tool from GPAC
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/mp4box/latest/mp4box.exe
- **Homepage**: https://gpac.wp.imt.fr/mp4box/

#### flvtool2
- **Latest Version**: 1.0.7
- **Description**: Command line tool for manipulating Macromedia Flash Video files
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/flvtool2/latest/flvtool2.exe
- **Homepage**: https://github.com/unnu/flvtool2

### Media Analysis Tools

#### mediainfo
- **Latest Version**: 24.01
- **Description**: Convenient unified display of technical and tag data for video and audio files
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/mediainfo/latest/mediainfo.exe
- **Homepage**: https://mediaarea.net/en/MediaInfo

#### exiftool
- **Latest Version**: 12.80
- **Description**: Platform-independent library for reading, writing and editing meta information
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/exiftool/latest/exiftool.exe
- **Homepage**: https://exiftool.org/

### Compression & Archive Tools

#### 7z
- **Latest Version**: 23.02
- **Description**: File archiver with a high compression ratio
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/7z/latest/7z.exe
- **Homepage**: https://7-zip.org/

### Web Automation Tools

#### chromedriver
- **Latest Version**: 121.0.6167.85
- **Description**: Standalone server that implements the W3C WebDriver wire protocol for Chromium
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/chromedriver/latest/chromedriver.exe
- **Homepage**: https://chromedriver.chromium.org/

#### phantomjs
- **Latest Version**: 2.1.2
- **Description**: Headless WebKit scriptable with a JavaScript API
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/phantomjs/latest/phantomjs.exe
- **Homepage**: https://phantomjs.org/

### Network Tools

#### openvpn
- **Latest Version**: 2.6.9
- **Description**: Open-source VPN daemon
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/openvpn/latest/openvpn.exe
- **Homepage**: https://openvpn.net/

### Audio Processing Tools

#### sox
- **Latest Version**: 14.4.3
- **Description**: Command line utility for converting various formats of computer audio files
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/sox/latest/sox.exe
- **Homepage**: https://sox.sourceforge.net/

### Data Processing Tools

#### jq
- **Latest Version**: 1.7.2
- **Description**: Lightweight and flexible command-line JSON processor
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/jq/latest/jq.exe
- **Homepage**: https://jqlang.github.io/jq/

### Runtime Environments

#### python
- **Latest Version**: 3.12.2
- **Description**: Portable Python interpreter for Windows
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/python/latest/python.exe
- **Homepage**: https://www.python.org/
- **Additional Files**: pythonw.exe, python312.dll

#### node
- **Latest Version**: 20.11.0
- **Description**: Portable Node.js runtime for Windows
- **Direct Download**: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/node/latest/node.exe
- **Homepage**: https://nodejs.org/
- **Additional Files**: npm.cmd, npx.cmd

## Usage

### For Users
Download the latest version of any tool directly from the URLs above, or browse specific versions in the versioned directories. **All tools are automatically kept up to date.**

### For Developers
Each tool has a `manifest.json` file containing:
- Current latest version
- List of all available versions
- Direct download URLs
- Tool description and metadata
- Changelog information

### For CI/CD
GitHub Actions workflows can use these URLs to download the latest versions of tools automatically.

## Version Management

- The `latest` directory always contains the most recent version of each tool
- Versioned directories preserve historical versions for compatibility
- All binaries are tested and verified before inclusion
- Updates are automated through GitHub Actions workflows
- **Next scheduled update**: Sunday at 03:00 UTC

## License Information

Each tool maintains its original license. See individual `manifest.json` files for specific license information. 

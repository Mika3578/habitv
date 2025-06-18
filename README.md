# habiTv - TV Replay Downloader

habiTv is a Java-based application for downloading TV replays from various streaming platforms.

## Features

- Download TV replays from multiple platforms
- Support for various video formats
- Cross-platform compatibility
- Plugin-based architecture for extensibility

## External Tools

habiTv uses several external tools for video processing and downloading. These are now centrally managed in the `repository/com/dabi/habitv/bin/` directory and **automatically updated** by GitHub Actions.

### 🔄 **Automated Updates**
- **Weekly Updates**: All tools are automatically updated every Sunday at 03:00 UTC
- **Manual Updates**: Can be triggered manually from the GitHub Actions UI
- **Always Current**: Direct download URLs always point to the latest versions
- **Version History**: Previous versions are preserved for compatibility

### Available Tools

#### Core Download Tools
- **curl** (8.0.0) - HTTP client for data transfer
- **wget** (1.21.5) - GNU Wget for retrieving files via HTTP, HTTPS, FTP
- **aria2c** (1.36.0) - Multi-protocol download utility
- **youtube-dl** (2023.12.30) - Video downloader

#### Video Processing Tools
- **ffmpeg** (6.1) - Video/audio processing
- **ffprobe** (6.1.1) - Multimedia streams analyzer
- **rtmpdump** (v2.6) - RTMP stream handling
- **mkvmerge** (82.0) - Matroska file manipulation
- **mp4box** (2.1.2) - MP4 container manipulation
- **flvtool2** (1.0.7) - Flash Video manipulation

#### Media Analysis Tools
- **mediainfo** (24.01) - Media file information display
- **exiftool** (12.80) - Metadata reading and writing

#### Compression & Archive Tools
- **7z** (23.02) - File archiver with high compression

#### Web Automation Tools
- **chromedriver** (121.0.6167.85) - Chrome WebDriver
- **phantomjs** (2.1.2) - Headless WebKit browser

#### Network Tools
- **openvpn** (2.6.9) - VPN client

#### Audio Processing Tools
- **sox** (14.4.3) - Audio format conversion

#### Data Processing Tools
- **jq** (1.7.2) - JSON processor

#### Runtime Environments
- **python** (3.12.2) - Portable Python interpreter
- **node** (20.11.0) - Portable Node.js runtime

### Direct Downloads

All tools are available for direct download and are **automatically kept up to date**:

#### Core Download Tools
- curl: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/curl/latest/curl.exe
- wget: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/wget/latest/wget.exe
- aria2c: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/aria2c/latest/aria2c.exe
- youtube-dl: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/youtube-dl/latest/youtube-dl.exe

#### Video Processing Tools
- ffmpeg: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/ffmpeg/latest/ffmpeg.exe
- ffprobe: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/ffprobe/latest/ffprobe.exe
- rtmpdump: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/rtmpdump/latest/rtmpdump.exe
- mkvmerge: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/mkvmerge/latest/mkvmerge.exe
- mp4box: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/mp4box/latest/mp4box.exe
- flvtool2: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/flvtool2/latest/flvtool2.exe

#### Media Analysis Tools
- mediainfo: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/mediainfo/latest/mediainfo.exe
- exiftool: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/exiftool/latest/exiftool.exe

#### Compression & Archive Tools
- 7z: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/7z/latest/7z.exe

#### Web Automation Tools
- chromedriver: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/chromedriver/latest/chromedriver.exe
- phantomjs: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/phantomjs/latest/phantomjs.exe

#### Network Tools
- openvpn: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/openvpn/latest/openvpn.exe

#### Audio Processing Tools
- sox: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/sox/latest/sox.exe

#### Data Processing Tools
- jq: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/jq/latest/jq.exe

#### Runtime Environments
- python: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/python/latest/python.exe
- node: https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/node/latest/node.exe

For more information about the external tools, see [repository/com/dabi/habitv/bin/README.md](repository/com/dabi/habitv/bin/README.md).

## Building

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher

### Build Commands

```bash
# Clean and compile all modules
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

## Installation

1. Download the latest release from the releases page
2. Extract the archive to your desired location
3. Ensure the required external tools are available in your PATH or in the application directory
4. Run the application

## Development

### Project Structure

```
habitv/
├── fwk/                    # Framework modules
├── application/            # Application modules
├── plugins/               # Plugin modules
├── repository/com/dabi/habitv/bin/        # External tool binaries
└── scripts/               # Build and update scripts
```

### Adding New Plugins

1. Create a new module in the `plugins/` directory
2. Implement the required plugin interfaces
3. Add the module to the parent pom.xml
4. Update the plugin registry

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the terms specified in the individual module licenses.

## External Tool Licenses

- curl: MIT/X derivate license
- ffmpeg: LGPL/GPL
- aria2c: GPL-2.0
- rtmpdump: GPL-2.0
- youtube-dl: Unlicense

See individual `manifest.json` files in `repository/com/dabi/habitv/bin/` for more details. 

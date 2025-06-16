# Environment Variables in habiTv

This document describes the environment variables that can be used to configure habiTv without modifying configuration files.

## Core Configuration Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_HOME` | Application home directory | `${user.home}/habitv` | `/custom/path/habitv` |
| `HABITV_DOWNLOAD_OUTPUT` | Download output directory | `${HABITV_HOME}/downloads` | `D:\Videos\TV` |
| `HABITV_LOG_LEVEL` | Logging level | `INFO` | `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `HABITV_DEBUG` | Enable debug mode | `false` | `true` |
| `HABITV_DEV_MODE` | Enable development mode | `false` | `true` |

## Logging Configuration

The `HABITV_LOG_LEVEL` environment variable overrides the root logger level defined in `log4j.properties`. Valid values are:

- `DEBUG`: Detailed diagnostic information (development/troubleshooting)
- `INFO`: General application flow and status information
- `WARN`: Warning conditions that don't stop operation
- `ERROR`: Error conditions that may affect functionality

Example:
```bash
# Linux/macOS
export HABITV_LOG_LEVEL=DEBUG

# Windows
set HABITV_LOG_LEVEL=DEBUG
```

## Directory Configuration

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_WORKING_DIR` | Working directory for temporary files | `${HABITV_HOME}/work` | `/tmp/habitv` |
| `HABITV_INDEX_DIR` | Directory for index files | `${HABITV_HOME}/index` | `/var/habitv/index` |
| `HABITV_PLUGIN_DIR` | Directory for plugins | `${HABITV_HOME}/plugins` | `/opt/habitv/plugins` |
| `HABITV_BIN_DIR` | Directory for external tools | `${HABITV_HOME}/bin` | `/usr/local/bin` |

## Network Configuration

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_PROXY_HOST` | Proxy server hostname | None | `proxy.example.com` |
| `HABITV_PROXY_PORT` | Proxy server port | None | `8080` |
| `HABITV_PROXY_USER` | Proxy username | None | `proxyuser` |
| `HABITV_PROXY_PASSWORD` | Proxy password | None | `proxypass` |
| `HABITV_PROXY_PROTOCOL` | Proxy protocol | `http` | `http`, `socks` |
| `HABITV_CONNECTION_TIMEOUT` | Connection timeout in seconds | `30` | `60` |
| `HABITV_READ_TIMEOUT` | Read timeout in seconds | `30` | `60` |

## Download Configuration

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_MAX_ATTEMPTS` | Maximum download attempts | `3` | `5` |
| `HABITV_RETRY_DELAY` | Delay between retry attempts (seconds) | `10` | `30` |
| `HABITV_MAX_CONCURRENT_DOWNLOADS` | Maximum concurrent downloads | `2` | `4` |
| `HABITV_FILENAME_CUT_SIZE` | Maximum filename length | `100` | `200` |

## Daemon Configuration

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_DAEMON_TIME` | Daemon check interval (minutes) | `60` | `30` |
| `HABITV_DAEMON_ENABLED` | Enable daemon mode at startup | `false` | `true` |
| `HABITV_DAEMON_START_DELAY` | Delay before first daemon check (seconds) | `10` | `60` |

## External Tools Configuration

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `HABITV_FFMPEG_PATH` | Path to ffmpeg executable | Auto-detected | `/usr/bin/ffmpeg` |
| `HABITV_RTMPDUMP_PATH` | Path to rtmpdump executable | Auto-detected | `/usr/bin/rtmpdump` |
| `HABITV_ARIA2C_PATH` | Path to aria2c executable | Auto-detected | `/usr/bin/aria2c` |
| `HABITV_YTDLP_PATH` | Path to yt-dlp executable | Auto-detected | `/usr/bin/yt-dlp` |
| `HABITV_CURL_PATH` | Path to curl executable | Auto-detected | `/usr/bin/curl` |

## Setting Environment Variables

### Windows

In Command Prompt:
```cmd
set HABITV_HOME=D:\habitv
set HABITV_DOWNLOAD_OUTPUT=D:\Videos\TV
```

Permanently (System Properties):
1. Right-click on "This PC" or "Computer" and select "Properties"
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Add or modify variables in the "User variables" or "System variables" section

### Linux/macOS

For current session:
```bash
export HABITV_HOME=/home/user/habitv
export HABITV_DOWNLOAD_OUTPUT=/media/videos/tv
```

Permanently (add to ~/.bashrc, ~/.zshrc, or equivalent):
```bash
echo 'export HABITV_HOME=/home/user/habitv' >> ~/.bashrc
echo 'export HABITV_DOWNLOAD_OUTPUT=/media/videos/tv' >> ~/.bashrc
source ~/.bashrc
```

## Precedence

Environment variables take precedence over values defined in configuration files. The order of precedence is:

1. Command-line arguments
2. Environment variables
3. Configuration files
4. Default values

## Examples

### Basic Configuration

```bash
# Set home directory and download location
export HABITV_HOME=/opt/habitv
export HABITV_DOWNLOAD_OUTPUT=/media/videos/tv

# Enable debug logging
export HABITV_LOG_LEVEL=DEBUG

# Run habiTv
java -jar habiTv.jar
```

### Proxy Configuration

```bash
# Configure proxy
export HABITV_PROXY_HOST=proxy.example.com
export HABITV_PROXY_PORT=8080
export HABITV_PROXY_USER=proxyuser
export HABITV_PROXY_PASSWORD=proxypass

# Run habiTv
java -jar habiTv.jar
```

### Daemon Mode

```bash
# Configure daemon
export HABITV_DAEMON_ENABLED=true
export HABITV_DAEMON_TIME=30
export HABITV_MAX_CONCURRENT_DOWNLOADS=3

# Run habiTv
java -jar habiTv.jar
```
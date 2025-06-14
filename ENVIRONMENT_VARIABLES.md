# habiTv Environment Variables Configuration

This document explains how to configure and use environment variables with the habiTv application.

## Overview

The habiTv application now supports environment variables for configuration, making it easier to:

- Configure different settings for development, testing, and production
- Override default configuration values
- Set up proxy settings
- Configure logging and debugging options

## Environment Variables

### Application Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `HABITV_VERSION` | Application version | `4.1.0-SNAPSHOT` |
| `HABITV_HOME` | Application home directory | `${user.home}/habitv` |
| `HABITV_LOG_LEVEL` | Logging level (DEBUG, INFO, WARN, ERROR) | `INFO` |
| `HABITV_DEBUG` | Enable debug mode | `false` |
| `HABITV_DEV_MODE` | Enable development mode | `false` |

### Download Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `HABITV_DOWNLOAD_OUTPUT` | Download output directory | `${user.home}/habitv/downloads` |
| `HABITV_MAX_ATTEMPTS` | Maximum download attempts | `3` |
| `HABITV_DAEMON_CHECK_TIME` | Daemon check time in seconds | `30` |
| `HABITV_FILENAME_CUT_SIZE` | Maximum filename length | `100` |

### Plugin Directories

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `HABITV_PLUGIN_DIR` | Main plugin directory | `${user.home}/habitv/plugins` |
| `HABITV_PROVIDER_PLUGIN_DIR` | Provider plugins directory | `${user.home}/habitv/plugins/providers` |
| `HABITV_DOWNLOADER_PLUGIN_DIR` | Downloader plugins directory | `${user.home}/habitv/plugins/downloaders` |
| `HABITV_EXPORTER_PLUGIN_DIR` | Exporter plugins directory | `${user.home}/habitv/plugins/exporters` |

### Update Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `HABITV_UPDATE_ON_STARTUP` | Check for updates on startup | `true` |
| `HABITV_AUTORISE_SNAPSHOT` | Allow snapshot updates | `true` |

### Proxy Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `HTTP_PROXY_HOST` | HTTP proxy host | (none) |
| `HTTP_PROXY_PORT` | HTTP proxy port | (none) |
| `HTTP_PROXY_USER` | HTTP proxy username | (none) |
| `HTTP_PROXY_PASSWORD` | HTTP proxy password | (none) |

### Logging Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `HABITV_LOG_FILE` | Log file path | `${user.home}/habitv/habiTv.log` |
| `HABITV_LOG_MAX_FILE_SIZE` | Maximum log file size | `10000KB` |
| `HABITV_LOG_MAX_BACKUP_INDEX` | Maximum backup log files | `30` |

### Java Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `JAVA_HOME` | Java installation directory | (system default) |
| `JAVA_OPTS` | Java VM options | `-Xmx2g -Xms512m -Dfile.encoding=UTF-8` |

## Configuration Methods

### 1. Using .env File

Create a `.env` file in the project root directory or in your user home directory as `.habitv.env`:

```bash
# Copy the example file
cp env.example .env

# Edit the .env file with your settings
nano .env
```

### 2. Using System Environment Variables

Set environment variables in your system:

**Windows (Command Prompt):**

```cmd
set HABITV_VERSION=4.1.0-SNAPSHOT
set HABITV_HOME=C:\Users\YourName\habitv
set HABITV_DEBUG=true
```

**Windows (PowerShell):**

```powershell
$env:HABITV_VERSION = "4.1.0-SNAPSHOT"
$env:HABITV_HOME = "$env:USERPROFILE\habitv"
$env:HABITV_DEBUG = "true"
```

**Linux/Mac:**

```bash
export HABITV_VERSION=4.1.0-SNAPSHOT
export HABITV_HOME=$HOME/habitv
export HABITV_DEBUG=true
```

### 3. Using Launch Scripts

Use the provided launch scripts that set environment variables:

**Windows:**

```cmd
run-habitv.bat
```

**PowerShell:**

```powershell
.\run-habitv.ps1
```

**Linux/Mac:**

```bash
./run-habitv.sh
```

### 4. Using VS Code Launch Configuration

Use the VS Code launch configurations in `.vscode/launch.json`:

1. Open VS Code
2. Go to Run and Debug (Ctrl+Shift+D)
3. Select a configuration:

   - "Launch habiTv (GUI Mode)"
   - "Launch habiTv (Console Mode)"
   - "Launch habiTv with Proxy"

## Priority Order

Environment variables are loaded in the following priority order:

1. **System Properties** (set via `-D` JVM arguments)
2. **System Environment Variables** (set in OS)
3. **.env File** (project root or user home)
4. **Default Values** (hardcoded in application)

## Examples

### Development Configuration

```bash
# .env file for development
HABITV_VERSION=4.1.0-SNAPSHOT
HABITV_HOME=/path/to/dev/habitv
HABITV_LOG_LEVEL=DEBUG
HABITV_DEBUG=true
HABITV_DEV_MODE=true
HABITV_DOWNLOAD_OUTPUT=/path/to/dev/habitv/downloads
HABITV_UPDATE_ON_STARTUP=false
HABITV_AUTORISE_SNAPSHOT=false
```

### Production Configuration

```bash
# .env file for production
HABITV_VERSION=4.1.0
HABITV_HOME=/opt/habitv
HABITV_LOG_LEVEL=WARN
HABITV_DEBUG=false
HABITV_DEV_MODE=false
HABITV_DOWNLOAD_OUTPUT=/opt/habitv/downloads
HABITV_UPDATE_ON_STARTUP=true
HABITV_AUTORISE_SNAPSHOT=false
JAVA_OPTS=-Xmx4g -Xms2g -Dfile.encoding=UTF-8
```

### Proxy Configuration

```bash
# .env file with proxy settings
HTTP_PROXY_HOST=proxy.company.com
HTTP_PROXY_PORT=8080
HTTP_PROXY_USER=username
HTTP_PROXY_PASSWORD=password
```

## Troubleshooting

### Environment Variables Not Loading

1. Check that the `.env` file exists and is readable
2. Verify the file format (no spaces around `=`)
3. Check for syntax errors in the file
4. Ensure the application has permission to read the file

### Java Version Issues

1. Set `JAVA_HOME` to point to Java 8 installation
2. Ensure Java 8 is in your PATH
3. Use the launch scripts which handle Java setup

### Proxy Issues

1. Verify proxy host and port are correct
2. Check proxy authentication if required
3. Test proxy connectivity manually
4. Check firewall settings

## Integration with Existing Configuration

The environment variables integrate with the existing XML configuration system:

- Environment variables can override XML configuration values
- XML configuration serves as fallback when environment variables are not set
- Both systems work together seamlessly

## Security Considerations

- Never commit `.env` files with sensitive information to version control
- Use environment variables for sensitive data like passwords
- Consider using a secrets management system for production deployments
- Regularly rotate proxy credentials and other sensitive values 
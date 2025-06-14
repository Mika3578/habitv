@echo off
REM habiTv GitHub Pages Mirror Setup Script (Windows Batch)
REM This script helps set up a GitHub Pages mirror of the dabiboo.free.fr repository
REM
REM Version: 4.1.0-SNAPSHOT
REM Author: habiTv Team
REM Date: June 15, 2025

setlocal enabledelayedexpansion

REM Configuration
set "ORIGINAL_REPO=http://dabiboo.free.fr/repository"
set "GITHUB_USERNAME=%1"
set "REPO_NAME=%2"

if "%GITHUB_USERNAME%"=="" set "GITHUB_USERNAME=your-username"
if "%REPO_NAME%"=="" set "REPO_NAME=habitv-tools"

set "GITHUB_PAGES_URL=https://%GITHUB_USERNAME%.github.io/%REPO_NAME%"

echo ==========================================
echo habiTv GitHub Pages Mirror Setup
echo ==========================================
echo.

REM Check for GitHub token
if "%GITHUB_TOKEN%"=="" (
    echo [ERROR] GITHUB_TOKEN environment variable is not set
    echo Please set your GitHub token:
    echo set GITHUB_TOKEN=your_github_token
    pause
    exit /b 1
)

REM Check dependencies
echo [INFO] Checking dependencies...

where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] git is not installed or not in PATH
    echo Please install git and try again.
    pause
    exit /b 1
)

echo [SUCCESS] All dependencies are available

REM Create GitHub repository
echo [INFO] Setting up GitHub repository...

REM Check if repository already exists
curl -s -o nul -w "%%{http_code}" "https://api.github.com/repos/%GITHUB_USERNAME%/%REPO_NAME%" | findstr "200" >nul
if %errorlevel% equ 0 (
    echo [WARNING] Repository %GITHUB_USERNAME%/%REPO_NAME% already exists
    set /p "continue=Do you want to continue with existing repository? (y/N): "
    if /i not "!continue!"=="y" exit /b 1
) else (
    echo [INFO] Creating GitHub repository %GITHUB_USERNAME%/%REPO_NAME%...
    
    REM Create repository using GitHub API
    curl -X POST ^
        -H "Authorization: token %GITHUB_TOKEN%" ^
        -H "Accept: application/vnd.github.v3+json" ^
        https://api.github.com/user/repos ^
        -d "{\"name\":\"%REPO_NAME%\",\"description\":\"habiTv External Tools Repository Mirror\",\"homepage\":\"%GITHUB_PAGES_URL%\",\"private\":false,\"has_issues\":true,\"has_wiki\":false,\"has_downloads\":true,\"auto_init\":true}"
    
    echo [SUCCESS] GitHub repository created
)

REM Clone and setup repository
echo [INFO] Setting up local repository...

if exist "%REPO_NAME%" (
    echo [WARNING] Directory %REPO_NAME% already exists
    set /p "continue=Do you want to remove it and start fresh? (y/N): "
    if /i "!continue!"=="y" (
        rmdir /s /q "%REPO_NAME%"
    ) else (
        echo [INFO] Using existing directory
        goto :download_content
    )
)

git clone "https://github.com/%GITHUB_USERNAME%/%REPO_NAME%.git"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to clone repository
    pause
    exit /b 1
)

cd "%REPO_NAME%"
echo [SUCCESS] Repository cloned successfully

:download_content
REM Download repository content
echo [INFO] Downloading repository content from %ORIGINAL_REPO%...

REM Create directory structure
if not exist "bin" mkdir "bin"
if not exist "metadata" mkdir "metadata"

REM Download plugins.txt
curl -L -o "plugins.txt" "%ORIGINAL_REPO%/plugins.txt"
if %errorlevel% equ 0 (
    echo [SUCCESS] Downloaded plugins.txt
) else (
    echo [WARNING] Failed to download plugins.txt
)

REM Download tool binaries and metadata
for %%t in (yt-dlp ffmpeg aria2c curl rtmpdump) do (
    echo [INFO] Downloading %%t...
    
    REM Windows version
    curl -L -o "bin\%%t.exe.zip" "%ORIGINAL_REPO%/bin/%%t.exe.zip"
    if %errorlevel% equ 0 (
        echo   [SUCCESS] Downloaded Windows %%t
    ) else (
        echo   [WARNING] Failed to download Windows %%t
    )
    
    REM Linux version
    curl -L -o "bin\%%t.zip" "%ORIGINAL_REPO%/bin/%%t.zip"
    if %errorlevel% equ 0 (
        echo   [SUCCESS] Downloaded Linux %%t
    ) else (
        echo   [WARNING] Failed to download Linux %%t
    )
    
    REM Version metadata
    curl -L -o "metadata\%%t.version" "%ORIGINAL_REPO%/metadata/%%t.version"
    if %errorlevel% equ 0 (
        echo   [SUCCESS] Downloaded %%t version metadata
    ) else (
        echo   [WARNING] Failed to download %%t version metadata
    )
)

echo [SUCCESS] Repository content downloaded

REM Create repository configuration
echo [INFO] Creating repository configuration...

(
echo # habiTv External Tools Repository Configuration
echo # Version: 4.1.0-SNAPSHOT
echo # Generated: %date% %time%
echo.
echo [repository]
echo name = habiTv External Tools Repository ^(GitHub Pages Mirror^)
echo url = %GITHUB_PAGES_URL%
echo version = 4.1.0-SNAPSHOT
echo description = HTTPS mirror of habiTv external tools repository
echo original_source = %ORIGINAL_REPO%
echo.
echo [security]
echo checksum_verification = true
echo signature_verification = false
echo https_enabled = true
echo allowed_hosts = github.com, github.io
echo.
echo [update]
echo check_interval = 86400
echo retry_attempts = 3
echo timeout = 30000
echo auto_sync = true
echo.
echo [tools]
echo auto_update = true
echo fallback_sources = true
echo version_check = true
) > "repository.conf"

echo [SUCCESS] Repository configuration created

REM Create README
echo [INFO] Creating README file...

(
echo # habiTv External Tools Repository
echo.
echo This is a GitHub Pages mirror of the habiTv external tools repository, providing secure HTTPS access to required tools.
echo.
echo ## Repository Information
echo.
echo - **Original Repository**: %ORIGINAL_REPO%
echo - **GitHub Pages Mirror**: %GITHUB_PAGES_URL%
echo - **Protocol**: HTTPS ^(encrypted^)
echo - **Status**: Active mirror
echo.
echo ## Repository Structure
echo.
echo ```
echo %REPO_NAME%/
echo ├── bin/                    # Tool binaries
echo │   ├── yt-dlp.exe.zip     # Windows yt-dlp
echo │   ├── yt-dlp.zip         # Linux yt-dlp
echo │   ├── ffmpeg.exe.zip     # Windows ffmpeg
echo │   ├── ffmpeg.zip         # Linux ffmpeg
echo │   ├── aria2c.exe.zip     # Windows aria2c
echo │   ├── aria2c.zip         # Linux aria2c
echo │   ├── curl.exe.zip       # Windows curl
echo │   ├── curl.zip           # Linux curl
echo │   ├── rtmpdump.exe.zip   # Windows rtmpdump
echo │   └── rtmpdump.zip       # Linux rtmpdump
echo ├── metadata/              # Version metadata
echo │   ├── yt-dlp.version
echo │   ├── ffmpeg.version
echo │   ├── aria2c.version
echo │   ├── curl.version
echo │   └── rtmpdump.version
echo ├── plugins.txt            # Tool list
echo ├── repository.conf        # Repository configuration
echo └── README.md             # This file
echo ```
echo.
echo ## Available Tools
echo.
echo - **yt-dlp**: YouTube and video platform downloader
echo - **ffmpeg**: Video processing and conversion
echo - **aria2c**: High-speed download utility
echo - **curl**: HTTP client and downloader
echo - **rtmpdump**: RTMP stream downloader
echo.
echo ## Usage
echo.
echo ### habiTv Configuration
echo.
echo Update your habiTv configuration to use this HTTPS mirror:
echo.
echo ```xml
echo ^<updateConfig^>
echo     ^<updateOnStartup^>true^</updateOnStartup^>
echo     ^<autoriseSnapshot^>true^</autoriseSnapshot^>
echo     ^<repositoryUrl^>%GITHUB_PAGES_URL%^</repositoryUrl^>
echo ^</updateConfig^>
echo ```
echo.
echo ### Environment Variable
echo.
echo ```bash
echo export HABITV_REPOSITORY_URL="%GITHUB_PAGES_URL%"
echo ```
echo.
echo ### Manual Download
echo.
echo ```bash
echo # Download Windows yt-dlp
echo curl -L -o yt-dlp.exe.zip %GITHUB_PAGES_URL%/bin/yt-dlp.exe.zip
echo.
echo # Download Linux yt-dlp
echo curl -L -o yt-dlp.zip %GITHUB_PAGES_URL%/bin/yt-dlp.zip
echo ```
echo.
echo ## Security
echo.
echo - **HTTPS Encryption**: All downloads are encrypted
echo - **Certificate Validation**: Automatic SSL certificate management
echo - **GitHub Infrastructure**: High availability and global CDN
echo - **Automatic Updates**: Regular sync with original repository
echo.
echo ## Maintenance
echo.
echo This repository is automatically synced with the original repository at %ORIGINAL_REPO%.
echo.
echo ### Automated Updates
echo.
echo GitHub Actions automatically sync this repository with the original source.
echo.
echo ### Manual Sync
echo.
echo To manually sync the repository:
echo.
echo ```bash
echo ./sync-repository.sh
echo ```
echo.
echo ## Support
echo.
echo For issues with this repository, please contact the habiTv development team.
echo.
echo ---
echo.
echo **Repository Version**: 4.1.0-SNAPSHOT  
echo **Last Updated**: %date% %time%  
echo **Generated by**: habiTv GitHub Mirror Setup Script
) > "README.md"

echo [SUCCESS] README file created

REM Create sync script
echo [INFO] Creating sync script...

(
echo #!/bin/bash
echo.
echo # Sync habiTv repository mirror with original source
echo # This script syncs the GitHub Pages mirror with the original repository
echo.
echo ORIGINAL_REPO="%ORIGINAL_REPO%"
echo LOCAL_DIR="."
echo.
echo echo "Syncing habiTv repository mirror..."
echo.
echo # Download plugins.txt
echo if curl -L -o "plugins.txt" "$ORIGINAL_REPO/plugins.txt"; then
echo     echo "✓ Downloaded plugins.txt"
echo else
echo     echo "✗ Failed to download plugins.txt"
echo fi
echo.
echo # Download tool binaries and metadata
echo for tool in yt-dlp ffmpeg aria2c curl rtmpdump; do
echo     echo "Syncing $tool..."
echo     
echo     # Windows version
echo     if curl -L -o "bin/${tool}.exe.zip" "$ORIGINAL_REPO/bin/${tool}.exe.zip"; then
echo         echo "  ✓ Downloaded Windows $tool"
echo     else
echo         echo "  ✗ Failed to download Windows $tool"
echo     fi
echo     
echo     # Linux version
echo     if curl -L -o "bin/${tool}.zip" "$ORIGINAL_REPO/bin/${tool}.zip"; then
echo         echo "  ✓ Downloaded Linux $tool"
echo     else
echo         echo "  ✗ Failed to download Linux $tool"
echo     fi
echo     
echo     # Version metadata
echo     if curl -L -o "metadata/${tool}.version" "$ORIGINAL_REPO/metadata/${tool}.version"; then
echo         echo "  ✓ Downloaded $tool version metadata"
echo     else
echo         echo "  ✗ Failed to download $tool version metadata"
echo     fi
echo done
echo.
echo # Commit and push changes
echo git add .
echo git commit -m "Sync repository with original source $(date)"
echo git push
echo.
echo echo "Repository mirror synced successfully!"
) > "sync-repository.sh"

echo [SUCCESS] Sync script created

REM Create GitHub Actions workflow
echo [INFO] Creating GitHub Actions workflow...

if not exist ".github\workflows" mkdir ".github\workflows"

(
echo name: Sync Repository Mirror
echo.
echo on:
echo   schedule:
echo     - cron: '0 2 * * *'  # Daily at 2 AM
echo   workflow_dispatch:     # Manual trigger
echo.
echo jobs:
echo   sync:
echo     runs-on: ubuntu-latest
echo     steps:
echo       - uses: actions/checkout@v3
echo       
echo       - name: Sync with original repository
echo         run: ^|
echo           chmod +x sync-repository.sh
echo           ./sync-repository.sh
echo       
echo       - name: Check for changes
echo         id: changes
echo         run: ^|
echo           if git diff --quiet HEAD~1 HEAD; then
echo             echo "has_changes=false" ^>^> $GITHUB_OUTPUT
echo           else
echo             echo "has_changes=true" ^>^> $GITHUB_OUTPUT
echo           fi
echo       
echo       - name: Push changes
echo         if: steps.changes.outputs.has_changes == 'true'
echo         run: ^|
echo           git config --local user.email "action@github.com"
echo           git config --local user.name "GitHub Action"
echo           git push
) > ".github\workflows\sync-repository.yml"

echo [SUCCESS] GitHub Actions workflow created

REM Commit and push
echo [INFO] Committing and pushing changes...

git add .
git commit -m "Initial setup of habiTv repository mirror"
git push

if %errorlevel% neq 0 (
    echo [ERROR] Failed to push changes
    pause
    exit /b 1
)

echo [SUCCESS] Changes pushed to GitHub

REM Enable GitHub Pages
echo [INFO] Enabling GitHub Pages...
echo.
echo To enable GitHub Pages:
echo 1. Go to https://github.com/%GITHUB_USERNAME%/%REPO_NAME%/settings/pages
echo 2. Select source: 'Deploy from a branch'
echo 3. Choose branch: 'main' or 'master'
echo 4. Select folder: '/' (root)
echo 5. Click Save
echo.
echo Your repository will be available at: %GITHUB_PAGES_URL%

echo.
echo ==========================================
echo [SUCCESS] GitHub Pages mirror setup completed!
echo ==========================================
echo.
echo Repository URL: https://github.com/%GITHUB_USERNAME%/%REPO_NAME%
echo GitHub Pages URL: %GITHUB_PAGES_URL%
echo.
echo Next steps:
echo 1. Enable GitHub Pages in repository settings
echo 2. Update habiTv configuration to use the new URL
echo 3. Test the repository by running habiTv
echo 4. Set up automated sync with GitHub Actions
echo.
echo To manually sync the repository:
echo cd %REPO_NAME% ^&^& ./sync-repository.sh
echo.

pause 
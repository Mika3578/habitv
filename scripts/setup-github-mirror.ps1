# habiTv GitHub Pages Mirror Setup Script (PowerShell)
# This script helps set up a GitHub Pages mirror of the dabiboo.free.fr repository
#
# Version: 4.1.0-SNAPSHOT
# Author: habiTv Team
# Date: June 15, 2025

param(
    [string]$GitHubUsername = "your-username",
    [string]$RepoName = "habitv-tools"
)

# Configuration
$OriginalRepo = "http://dabiboo.free.fr/repository"
$GitHubPagesUrl = "https://${GitHubUsername}.github.io/${RepoName}"

# Colors for output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Blue"
$White = "White"

# Logging functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor $Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor $Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor $Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor $Red
}

# Check dependencies
function Test-Dependencies {
    Write-Info "Checking dependencies..."
    
    $missingTools = @()
    
    # Check for git
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
        $missingTools += "git"
    }
    
    # Check for curl (PowerShell has Invoke-WebRequest)
    if (-not (Get-Command curl -ErrorAction SilentlyContinue)) {
        Write-Warning "curl not found, will use PowerShell's Invoke-WebRequest"
    }
    
    if ($missingTools.Count -gt 0) {
        Write-Error "Missing required tools: $($missingTools -join ', ')"
        Write-Info "Please install the missing tools and try again."
        exit 1
    }
    
    Write-Success "All dependencies are available"
}

# Create GitHub repository
function New-GitHubRepository {
    Write-Info "Setting up GitHub repository..."
    
    # Check if repository already exists
    try {
        $response = Invoke-WebRequest -Uri "https://api.github.com/repos/${GitHubUsername}/${RepoName}" -Method Head -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Warning "Repository ${GitHubUsername}/${RepoName} already exists"
            $continue = Read-Host "Do you want to continue with existing repository? (y/N)"
            if ($continue -notmatch '^[Yy]$') {
                exit 1
            }
        }
    }
    catch {
        Write-Info "Creating GitHub repository ${GitHubUsername}/${RepoName}..."
        
        # Create repository using GitHub API
        $headers = @{
            "Authorization" = "token $env:GITHUB_TOKEN"
            "Accept" = "application/vnd.github.v3+json"
        }
        
        $body = @{
            name = $RepoName
            description = "habiTv External Tools Repository Mirror"
            homepage = $GitHubPagesUrl
            private = $false
            has_issues = $true
            has_wiki = $false
            has_downloads = $true
            auto_init = $true
        } | ConvertTo-Json
        
        try {
            Invoke-WebRequest -Uri "https://api.github.com/user/repos" -Method Post -Headers $headers -Body $body -ContentType "application/json"
            Write-Success "GitHub repository created"
        }
        catch {
            Write-Error "Failed to create GitHub repository: $($_.Exception.Message)"
            exit 1
        }
    }
}

# Clone and setup repository
function Initialize-Repository {
    Write-Info "Setting up local repository..."
    
    # Clone repository
    if (Test-Path $RepoName) {
        Write-Warning "Directory $RepoName already exists"
        $continue = Read-Host "Do you want to remove it and start fresh? (y/N)"
        if ($continue -match '^[Yy]$') {
            Remove-Item -Recurse -Force $RepoName
        }
        else {
            Write-Info "Using existing directory"
            return
        }
    }
    
    try {
        git clone "https://github.com/${GitHubUsername}/${RepoName}.git"
        Set-Location $RepoName
        Write-Success "Repository cloned successfully"
    }
    catch {
        Write-Error "Failed to clone repository: $($_.Exception.Message)"
        exit 1
    }
}

# Download repository content
function Get-RepositoryContent {
    Write-Info "Downloading repository content from ${OriginalRepo}..."
    
    # Create directory structure
    New-Item -ItemType Directory -Force -Path "bin" | Out-Null
    New-Item -ItemType Directory -Force -Path "metadata" | Out-Null
    
    # Download plugins.txt
    try {
        Invoke-WebRequest -Uri "${OriginalRepo}/plugins.txt" -OutFile "plugins.txt"
        Write-Success "Downloaded plugins.txt"
    }
    catch {
        Write-Warning "Failed to download plugins.txt"
    }
    
    # Download tool binaries and metadata
    $tools = @("yt-dlp", "ffmpeg", "aria2c", "curl", "rtmpdump")
    
    foreach ($tool in $tools) {
        Write-Info "Downloading $tool..."
        
        # Windows version
        try {
            Invoke-WebRequest -Uri "${OriginalRepo}/bin/${tool}.exe.zip" -OutFile "bin/${tool}.exe.zip"
            Write-Success "Downloaded Windows $tool"
        }
        catch {
            Write-Warning "Failed to download Windows $tool"
        }
        
        # Linux version
        try {
            Invoke-WebRequest -Uri "${OriginalRepo}/bin/${tool}.zip" -OutFile "bin/${tool}.zip"
            Write-Success "Downloaded Linux $tool"
        }
        catch {
            Write-Warning "Failed to download Linux $tool"
        }
        
        # Version metadata
        try {
            Invoke-WebRequest -Uri "${OriginalRepo}/metadata/${tool}.version" -OutFile "metadata/${tool}.version"
            Write-Success "Downloaded $tool version metadata"
        }
        catch {
            Write-Warning "Failed to download $tool version metadata"
        }
    }
    
    Write-Success "Repository content downloaded"
}

# Create repository configuration
function New-RepositoryConfig {
    Write-Info "Creating repository configuration..."
    
    $config = @"
# habiTv External Tools Repository Configuration
# Version: 4.1.0-SNAPSHOT
# Generated: $(Get-Date)

[repository]
name = habiTv External Tools Repository (GitHub Pages Mirror)
url = ${GitHubPagesUrl}
version = 4.1.0-SNAPSHOT
description = HTTPS mirror of habiTv external tools repository
original_source = ${OriginalRepo}

[security]
checksum_verification = true
signature_verification = false
https_enabled = true
allowed_hosts = github.com, github.io

[update]
check_interval = 86400
retry_attempts = 3
timeout = 30000
auto_sync = true

[tools]
auto_update = true
fallback_sources = true
version_check = true
"@
    
    $config | Out-File -FilePath "repository.conf" -Encoding UTF8
    Write-Success "Repository configuration created"
}

# Create README
function New-RepositoryReadme {
    Write-Info "Creating README file..."
    
    $readme = @"
# habiTv External Tools Repository

This is a GitHub Pages mirror of the habiTv external tools repository, providing secure HTTPS access to required tools.

## Repository Information

- **Original Repository**: ${OriginalRepo}
- **GitHub Pages Mirror**: ${GitHubPagesUrl}
- **Protocol**: HTTPS (encrypted)
- **Status**: Active mirror

## Repository Structure

\`\`\`
${RepoName}/
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
└── README.md             # This file
\`\`\`

## Available Tools

- **yt-dlp**: YouTube and video platform downloader
- **ffmpeg**: Video processing and conversion
- **aria2c**: High-speed download utility
- **curl**: HTTP client and downloader
- **rtmpdump**: RTMP stream downloader

## Usage

### habiTv Configuration

Update your habiTv configuration to use this HTTPS mirror:

\`\`\`xml
<updateConfig>
    <updateOnStartup>true</updateOnStartup>
    <autoriseSnapshot>true</autoriseSnapshot>
    <repositoryUrl>${GitHubPagesUrl}</repositoryUrl>
</updateConfig>
\`\`\`

### Environment Variable

\`\`\`bash
export HABITV_REPOSITORY_URL="${GitHubPagesUrl}"
\`\`\`

### Manual Download

\`\`\`bash
# Download Windows yt-dlp
curl -L -o yt-dlp.exe.zip ${GitHubPagesUrl}/bin/yt-dlp.exe.zip

# Download Linux yt-dlp
curl -L -o yt-dlp.zip ${GitHubPagesUrl}/bin/yt-dlp.zip
\`\`\`

## Security

- **HTTPS Encryption**: All downloads are encrypted
- **Certificate Validation**: Automatic SSL certificate management
- **GitHub Infrastructure**: High availability and global CDN
- **Automatic Updates**: Regular sync with original repository

## Maintenance

This repository is automatically synced with the original repository at ${OriginalRepo}.

### Automated Updates

GitHub Actions automatically sync this repository with the original source.

### Manual Sync

To manually sync the repository:

\`\`\`bash
./sync-repository.sh
\`\`\`

## Support

For issues with this repository, please contact the habiTv development team.

---

**Repository Version**: 4.1.0-SNAPSHOT  
**Last Updated**: $(Get-Date)  
**Generated by**: habiTv GitHub Mirror Setup Script
"@
    
    $readme | Out-File -FilePath "README.md" -Encoding UTF8
    Write-Success "README file created"
}

# Create sync script
function New-SyncScript {
    Write-Info "Creating sync script..."
    
    $syncScript = @'
#!/bin/bash

# Sync habiTv repository mirror with original source
# This script syncs the GitHub Pages mirror with the original repository

ORIGINAL_REPO="http://dabiboo.free.fr/repository"
LOCAL_DIR="."

echo "Syncing habiTv repository mirror..."

# Download plugins.txt
if curl -L -o "plugins.txt" "$ORIGINAL_REPO/plugins.txt"; then
    echo "✓ Downloaded plugins.txt"
else
    echo "✗ Failed to download plugins.txt"
fi

# Download tool binaries and metadata
for tool in yt-dlp ffmpeg aria2c curl rtmpdump; do
    echo "Syncing $tool..."
    
    # Windows version
    if curl -L -o "bin/${tool}.exe.zip" "$ORIGINAL_REPO/bin/${tool}.exe.zip"; then
        echo "  ✓ Downloaded Windows $tool"
    else
        echo "  ✗ Failed to download Windows $tool"
    fi
    
    # Linux version
    if curl -L -o "bin/${tool}.zip" "$ORIGINAL_REPO/bin/${tool}.zip"; then
        echo "  ✓ Downloaded Linux $tool"
    else
        echo "  ✗ Failed to download Linux $tool"
    fi
    
    # Version metadata
    if curl -L -o "metadata/${tool}.version" "$ORIGINAL_REPO/metadata/${tool}.version"; then
        echo "  ✓ Downloaded $tool version metadata"
    else
        echo "  ✗ Failed to download $tool version metadata"
    fi
done

# Commit and push changes
git add .
git commit -m "Sync repository with original source $(date)"
git push

echo "Repository mirror synced successfully!"
'@
    
    $syncScript | Out-File -FilePath "sync-repository.sh" -Encoding UTF8
    Write-Success "Sync script created"
}

# Create GitHub Actions workflow
function New-GitHubActions {
    Write-Info "Creating GitHub Actions workflow..."
    
    New-Item -ItemType Directory -Force -Path ".github/workflows" | Out-Null
    
    $workflow = @"
name: Sync Repository Mirror

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM
  workflow_dispatch:     # Manual trigger

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Sync with original repository
        run: |
          chmod +x sync-repository.sh
          ./sync-repository.sh
      
      - name: Check for changes
        id: changes
        run: |
          if git diff --quiet HEAD~1 HEAD; then
            echo "has_changes=false" >> \$GITHUB_OUTPUT
          else
            echo "has_changes=true" >> \$GITHUB_OUTPUT
          fi
      
      - name: Push changes
        if: steps.changes.outputs.has_changes == 'true'
        run: |
          git config --local user.email "action@github.com"
          git config --local user.name "GitHub Action"
          git push
"@
    
    $workflow | Out-File -FilePath ".github/workflows/sync-repository.yml" -Encoding UTF8
    Write-Success "GitHub Actions workflow created"
}

# Enable GitHub Pages
function Enable-GitHubPages {
    Write-Info "Enabling GitHub Pages..."
    
    Write-Info "To enable GitHub Pages:"
    Write-Host "1. Go to https://github.com/${GitHubUsername}/${RepoName}/settings/pages" -ForegroundColor $Blue
    Write-Host "2. Select source: 'Deploy from a branch'" -ForegroundColor $Blue
    Write-Host "3. Choose branch: 'main' or 'master'" -ForegroundColor $Blue
    Write-Host "4. Select folder: '/' (root)" -ForegroundColor $Blue
    Write-Host "5. Click Save" -ForegroundColor $Blue
    Write-Host ""
    Write-Host "Your repository will be available at: ${GitHubPagesUrl}" -ForegroundColor $Green
}

# Commit and push
function Push-Repository {
    Write-Info "Committing and pushing changes..."
    
    try {
        git add .
        git commit -m "Initial setup of habiTv repository mirror"
        git push
        Write-Success "Changes pushed to GitHub"
    }
    catch {
        Write-Error "Failed to push changes: $($_.Exception.Message)"
        exit 1
    }
}

# Main execution
function Main {
    Write-Host "==========================================" -ForegroundColor $Green
    Write-Host "habiTv GitHub Pages Mirror Setup" -ForegroundColor $Green
    Write-Host "==========================================" -ForegroundColor $Green
    Write-Host ""
    
    # Check for GitHub token
    if (-not $env:GITHUB_TOKEN) {
        Write-Error "GITHUB_TOKEN environment variable is not set"
        Write-Info "Please set your GitHub token:"
        Write-Info '$env:GITHUB_TOKEN = "your_github_token"'
        exit 1
    }
    
    Test-Dependencies
    New-GitHubRepository
    Initialize-Repository
    Get-RepositoryContent
    New-RepositoryConfig
    New-RepositoryReadme
    New-SyncScript
    New-GitHubActions
    Push-Repository
    Enable-GitHubPages
    
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor $Green
    Write-Success "GitHub Pages mirror setup completed!"
    Write-Host "==========================================" -ForegroundColor $Green
    Write-Host ""
    Write-Host "Repository URL: https://github.com/${GitHubUsername}/${RepoName}" -ForegroundColor $Blue
    Write-Host "GitHub Pages URL: ${GitHubPagesUrl}" -ForegroundColor $Blue
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor $Yellow
    Write-Host "1. Enable GitHub Pages in repository settings" -ForegroundColor $White
    Write-Host "2. Update habiTv configuration to use the new URL" -ForegroundColor $White
    Write-Host "3. Test the repository by running habiTv" -ForegroundColor $White
    Write-Host "4. Set up automated sync with GitHub Actions" -ForegroundColor $White
    Write-Host ""
    Write-Host "To manually sync the repository:" -ForegroundColor $Yellow
    Write-Host "cd ${RepoName} && ./sync-repository.sh" -ForegroundColor $White
    Write-Host ""
}

# Run main function
Main 
#!/usr/bin/env pwsh
# Post-deploy script for habiTv Maven artifacts
# This script copies updated artifacts from ~/.m2/repository to docs/repository
# and commits them to git for GitHub Pages deployment

param(
    [string]$MavenRepoPath = "$env:USERPROFILE\.m2\repository",
    [string]$TargetPath = "docs\repository",
    [string]$GroupId = "com\dabi\habitv",
    [string]$CommitMessage = "Deploy Maven artifacts to GitHub Pages"
)

Write-Host "=== habiTv Post-Deploy Script ===" -ForegroundColor Green
Write-Host "Copying Maven artifacts to GitHub Pages repository..." -ForegroundColor Yellow

# Construct source and target paths
$SourcePath = Join-Path $MavenRepoPath $GroupId
$TargetGroupPath = Join-Path $TargetPath $GroupId

# Check if source directory exists
if (-not (Test-Path $SourcePath)) {
    Write-Error "Source directory not found: $SourcePath"
    Write-Host "Make sure you have run 'mvn deploy' first to generate artifacts." -ForegroundColor Red
    exit 1
}

# Create target directory if it doesn't exist
if (-not (Test-Path $TargetGroupPath)) {
    New-Item -ItemType Directory -Path $TargetGroupPath -Force | Out-Null
    Write-Host "Created target directory: $TargetGroupPath" -ForegroundColor Cyan
}

# Copy artifacts recursively
try {
    Copy-Item -Path "$SourcePath\*" -Destination $TargetGroupPath -Recurse -Force
    Write-Host "Successfully copied artifacts from $SourcePath to $TargetGroupPath" -ForegroundColor Green
} catch {
    Write-Error "Failed to copy artifacts: $_"
    exit 1
}

# Check if there are any changes to commit
$GitStatus = git status --porcelain "$TargetPath"
if ([string]::IsNullOrEmpty($GitStatus)) {
    Write-Host "No changes detected in $TargetPath" -ForegroundColor Yellow
    Write-Host "Artifacts may already be up to date." -ForegroundColor Yellow
    exit 0
}

# Add changes to git
Write-Host "Adding changes to git..." -ForegroundColor Yellow
git add "$TargetPath\*"

# Commit changes
Write-Host "Committing changes..." -ForegroundColor Yellow
git commit -m $CommitMessage

if ($LASTEXITCODE -eq 0) {
    Write-Host "Successfully committed changes" -ForegroundColor Green
} else {
    Write-Error "Failed to commit changes"
    exit 1
}

# Push to remote repository
Write-Host "Pushing to remote repository..." -ForegroundColor Yellow
git push

if ($LASTEXITCODE -eq 0) {
    Write-Host "Successfully pushed changes to remote repository" -ForegroundColor Green
    Write-Host "Maven artifacts are now available at: https://mika3578.github.io/habitv/repository/" -ForegroundColor Cyan
} else {
    Write-Error "Failed to push changes to remote repository"
    exit 1
}

Write-Host "=== Post-deploy script completed successfully ===" -ForegroundColor Green 
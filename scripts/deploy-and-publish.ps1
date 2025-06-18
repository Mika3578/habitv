#!/usr/bin/env pwsh
# Combined deploy and publish script for habiTv
# This script runs 'mvn deploy' and then copies artifacts to GitHub Pages

param(
    [string]$MavenGoals = "clean deploy",
    [string]$CommitMessage = "Deploy Maven artifacts to GitHub Pages"
)

Write-Host "=== habiTv Deploy and Publish Script ===" -ForegroundColor Green

# Step 1: Run Maven deploy
Write-Host "Step 1: Running Maven deploy..." -ForegroundColor Yellow
Write-Host "Command: mvn $MavenGoals" -ForegroundColor Cyan

$MavenResult = & mvn $MavenGoals.Split(' ')

if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven deploy failed with exit code $LASTEXITCODE"
    Write-Host "Please fix any build errors and try again." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Maven deploy completed successfully!" -ForegroundColor Green

# Step 2: Run post-deploy script
Write-Host "Step 2: Publishing artifacts to GitHub Pages..." -ForegroundColor Yellow

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PostDeployScript = Join-Path $ScriptDir "post-deploy.ps1"

if (Test-Path $PostDeployScript) {
    & $PostDeployScript -CommitMessage $CommitMessage
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Post-deploy script failed with exit code $LASTEXITCODE"
        exit $LASTEXITCODE
    }
} else {
    Write-Error "Post-deploy script not found: $PostDeployScript"
    exit 1
}

Write-Host "=== Deploy and publish completed successfully ===" -ForegroundColor Green
Write-Host "Your Maven artifacts are now available at: https://mika3578.github.io/habitv/repository/" -ForegroundColor Cyan 
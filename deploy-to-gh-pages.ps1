# Deploy Maven artifacts to GitHub Pages repository
Write-Host "Deploying Maven artifacts to GitHub Pages repository..." -ForegroundColor Green

# Switch to gh-pages branch
git checkout gh-pages

# Clean the repository directory
if (Test-Path "repository") {
    Remove-Item -Recurse -Force "repository"
}

# Create repository directory structure
New-Item -ItemType Directory -Path "repository" -Force
New-Item -ItemType Directory -Path "repository\com\dabi\habitv" -Force

# Copy Maven artifacts from local repository
$localRepo = "$env:USERPROFILE\.m2\repository\com\dabi\habitv"
$ghPagesRepo = "repository\com\dabi\habitv"

if (Test-Path $localRepo) {
    Copy-Item -Path "$localRepo\*" -Destination $ghPagesRepo -Recurse -Force
    Write-Host "Copied Maven artifacts from $localRepo" -ForegroundColor Yellow
} else {
    Write-Host "Local Maven repository not found at $localRepo" -ForegroundColor Red
    exit 1
}

# Add all files to git
git add -A

# Commit the changes
git commit -m "Deploy Maven artifacts for version 5.0.0-SNAPSHOT"

# Push to GitHub Pages
git push origin gh-pages

# Switch back to java8 branch
git checkout java8

Write-Host "Deployment completed!" -ForegroundColor Green
Write-Host "Repository URL: https://mika3578.github.io/habitv/repository/" -ForegroundColor Cyan
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 
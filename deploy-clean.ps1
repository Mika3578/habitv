# Clean deployment script for gh-pages branch
Write-Host "Starting clean deployment to gh-pages..." -ForegroundColor Green

# Switch to gh-pages branch
git checkout gh-pages

# Remove everything except .git
Get-ChildItem -Path . -Exclude .git | Remove-Item -Recurse -Force

# Create fresh repository directory
New-Item -ItemType Directory -Path "repository\com\dabi\habitv" -Force

# Copy fresh Maven artifacts
$localRepo = "$env:USERPROFILE\.m2\repository\com\dabi\habitv"
$ghPagesRepo = "repository\com\dabi\habitv"

if (Test-Path $localRepo) {
    Copy-Item -Path "$localRepo\*" -Destination $ghPagesRepo -Recurse -Force
    Write-Host "Copied Maven artifacts from $localRepo" -ForegroundColor Green
} else {
    Write-Host "Local Maven repository not found at $localRepo" -ForegroundColor Red
    Write-Host "Please run 'mvn clean install' first" -ForegroundColor Yellow
    exit 1
}

# Add only the repository directory
git add repository/

# Commit and push
git commit -m "Deploy: clean Maven artifacts update $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
git push origin gh-pages

Write-Host "Clean deployment completed!" -ForegroundColor Green
Write-Host "Repository URL: https://mika3578.github.io/habitv/repository/" -ForegroundColor Cyan 
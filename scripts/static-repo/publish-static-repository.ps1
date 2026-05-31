<#
.SYNOPSIS
  Convenience wrapper around the Maven static repository deploy profile.

.DESCRIPTION
  Delegates to:

  mvn -B -ntp -Pstatic-repo-deploy deploy "-Dhabitv.static.repo.path=$env:HABITV_REPO_DIR/repository"

  Maven deploy publishes artifacts, external tools, plugins.txt,
  habitv-update-manifest.properties, repository indexes, and manifest validation.
#>
[CmdletBinding()]
param(
    [switch]$SkipTests
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:HABITV_REPO_DIR)) {
    throw "HABITV_REPO_DIR is not set. Point it at the local habitv-repo Git checkout root."
}

$repoRoot = (Resolve-Path $env:HABITV_REPO_DIR).Path
$repositoryPath = Join-Path $repoRoot "repository"
if (-not (Test-Path $repositoryPath)) {
    New-Item -Path $repositoryPath -ItemType Directory -Force | Out-Null
}

$normalized = $repositoryPath.Trim() -replace '\\', '/'
$habitvRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path

Write-Host "Running Maven static repository deploy via -Pstatic-repo-deploy"
Write-Host "Repository path: $normalized"

Push-Location $habitvRoot
try {
    $mvnArgs = @("-B", "-ntp", "-Pstatic-repo-deploy", "deploy", "-Dhabitv.static.repo.path=$normalized")
    if ($SkipTests) {
        $mvnArgs = @("-DskipTests") + $mvnArgs
    }

    & mvn @mvnArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven static repository deploy failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

Write-Host ""
Write-Host "Next steps in habitv-repo (manual Git publication):"
Write-Host "  Push-Location '$repoRoot'"
Write-Host "  git status --short --branch"
Write-Host "  git add repository"
Write-Host "  git commit -m `"repo: update SNAPSHOT versions and refresh metadata`""
Write-Host "  git push"
Write-Host "  Pop-Location"

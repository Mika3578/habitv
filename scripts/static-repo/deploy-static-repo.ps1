param(
    [Parameter(Mandatory = $false)]
    [string]$StaticRepoPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-RepoRoot {
    $scriptDirectory = Split-Path -Parent $PSCommandPath
    return (Resolve-Path (Join-Path $scriptDirectory "..\..")).Path
}

function Get-MavenStaticRepoPath {
    param(
        [string]$RepoRootPath,
        [string]$OverridePath
    )

    $mvnArgs = @(
        "help:evaluate",
        "-Dexpression=habitv.static.repo.path",
        "-q",
        "-DforceStdout"
    )
    if (-not [string]::IsNullOrWhiteSpace($OverridePath)) {
        $normalized = $OverridePath.Trim() -replace '\\', '/'
        $mvnArgs = @("-Dhabitv.static.repo.path=$normalized") + $mvnArgs
    }

    Push-Location $RepoRootPath
    try {
        $evaluated = (& mvn @mvnArgs | Select-Object -Last 1).Trim()
        if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($evaluated)) {
            throw "Unable to evaluate habitv.static.repo.path."
        }
        return ($evaluated -replace '\\', '/')
    }
    finally {
        Pop-Location
    }
}

$repoRoot = Get-RepoRoot
$staticRepoPath = Get-MavenStaticRepoPath -RepoRootPath $repoRoot -OverridePath $StaticRepoPath

if (-not (Test-Path -Path $staticRepoPath -PathType Container)) {
    New-Item -Path $staticRepoPath -ItemType Directory -Force | Out-Null
}

$habitvRepoRoot = Split-Path -Parent $staticRepoPath
Write-Host ("habitv.static.repo.path = {0}" -f $staticRepoPath)
Write-Host "Deploying with Maven property-based file repository (no hardcoded machine path)."

Push-Location $repoRoot
try {
    $deployArgs = @(
        "-B", "-ntp", "-DskipTests", "clean", "deploy",
        '-DaltDeploymentRepository=habitv-local::default::file://${habitv.static.repo.path}'
    )
    if (-not [string]::IsNullOrWhiteSpace($StaticRepoPath)) {
        $normalized = $StaticRepoPath.Trim() -replace '\\', '/'
        $deployArgs = @("-Dhabitv.static.repo.path=$normalized") + $deployArgs
    }

    & mvn @deployArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven deploy failed."
    }
}
finally {
    Pop-Location
}

Write-Host "Deploy complete. Publish with:"
Write-Host "  cd $habitvRepoRoot"
Write-Host "  git status --short"
Write-Host "  git add repository"
Write-Host '  git commit -m "repo: publish habitv artifacts"'
Write-Host "  git push"

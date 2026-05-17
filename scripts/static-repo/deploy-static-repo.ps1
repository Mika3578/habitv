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

function Resolve-StaticRepoPath {
    param(
        [string]$ExplicitPath,
        [string]$RepoRootPath
    )

    $candidates = @()

    if (-not [string]::IsNullOrWhiteSpace($ExplicitPath)) {
        $candidates += @{
            Label = "argument -StaticRepoPath"
            Path = $ExplicitPath
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($env:HABITV_STATIC_REPO_LOCAL_PATH)) {
        $candidates += @{
            Label = "environment HABITV_STATIC_REPO_LOCAL_PATH"
            Path = $env:HABITV_STATIC_REPO_LOCAL_PATH
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($HOME)) {
        $candidates += @{
            Label = "standard home workspace"
            Path = (Join-Path $HOME "dev/habitv-repo")
        }
    }

    $siblingDefault = Join-Path (Split-Path -Parent $RepoRootPath) "habitv-repo"
    $candidates += @{
        Label = "sibling fallback ../habitv-repo"
        Path = $siblingDefault
    }

    foreach ($candidate in $candidates) {
        try {
            $resolved = (Resolve-Path -Path $candidate.Path -ErrorAction Stop).Path
            if (Test-Path -Path $resolved -PathType Container) {
                Write-Host ("Using static repository path from {0}: {1}" -f $candidate.Label, $resolved)
                return $resolved
            }
        }
        catch {
            continue
        }
    }

    $setupMessage = @(
        "Unable to resolve habitv-repo checkout.",
        "Resolution order:",
        "  1. -StaticRepoPath",
        "  2. HABITV_STATIC_REPO_LOCAL_PATH",
        "  3. $HOME/dev/habitv-repo",
        "  4. ../habitv-repo",
        "Expected standard layout:",
        "  $HOME/dev/habitv",
        "  $HOME/dev/habitv-repo"
    ) -join [Environment]::NewLine

    throw $setupMessage
}

function Assert-GitCheckout {
    param(
        [string]$RepoPath
    )

    & git -C $RepoPath rev-parse --is-inside-work-tree *> $null
    if ($LASTEXITCODE -ne 0) {
        throw ("Path is not a Git checkout: {0}" -f $RepoPath)
    }
}

function Assert-HabitvRepoRemote {
    param(
        [string]$RepoPath
    )

    $remoteUrl = (& git -C $RepoPath remote get-url origin).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw ("Unable to read origin remote for {0}" -f $RepoPath)
    }

    if ($remoteUrl -notmatch 'Mika3578/habitv-repo(?:\.git)?$') {
        throw ("Static repository origin must target Mika3578/habitv-repo. Found: {0}" -f $remoteUrl)
    }
}

function Convert-ToFileUrlPath {
    param(
        [string]$PathValue
    )

    return ($PathValue -replace "\\", "/")
}

$repoRoot = Get-RepoRoot
$resolvedStaticRepoPath = Resolve-StaticRepoPath -ExplicitPath $StaticRepoPath -RepoRootPath $repoRoot

if (-not (Test-Path -Path $resolvedStaticRepoPath -PathType Container)) {
    throw ("Static repository path does not exist: {0}" -f $resolvedStaticRepoPath)
}

Assert-GitCheckout -RepoPath $resolvedStaticRepoPath
Assert-HabitvRepoRemote -RepoPath $resolvedStaticRepoPath

$resolvedStaticRepoMavenPath = Join-Path $resolvedStaticRepoPath "maven"
if (-not (Test-Path -Path $resolvedStaticRepoMavenPath -PathType Container)) {
    New-Item -Path $resolvedStaticRepoMavenPath -ItemType Directory | Out-Null
}

$fileUrlPath = Convert-ToFileUrlPath -PathValue $resolvedStaticRepoMavenPath
$deployRepository = "habitv-static-repo::default::file:///$fileUrlPath"

Write-Host ("Deploying Maven artifacts to: {0}" -f $deployRepository)
Push-Location $repoRoot
try {
    & mvn -B -ntp -DskipTests deploy "-DaltDeploymentRepository=$deployRepository"
    if ($LASTEXITCODE -ne 0) {
        throw "Maven deploy failed."
    }
}
finally {
    Pop-Location
}

& git -C $resolvedStaticRepoPath add .
if ($LASTEXITCODE -ne 0) {
    throw "Failed to stage static repository changes."
}

& git -C $resolvedStaticRepoPath diff --cached --quiet
if ($LASTEXITCODE -eq 0) {
    Write-Host "No artifact changes to publish."
    exit 0
}

& git -C $resolvedStaticRepoPath commit -m "repo: publish Habitv Maven artifacts"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to commit static repository changes."
}

& git -C $resolvedStaticRepoPath push
if ($LASTEXITCODE -ne 0) {
    throw "Failed to push static repository changes."
}

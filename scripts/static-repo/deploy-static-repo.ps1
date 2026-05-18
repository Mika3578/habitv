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
                Write-Host ("Using static repository checkout from {0}: {1}" -f $candidate.Label, $resolved)
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

$repoRoot = Get-RepoRoot
$resolvedStaticRepoPath = Resolve-StaticRepoPath -ExplicitPath $StaticRepoPath -RepoRootPath $repoRoot

if (-not (Test-Path -Path $resolvedStaticRepoPath -PathType Container)) {
    throw ("Static repository path does not exist: {0}" -f $resolvedStaticRepoPath)
}

$resolvedRepositoryPath = Join-Path $resolvedStaticRepoPath "repository"
if (-not (Test-Path -Path $resolvedRepositoryPath -PathType Container)) {
    New-Item -Path $resolvedRepositoryPath -ItemType Directory | Out-Null
}

$repositoryFileUrl = ((Resolve-Path $resolvedRepositoryPath).Path -replace '\\', '/')
$deployRepository = "habitv-local::file:///$repositoryFileUrl"

Write-Host ("Deploying Maven artifacts to: {0}" -f $deployRepository)
Push-Location $repoRoot
try {
    & mvn -B -ntp -DskipTests clean deploy "-DaltDeploymentRepository=$deployRepository"
    if ($LASTEXITCODE -ne 0) {
        throw "Maven deploy failed."
    }
}
finally {
    Pop-Location
}

Write-Host "Deploy complete. Publish with:"
Write-Host "  cd $resolvedStaticRepoPath"
Write-Host "  git status --short"
Write-Host "  git add repository"
Write-Host '  git commit -m "repo: publish habitv artifacts"'
Write-Host "  git push"

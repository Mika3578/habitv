param(
    [Parameter(Mandatory = $false)]
    [string]$StaticRepoPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-HabitvRepoDirFromStaticRepoPath {
    param([string]$StaticRepoPath)

    if ([string]::IsNullOrWhiteSpace($StaticRepoPath)) {
        throw "StaticRepoPath is empty."
    }

    $normalized = $StaticRepoPath.Trim().Replace('\', '/')
    while ($normalized.EndsWith('/')) {
        $normalized = $normalized.Substring(0, $normalized.Length - 1)
    }

    if ($normalized.EndsWith('/repository')) {
        $normalized = $normalized.Substring(0, $normalized.Length - '/repository'.Length)
        while ($normalized.EndsWith('/')) {
            $normalized = $normalized.Substring(0, $normalized.Length - 1)
        }
    }

    if ([string]::IsNullOrWhiteSpace($normalized)) {
        throw "StaticRepoPath resolved to an empty habitv-repo root: $StaticRepoPath"
    }

    return ($normalized -replace '/', [System.IO.Path]::DirectorySeparatorChar)
}

if (-not [string]::IsNullOrWhiteSpace($StaticRepoPath)) {
    $env:HABITV_REPO_DIR = Resolve-HabitvRepoDirFromStaticRepoPath -StaticRepoPath $StaticRepoPath
}

& (Join-Path $PSScriptRoot "publish-static-repository.ps1") -SkipTests

param(
    [switch]$DependencyTree
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-RepoRoot {
    $scriptDirectory = Split-Path -Parent $PSCommandPath
    return (Resolve-Path (Join-Path $scriptDirectory "..\..")).Path
}

function Invoke-Maven {
    param(
        [string]$RepoRootPath,
        [string[]]$Arguments
    )

    Push-Location $RepoRootPath
    try {
        Write-Host ("mvn " + ($Arguments -join " "))
        & mvn @Arguments
        if ($LASTEXITCODE -ne 0) {
            throw "Maven failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}

$repoRoot = Get-RepoRoot
Write-Host "Repository root: $repoRoot"

$pomFiles = Get-ChildItem -Path $repoRoot -Filter pom.xml -Recurse -File |
    Where-Object { $_.FullName -notmatch '[\\/]target[\\/]' } |
    Sort-Object FullName

Write-Host ""
Write-Host "pom.xml inventory ($($pomFiles.Count) files):"
foreach ($pom in $pomFiles) {
    $relative = $pom.FullName.Substring($repoRoot.Length).TrimStart('\', '/')
    Write-Host "  $relative"
}

$targetDir = Join-Path $repoRoot "target"
if (-not (Test-Path -Path $targetDir -PathType Container)) {
    New-Item -Path $targetDir -ItemType Directory -Force | Out-Null
}

Invoke-Maven -RepoRootPath $repoRoot -Arguments @(
    "-B", "-ntp", "-DskipTests", "validate"
)

if ($DependencyTree) {
    $treeOutput = Join-Path $targetDir "dependency-tree.txt"
    Invoke-Maven -RepoRootPath $repoRoot -Arguments @(
        "-B", "-ntp", "dependency:tree",
        "-DoutputFile=$treeOutput"
    )
    Write-Host "Wrote dependency tree to $treeOutput"
}

Write-Host "Maven dependency inventory completed successfully."

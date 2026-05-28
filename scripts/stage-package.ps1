param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-RepoRoot {
    return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

$repoRoot = Get-RepoRoot
Push-Location $repoRoot
try {
    $version = (& mvn help:evaluate "-Dexpression=project.version" -q "-DforceStdout" | Select-Object -Last 1).Trim()
    if ([string]::IsNullOrWhiteSpace($version)) {
        throw "Unable to resolve project.version from Maven."
    }

    $staging = Join-Path $repoRoot "target/package-staging/Habitv"
    $libDir = Join-Path $staging "lib"
    $pluginsDir = Join-Path $libDir "plugins"
    $binDir = Join-Path $staging "bin"

    if (Test-Path (Join-Path $repoRoot "target/package-staging")) {
        Remove-Item -Recurse -Force (Join-Path $repoRoot "target/package-staging")
    }
    New-Item -ItemType Directory -Force -Path $libDir, $pluginsDir, $binDir | Out-Null

    $mainJar = Join-Path $repoRoot "application/habiTv/target/habiTv-$version.jar"
    if (-not (Test-Path -Path $mainJar)) {
        throw "Main JAR not found: $mainJar. Run: mvn -B -ntp -DskipTests package"
    }
    Copy-Item -Path $mainJar -Destination $libDir -Force

    $pluginCount = 0
    Get-ChildItem -Path (Join-Path $repoRoot "plugins") -Directory | ForEach-Object {
        if ($_.Name -eq "plugin-tester") {
            return
        }
        $targetDir = Join-Path $_.FullName "target"
        if (-not (Test-Path -Path $targetDir)) {
            return
        }
        Get-ChildItem -Path $targetDir -Filter "$($_.Name)-*.jar" -File | ForEach-Object {
            switch -Regex ($_.Name) {
                '(-sources|-javadoc|-tests)\.jar$|^original-' { return }
            }
            Copy-Item -Path $_.FullName -Destination $pluginsDir -Force
            $script:pluginCount++
        }
    }

    $commonReadme = Join-Path $repoRoot "packaging/common/README.txt"
    if (Test-Path -Path $commonReadme) {
        Copy-Item -Path $commonReadme -Destination (Join-Path $staging "README.txt") -Force
    }

    $envFile = Join-Path $repoRoot "target/package-staging/.env"
    @(
        "VERSION=$version"
        "STAGING=$staging"
    ) | Set-Content -Path $envFile -Encoding ASCII

    Write-Host "Staged Habitv $version at $staging ($pluginCount plugins)"
}
finally {
    Pop-Location
}

param(
    [switch]$SkipInnoSetup,
    [switch]$SkipLaunch4j
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-RepoRoot {
    return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function Read-StagingEnv {
    param([string]$EnvFile)
    $values = @{}
    Get-Content -Path $EnvFile | ForEach-Object {
        if ($_ -match '^(?<key>[A-Z_]+)=(?<value>.*)$') {
            $values[$Matches.key] = $Matches.value
        }
    }
    return $values
}

function Invoke-StagePackage {
    & (Join-Path $PSScriptRoot "stage-package.ps1")
    if ($LASTEXITCODE -ne 0) {
        throw "stage-package.ps1 failed with exit code $LASTEXITCODE"
    }
}

function Find-Launch4j {
    $cmd = Get-Command launch4j -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }
    $candidates = @(
        (Join-Path $env:ProgramFiles "Launch4j\launch4j.exe"),
        (Join-Path ${env:ProgramFiles(x86)} "Launch4j\launch4j.exe"),
        (Join-Path $env:ChocolateyInstall "lib\launch4j\tools\launch4j.exe")
    )
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -Path $candidate)) {
            return $candidate
        }
    }
    return $null
}

function Find-InnoSetupCompiler {
    $cmd = Get-Command iscc -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }
    $candidates = @(
        (Join-Path ${env:ProgramFiles(x86)} "Inno Setup 6\ISCC.exe"),
        (Join-Path $env:ProgramFiles "Inno Setup 6\ISCC.exe")
    )
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -Path $candidate)) {
            return $candidate
        }
    }
    return $null
}

$repoRoot = Get-RepoRoot
Push-Location $repoRoot
try {
    Invoke-StagePackage

    $envFile = Join-Path $repoRoot "target/package-staging/.env"
    $stagingEnv = Read-StagingEnv -EnvFile $envFile
    $version = $stagingEnv["VERSION"]
    $staging = $stagingEnv["STAGING"]

    if (-not $version -or -not $staging) {
        throw "Unable to read staging metadata from $envFile"
    }

    $packagesDir = Join-Path $repoRoot "target/packages"
    New-Item -ItemType Directory -Force -Path $packagesDir | Out-Null

    $stagingBinDir = Join-Path $staging "bin"
    New-Item -ItemType Directory -Force -Path $stagingBinDir | Out-Null

    Copy-Item -Path (Join-Path $repoRoot "packaging/windows/bin/habitv.bat") `
        -Destination (Join-Path $stagingBinDir "habitv.bat") -Force

    $mainJarName = "habiTv-$version.jar"
    $mainJarPath = Join-Path (Join-Path $staging "lib") $mainJarName
    if (-not (Test-Path -Path $mainJarPath)) {
        throw "Main JAR not found in staging: $mainJarPath"
    }

    $launch4jXml = Join-Path $repoRoot "target/packages/launch4j.xml"
    $launch4jOutExe = Join-Path $stagingBinDir "Habitv.exe"
    $packagesExe = Join-Path $packagesDir "Habitv.exe"
    $iconPath = (Join-Path $repoRoot "packaging/windows/installer/habitv.ico").Replace("\", "/")
    $launch4jOutPath = $launch4jOutExe.Replace("\", "/")
    $launch4jContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>true</dontWrapJar>
  <headerType>gui</headerType>
  <jar>lib/$mainJarName</jar>
  <outfile>$launch4jOutPath</outfile>
  <errTitle>Habitv</errTitle>
  <cmdLine></cmdLine>
  <chdir>`$EXEDIR/..</chdir>
  <priority>normal</priority>
  <downloadUrl>https://bell-sw.com/pages/downloads/</downloadUrl>
  <supportUrl>https://github.com/Mika3578/habitv</supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon>$iconPath</icon>
  <jre>
    <path></path>
    <bundledJre64Bit>false</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>1.8.0</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64/32</runtimeBits>
  </jre>
  <messages>
    <startupErr>Failed to start Habitv.</startupErr>
    <bundledJreErr>This launcher does not bundle Java.</bundledJreErr>
    <jreVersionErr>Java 8 with JavaFX support is required.</jreVersionErr>
    <launcherErr>Failed to launch Habitv.</launcherErr>
  </messages>
</launch4jConfig>
"@
    Set-Content -Path $launch4jXml -Value $launch4jContent -Encoding UTF8

    if (-not $SkipLaunch4j) {
        $launch4j = Find-Launch4j
        if (-not $launch4j) {
            throw "Launch4j was not found. Install Launch4j or pass -SkipLaunch4j for staging-only validation."
        }
        & $launch4j $launch4jXml | Out-Host
        $deadline = (Get-Date).AddSeconds(30)
        while (-not (Test-Path -LiteralPath $launch4jOutExe) -and (Get-Date) -lt $deadline) {
            Start-Sleep -Milliseconds 200
        }
        if (-not (Test-Path -LiteralPath $launch4jOutExe)) {
            throw "Launch4j did not produce $launch4jOutExe (exit code $LASTEXITCODE)"
        }
        Copy-Item -Path $launch4jOutExe -Destination $packagesExe -Force
    }

    $zipPath = Join-Path $packagesDir "habitv-windows.zip"
    if (Test-Path -Path $zipPath) {
        Remove-Item -Path $zipPath -Force
    }
    Compress-Archive -Path (Join-Path $repoRoot "target/package-staging/Habitv") `
        -DestinationPath $zipPath -Force

    if (-not (Test-Path -Path $zipPath) -or ((Get-Item $zipPath).Length -eq 0)) {
        throw "Windows archive is missing or empty: $zipPath"
    }

    if (-not $SkipInnoSetup) {
        $issTemplate = Get-Content -Path (Join-Path $repoRoot "packaging/windows/installer/habitv.iss") -Raw
        $issRendered = $issTemplate `
            -replace '@VERSION@', $version `
            -replace '@OUTPUT_DIR@', $packagesDir `
            -replace '@STAGING_DIR@', $staging

        $issPath = Join-Path $repoRoot "target/packages/habitv.iss"
        Set-Content -Path $issPath -Value $issRendered -Encoding UTF8

        $iscc = Find-InnoSetupCompiler
        if (-not $iscc) {
            throw "Inno Setup compiler (ISCC.exe) was not found."
        }
        & $iscc $issPath
        if ($LASTEXITCODE -ne 0) {
            throw "Inno Setup failed with exit code $LASTEXITCODE"
        }
        $setupPath = Join-Path $packagesDir "HabitvSetup.exe"
        if (-not (Test-Path -Path $setupPath) -or ((Get-Item $setupPath).Length -eq 0)) {
            throw "Installer is missing or empty: $setupPath"
        }
    }

    Write-Host "Windows packages written to $packagesDir"
    Write-Host "  - habitv-windows.zip"
    if (-not $SkipLaunch4j) {
        Write-Host "  - Habitv.exe"
    }
    if (-not $SkipInnoSetup) {
        Write-Host "  - HabitvSetup.exe"
    }
}
finally {
    Pop-Location
}

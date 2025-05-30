# PowerShell script to run habiTv
param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$Arguments
)

Write-Host "Starting habiTv 4.1.0-SNAPSHOT..." -ForegroundColor Green
Write-Host ""

try {
    & java -jar "application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar" @Arguments
    $exitCode = $LASTEXITCODE
}
catch {
    Write-Host "Error running habiTv: $_" -ForegroundColor Red
    $exitCode = 1
}

Write-Host ""
Write-Host "habiTv execution completed with exit code: $exitCode" -ForegroundColor $(if ($exitCode -eq 0) { "Green" } else { "Red" })

if ($exitCode -ne 0) {
    exit $exitCode
}
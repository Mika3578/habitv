# PR body hygiene helper (advisory only).
# Usage: pwsh -File scripts/validate-pr-public-body.ps1 [-Body $text] [-BodyFile path]
# Always exits 0. Third-party PR description footers (Sourcery, cubic, Cursor)
# are tolerated and are not a merge or Ready gate.

param(
    [string] $Body = "",
    [string] $BodyFile = ""
)

$ErrorActionPreference = "Stop"

if ($BodyFile) {
    $Body = Get-Content -Raw $BodyFile
} elseif (-not $Body -and $env:PR_BODY) {
    $Body = $env:PR_BODY
}

Write-Host "pr-public-body: OK (advisory; tool footers not enforced)"
exit 0

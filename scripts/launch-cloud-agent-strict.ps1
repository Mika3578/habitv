# Start a Cursor Cloud Agent on a pre-created canonical branch (strict naming).
# Requires CURSOR_API_KEY in the environment (never commit secrets).
#
# Example:
#   git fetch origin develop
#   git push -u origin fix/arte   # after creating the branch locally, when ready
#   $env:CURSOR_API_KEY = '...'   # from local secret storage
#   pwsh -File scripts/launch-cloud-agent-strict.ps1 -Type fix -Scope arte -Prompt "Fix listing"
#
# See .agents/skills/git-workflow/SKILL.md and docs/development.md.

#requires -Version 7.0
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("feat", "fix", "docs", "test", "refactor", "chore", "ci")]
    [string] $Type,

    [Parameter(Mandatory = $true)]
    [string] $Scope,

    [Parameter(Mandatory = $true)]
    [string] $Prompt,

    [string] $RepoUrl = "https://github.com/Mika3578/habitv",

    [string] $StartingRef = "develop",

    [switch] $AutoCreatePR
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:CURSOR_API_KEY)) {
    throw "CURSOR_API_KEY is not set. Use local secret storage; never commit API keys."
}

if ($Scope -notmatch '^[a-z0-9][a-z0-9-]*$') {
    throw "Scope must be lowercase kebab-case."
}

$branch = "$Type/$Scope"
$body = @{
    prompt = @{ text = $Prompt }
    repos  = @(
        @{
            url         = $RepoUrl
            startingRef = $branch
        }
    )
    workOnCurrentBranch = $true
    autoCreatePR        = [bool]$AutoCreatePR
} | ConvertTo-Json -Depth 5

$headers = @{
    Authorization = "Bearer $($env:CURSOR_API_KEY)"
    "Content-Type" = "application/json"
}

Write-Host "Launching Cloud Agent on branch $branch (workOnCurrentBranch=true)"
Write-Host "Ensure branch $branch exists on the remote before the agent pushes."

$response = Invoke-RestMethod -Method Post -Uri "https://api.cursor.com/v1/agents" -Headers $headers -Body $body
$response | ConvertTo-Json -Depth 6

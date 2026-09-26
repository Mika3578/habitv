# Deterministic checks for repository agent-policy layout.
# Usage: scripts/validate-agent-policy.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$failures = 0
function Fail([string]$Message) {
    Write-Host "agent-policy: $Message"
    $script:failures++
}

$AdapterMaxBytes = 8192
$SkillDescMax = 500

if (-not (Test-Path "AGENTS.md")) {
    Fail "missing root AGENTS.md"
}

Get-ChildItem -Recurse -Filter "AGENTS.md" -File -Force |
    Where-Object {
        $rel = $_.FullName.Substring($Root.Length + 1) -replace '\\', '/'
        $rel -ne "AGENTS.md" -and
            $rel -notmatch '^agent_space/' -and
            $rel -notmatch '^\.git/'
    } |
    ForEach-Object { Fail "unexpected nested AGENTS.md: $($_.FullName.Substring($Root.Length + 1))" }

@("CLAUDE.md", "GEMINI.md", ".cursorrules", ".windsurfrules") | ForEach-Object {
    if (Test-Path $_) {
        $size = (Get-Item $_).Length
        if ($size -gt 200) {
            Fail "competing substantive file present: $_ ($size bytes)"
        }
    }
}

function Test-Adapter([string]$Path) {
    if (-not (Test-Path $Path)) { return }
    $size = (Get-Item $Path).Length
    if ($size -gt $AdapterMaxBytes) {
        Fail "adapter too large ($size bytes): $Path"
    }
    $text = Get-Content -Raw $Path
    if ($text -notmatch "AGENTS\.md") {
        Fail "adapter must reference AGENTS.md: $Path"
    }
}

if (Test-Path ".continue/rules") {
    Get-ChildItem ".continue/rules" -Filter "*.md" -File | ForEach-Object {
        if ($_.Name -ne "00-habitv.md") {
            Fail "unexpected Continue rule (use 00-habitv.md only): $($_.FullName.Substring($Root.Length + 1))"
        }
    }
}

Test-Adapter ".continue/rules/00-habitv.md"
Test-Adapter ".github/copilot-instructions.md"
Test-Adapter ".cursor/CLOUD.md"

$publicGitRule = ".cursor/rules/public-git-text.mdc"
if (-not (Test-Path $publicGitRule)) {
    Fail "missing thin Cursor rule: $publicGitRule"
} else {
    $ruleText = Get-Content -Raw $publicGitRule
    if ($ruleText -notmatch "\.agents/skills/public-git-text") {
        Fail "public-git-text Cursor rule must reference portable skill: $publicGitRule"
    }
    $parts = $ruleText -split "(?m)^---\s*$", 0
    if ($parts.Count -ge 3) {
        $body = $parts[2]
        if ($body -match "(?m)^\s*-\s") {
            Fail "thin Cursor rule must not duplicate policy bullets: $publicGitRule"
        }
    }
}

if (-not (Test-Path ".cursor/hooks.json")) {
    Fail "missing .cursor/hooks.json"
} elseif (-not (Test-Path ".cursor/hooks/before-shell-branch-policy.sh")) {
    Fail "missing branch policy hook script"
} else {
    try {
        $hooksJson = Get-Content -Raw ".cursor/hooks.json" | ConvertFrom-Json
        $hookRegistered = $false
        if ($hooksJson.hooks.beforeShellExecution) {
            foreach ($entry in @($hooksJson.hooks.beforeShellExecution)) {
                if ($entry.command -match "before-shell-branch-policy\.sh") {
                    $hookRegistered = $true
                    break
                }
            }
        }
        if (-not $hookRegistered) {
            Fail ".cursor/hooks.json must register before-shell-branch-policy hook command"
        }
    } catch {
        Fail ".cursor/hooks.json is not valid JSON: $($_.Exception.Message)"
    }
}

$gitWorkflowSkill = ".agents/skills/git-workflow/SKILL.md"
if ((Test-Path $gitWorkflowSkill) -and -not (Select-String -Path $gitWorkflowSkill -Pattern "workOnCurrentBranch" -Quiet)) {
    Fail "git-workflow skill must document Cloud workOnCurrentBranch workflow"
}

if (Test-Path ".cursor/skills") {
    Get-ChildItem ".cursor/skills" -Recurse -Filter "SKILL.md" | ForEach-Object {
        $text = Get-Content -Raw $_.FullName
        if ($text -notmatch "\.agents/skills/") {
            Fail "Cursor skill must point to .agents/skills/: $($_.FullName)"
        }
    }
}

$RequiredSkills = @(
    "public-git-text",
    "git-workflow",
    "code-change-verification",
    "provider-diagnostics",
    "pr-review"
)
$SkillsRoot = ".agents/skills"
$skillFiles = @()
if (-not (Test-Path $SkillsRoot)) {
    Fail "missing .agents/skills directory"
} else {
    $skillFiles = @(Get-ChildItem $SkillsRoot -Recurse -Filter "SKILL.md" -File -Force)
    if ($skillFiles.Count -eq 0) {
        Fail "no SKILL.md files under .agents/skills"
    }
    foreach ($req in $RequiredSkills) {
        $requiredPath = Join-Path $SkillsRoot "$req/SKILL.md"
        if (-not (Test-Path $requiredPath)) {
            Fail "missing required skill: $requiredPath"
        }
    }

    $skillNames = @{}
    $skillFiles | ForEach-Object {
        $rel = $_.FullName.Substring($Root.Length + 1)
        $lines = Get-Content $_.FullName
        if ($lines.Count -lt 3 -or $lines[0] -ne "---") {
            Fail "skill missing YAML frontmatter: $rel"
            return
        }
        $frontmatter = @()
        $closed = $false
        for ($i = 1; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -eq "---") {
                $closed = $true
                break
            }
            $frontmatter += $lines[$i]
        }
        if (-not $closed) {
            Fail "skill missing YAML frontmatter: $rel"
            return
        }
        if (-not ($frontmatter -cmatch "^name:")) { Fail "skill missing name metadata: $rel" }
        if (-not ($frontmatter -cmatch "^description:")) { Fail "skill missing description metadata: $rel" }
        $name = ($frontmatter | Where-Object { $_ -cmatch "^name:" } | Select-Object -First 1) -replace "^name:\s*", ""
        $desc = ($frontmatter | Where-Object { $_ -cmatch "^description:" } | Select-Object -First 1) -replace "^description:\s*", ""
        if ([string]::IsNullOrWhiteSpace($name)) { Fail "empty skill name: $rel" }
        if ([string]::IsNullOrWhiteSpace($desc)) { Fail "empty skill description: $rel" }
        if ($desc.Length -gt $SkillDescMax) { Fail "skill description too long ($($desc.Length) chars): $rel" }
        if (-not [string]::IsNullOrEmpty($name) -and $skillNames.ContainsKey($name)) {
            Fail "duplicate skill name '$name': $($skillNames[$name]) and $rel"
        }
        if (-not [string]::IsNullOrEmpty($name)) {
            $skillNames[$name] = $rel
        }
    }
}

$agents = Get-Content -Raw "AGENTS.md"
@(
    "Keep every pull request in **Draft**",
    "current PR HEAD",
    "explicitly confirms success in the current conversation"
) | ForEach-Object {
    if ($agents.IndexOf($_, [StringComparison]::Ordinal) -lt 0) {
        Fail "AGENTS.md missing required phrase: $_"
    }
}

if ($failures -gt 0) {
    Write-Host "agent-policy: FAILED ($failures check(s))"
    exit 1
}

Write-Host "agent-policy: OK"
exit 0

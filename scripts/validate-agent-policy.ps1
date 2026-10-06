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
    if ($text.IndexOf("AGENTS.md", [System.StringComparison]::Ordinal) -lt 0) {
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
        if (-not ($frontmatter -cmatch "^name:")) { Fail "skill missing name metadata: $rel"; return }
        if (-not ($frontmatter -cmatch "^description:")) { Fail "skill missing description metadata: $rel"; return }
        $name = ($frontmatter | Where-Object { $_ -cmatch "^name:" } | Select-Object -First 1) -replace "^name:\s*", ""
        $descParts = [System.Collections.Generic.List[string]]::new()
        $grabbing = $false
        foreach ($line in $frontmatter) {
            if ($line -cmatch '^description:\s*[>|][-+]?\s*$') {
                $grabbing = $true
                continue
            }
            if ($line -cmatch '^description:\s*') {
                $descParts.Add(($line -replace '^description:\s*', ''))
                break
            }
            if ($grabbing) {
                if ($line -cmatch '^[A-Za-z0-9_-]+:') { break }
                $descParts.Add($line.Trim())
            }
        }
        $desc = (($descParts -join ' ') -replace '\s+', ' ').Trim()
        if ([string]::IsNullOrWhiteSpace($name)) { Fail "empty skill name: $rel"; return }
        if ([string]::IsNullOrWhiteSpace($desc)) { Fail "empty skill description: $rel"; return }
        if ($desc.Length -gt $SkillDescMax) { Fail "skill description too long ($($desc.Length) chars): $rel"; return }
        if ($skillNames.ContainsKey($name)) {
            Fail "duplicate skill name '$name': $($skillNames[$name]) and $rel"
            return
        }
        $skillNames[$name] = $rel
    }
}

$agents = Get-Content -Raw "AGENTS.md"
@(
    "Keep every pull request in **Draft**",
    "current PR HEAD",
    "explicitly confirms success in the current conversation",
    "Do not resolve unanswered",
    "Live GitHub PR state is authoritative",
    "only the PR orchestrator",
    "reviewer-trigger comments",
    "@coderabbitai full review",
    "/q review",
    "Do not request Copilot during Draft"
) | ForEach-Object {
    if ($agents.IndexOf($_, [StringComparison]::Ordinal) -lt 0) {
        Fail "AGENTS.md missing required phrase: $_"
    }
}

$prReviewSkill = ".agents/skills/pr-review/SKILL.md"
if (-not (Test-Path $prReviewSkill)) {
    Fail "missing pr-review skill: $prReviewSkill"
} else {
    $prReviewText = Get-Content -Raw $prReviewSkill
    if ($prReviewText -notmatch "REVIEW_HEAD") {
        Fail "pr-review skill must document HEAD-bound review rounds"
    }
    if ($prReviewText -notmatch "validate-pr-public-body") {
        Fail "pr-review skill must reference validate-pr-public-body scripts"
    }
    if ($prReviewText -notmatch "READY_GATE") {
        Fail "pr-review skill must document READY_GATE orchestration"
    }
    if ($prReviewText -notmatch "BLOCKING") {
        Fail "pr-review skill must document finding classification"
    }
    if ($prReviewText -notmatch "Single-writer") {
        Fail "pr-review skill must document single-writer rule"
    }
    if ($prReviewText -notmatch "pr-gh-snapshot") {
        Fail "pr-review skill must reference pr-gh-snapshot scripts"
    }
    if ($prReviewText -cnotmatch "APPROVAL_ONLY") {
        Fail "pr-review skill must distinguish approval-only reviews"
    }
    if ($prReviewText -cnotmatch "execution_state") {
        Fail "pr-review skill must document review execution state"
    }
    if ($prReviewText.IndexOf("Agent-owned reviewer triggering", [StringComparison]::Ordinal) -lt 0) {
        Fail "pr-review skill must document agent-owned reviewer triggering"
    }
    if ($prReviewText.IndexOf("@coderabbitai full review", [StringComparison]::Ordinal) -lt 0) {
        Fail "pr-review skill must require @coderabbitai full review before Ready"
    }
    if ($prReviewText.IndexOf("/q review", [StringComparison]::Ordinal) -lt 0) {
        Fail "pr-review skill must document Amazon Q /q review on a stabilized HEAD"
    }
    if ($prReviewText.IndexOf("Do not request Copilot before Ready", [StringComparison]::Ordinal) -lt 0) {
        Fail "pr-review skill must keep Copilot final-only after Ready"
    }
    if ($prReviewText.IndexOf("must not independently post", [StringComparison]::Ordinal) -lt 0) {
        Fail "pr-review skill must keep single-writer reviewer-request rule"
    }
}

if (-not (Test-Path ".coderabbit.yaml")) {
    Fail "missing .coderabbit.yaml"
} else {
    function Get-YamlChildBlock([string] $Text, [string] $Key) {
        $lines = $Text -split "`n"
        $capture = $false
        $out = New-Object System.Collections.Generic.List[string]
        foreach ($line in $lines) {
            $trimEnd = $line.TrimEnd("`r")
            if ($trimEnd -match "^  $([regex]::Escape($Key)):") {
                $capture = $true
                continue
            }
            if ($capture -and $trimEnd -match '^  \S') {
                break
            }
            if ($capture) {
                [void]$out.Add($trimEnd)
            }
        }
        return ($out -join "`n")
    }
    function Get-YamlNestedBlock([string] $Text, [string] $Key) {
        $lines = $Text -split "`n"
        $capture = $false
        $out = New-Object System.Collections.Generic.List[string]
        foreach ($line in $lines) {
            $trimEnd = $line.TrimEnd("`r")
            if ($trimEnd -match "^    $([regex]::Escape($Key)):") {
                $capture = $true
                continue
            }
            if ($capture -and $trimEnd -match '^    \S') {
                break
            }
            if ($capture) {
                [void]$out.Add($trimEnd)
            }
        }
        return ($out -join "`n")
    }
    $crText = (Get-Content -Raw ".coderabbit.yaml") -replace "`r", ""
    if ($crText -notmatch '(?m)^[ \t]*profile:[ \t]*assertive[ \t]*$') {
        Fail ".coderabbit.yaml must set profile: assertive"
    }
    if ($crText -notmatch '(?m)^[ \t]*request_changes_workflow:[ \t]*false[ \t]*$') {
        Fail ".coderabbit.yaml must set request_changes_workflow: false"
    }
    $autoReview = Get-YamlChildBlock $crText "auto_review"
    if ($autoReview -notmatch '(?m)^[ \t]*enabled:[ \t]*true[ \t]*$') {
        Fail ".coderabbit.yaml must set auto_review.enabled: true"
    }
    if ($autoReview -notmatch '(?m)^[ \t]*drafts:[ \t]*true[ \t]*$') {
        Fail ".coderabbit.yaml must set auto_review.drafts: true"
    }
    if ($autoReview -notmatch '(?m)^[ \t]*auto_incremental_review:[ \t]*true[ \t]*$') {
        Fail ".coderabbit.yaml must set auto_review.auto_incremental_review: true"
    }
    $finishing = Get-YamlChildBlock $crText "finishing_touches"
    foreach ($feat in @("autofix", "fix_ci", "resolve_merge_conflict")) {
        $featBlock = Get-YamlNestedBlock $finishing $feat
        if ($featBlock -notmatch '(?m)^[ \t]*enabled:[ \t]*false[ \t]*$') {
            Fail ".coderabbit.yaml must disable finishing_touches.$feat"
        }
    }
}

@(
    "scripts/validate-pr-public-body.sh",
    "scripts/validate-pr-public-body.ps1",
    "scripts/pr-gh-snapshot.sh",
    "scripts/pr-gh-snapshot.ps1",
    "scripts/pr-classify-review-sources.sh",
    "scripts/pr-classify-review-sources.ps1"
) | ForEach-Object {
    if (-not (Test-Path $_)) {
        Fail "missing PR orchestration script: $_"
    }
}

if ($failures -gt 0) {
    Write-Host "agent-policy: FAILED ($failures check(s))"
    exit 1
}

Write-Host "agent-policy: OK"
exit 0

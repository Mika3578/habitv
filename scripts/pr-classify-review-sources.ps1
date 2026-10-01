# Classify review *execution* state from live GitHub review + issue comments.
# Used by pr-gh-snapshot.ps1. Not a substitute for orchestrator adjudication.

param(
    [string] $HeadSha,
    [object] $Reviews,
    [object] $IssueComments,
    [object] $StatusRollup
)

function Test-BodyMatch($text, [string[]] $patterns) {
    if (-not $text) { return $false }
    foreach ($p in $patterns) {
        if ($text -like "*$p*") { return $true }
    }
    return $false
}

function Test-ExactLogin([string] $login, [string[]] $allowed) {
    if (-not $login) { return $false }
    $lower = $login.ToLowerInvariant()
    foreach ($a in $allowed) {
        if ($a.ToLowerInvariant() -eq $lower) { return $true }
    }
    return $false
}

$sources = [System.Collections.Generic.List[object]]::new()

# --- Copilot (required final reviewer when policy applies) ---
# Exact GitHub App bot identities only (no squattable human logins).
$copilotLogins = @(
    'copilot-pull-request-reviewer[bot]',
    'github-copilot[bot]'
)
$copilot = @($Reviews | Where-Object { Test-ExactLogin $_.author.login $copilotLogins })
if ($copilot.Count -eq 0) {
    $sources.Add([ordered]@{ source = 'copilot'; execution_state = 'MISSING'; commit = '' })
} else {
    $onHead = @($copilot | Where-Object { $_.commit.oid -eq $HeadSha })
    if ($onHead.Count -eq 0) {
        $sources.Add([ordered]@{ source = 'copilot'; execution_state = 'STALE'; commit = $copilot[-1].commit.oid })
    } else {
        $r = $onHead[-1]
        $sources.Add([ordered]@{
            source           = 'copilot'
            execution_state  = if ($r.state -eq 'APPROVED') { 'NO_FINDINGS' } else { 'SUBSTANTIVE' }
            commit           = $r.commit.oid
            github_state     = $r.state
        })
    }
}

# --- Amazon Q ---
$amazonQLogins = @('amazon-q-developer[bot]', 'amazon-q[bot]')
$aq = @($Reviews | Where-Object { Test-ExactLogin $_.author.login $amazonQLogins })
if ($aq.Count -gt 0) {
    $r = $aq[-1]
    if ($r.commit.oid -ne $HeadSha) {
        $st = 'STALE'
    } elseif ($r.state -eq 'APPROVED') {
        $st = 'NO_FINDINGS'
    } else {
        $st = 'SUBSTANTIVE'
    }
    $sources.Add([ordered]@{ source = 'amazon-q'; execution_state = $st; commit = $r.commit.oid; github_state = $r.state })
}

# --- Cursor / routing approval ---
$cursor = @($Reviews | Where-Object {
    Test-ExactLogin $_.author.login @('cursor[bot]')
})
foreach ($r in $cursor) {
    $st = 'APPROVAL_ONLY'
    if (Test-BodyMatch $r.body @('Bugbot was not present', 'did not report findings that need human review')) {
        $st = 'APPROVAL_ONLY'
    } elseif ($r.commit.oid -eq $HeadSha -and $r.state -eq 'APPROVED') {
        $st = 'APPROVAL_ONLY'
    } elseif ($r.commit.oid -ne $HeadSha) {
        $st = 'STALE'
    }
    $sources.Add([ordered]@{ source = 'cursor'; execution_state = $st; commit = $r.commit.oid; github_state = $r.state })
}

# --- Issue comments (bots that post to conversation, not always as reviews) ---
$codeRabbitLogins = @('coderabbitai[bot]')
$cr = @($IssueComments | Where-Object { Test-ExactLogin $_.user.login $codeRabbitLogins })
if ($cr.Count -gt 0) {
    $body = ($cr | ForEach-Object { $_.body }) -join ' '
    $st = if (Test-BodyMatch $body @('does not receive automatic reviews', 'fewer than 10 stars', 'Review skipped')) { 'SKIPPED' } else { 'PENDING' }
    $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = $st })
}

$sourceryLogins = @('sourcery-ai[bot]')
$so = @($IssueComments | Where-Object { Test-ExactLogin $_.user.login $sourceryLogins })
if ($so.Count -gt 0) {
    $body = ($so | ForEach-Object { $_.body }) -join ' '
    if (Test-BodyMatch $body @('diff characters', 'quota', '6 days', '6 hours')) {
        $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'RATE_LIMITED' })
    } elseif (Test-BodyMatch $body @("Reviewer's Guide", 'review_guide')) {
        $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'SUMMARY_ONLY' })
    } else {
        $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'PENDING' })
    }
}

$sonarLogins = @('sonarqubecloud[bot]', 'sonarcloud[bot]')
$sonar = @($IssueComments | Where-Object { Test-ExactLogin $_.user.login $sonarLogins })
if ($sonar.Count -gt 0) {
    $sources.Add([ordered]@{ source = 'sonarcloud'; execution_state = 'STATIC_ANALYSIS' })
}

# Do not infer CodeRabbit SKIPPED from a status context alone.

$countsForFinal = @('SUBSTANTIVE', 'NO_FINDINGS')
$onHeadSubstantive = @($sources | Where-Object {
    $countsForFinal -contains $_.execution_state -and $_.commit -eq $HeadSha
})

return [ordered]@{
    review_sources              = $sources
    substantive_review_on_head  = ($onHeadSubstantive.Count -gt 0)
    final_review_gate_eligible  = ($onHeadSubstantive | Where-Object { $_.source -eq 'copilot' }).Count -gt 0
}

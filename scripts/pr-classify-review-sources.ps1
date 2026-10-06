# Classify review *execution* state from live GitHub review + issue comments.
# Used by pr-gh-snapshot.ps1. Not a substitute for orchestrator adjudication.
# CodeRabbit APPROVED is iterative only; it never sets final_review_gate_eligible.

param(
    [switch] $SelfTest,
    [string] $HeadSha,
    [object] $Reviews,
    [object] $IssueComments,
    [object] $StatusRollup
)

$ErrorActionPreference = "Stop"

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

function Get-TrimmedBody($text) {
    if ($null -eq $text) { return "" }
    return ([string]$text).Trim()
}

function Get-ReviewerRequests($IssueComments) {
    $reqs = [System.Collections.Generic.List[object]]::new()
    $seen = @{}
    foreach ($c in @($IssueComments)) {
        $t = Get-TrimmedBody $c.body
        $item = $null
        if ($t -eq "@coderabbitai full review") {
            $item = [ordered]@{ reviewer = "coderabbit"; request_command = "@coderabbitai full review" }
        } elseif ($t -eq "@coderabbitai review") {
            $item = [ordered]@{ reviewer = "coderabbit"; request_command = "@coderabbitai review" }
        } elseif ($t -eq "/q review") {
            $item = [ordered]@{ reviewer = "amazon-q"; request_command = "/q review" }
        } elseif ($t -eq "@sourcery-ai review") {
            $item = [ordered]@{ reviewer = "sourcery"; request_command = "@sourcery-ai review" }
        }
        if ($null -ne $item) {
            $key = "$($item.reviewer)|$($item.request_command)"
            if (-not $seen.ContainsKey($key)) {
                $seen[$key] = $true
                $reqs.Add($item)
            }
        }
    }
    return @($reqs)
}

function Invoke-Classify([string] $HeadSha, $Reviews, $IssueComments) {
    $sources = [System.Collections.Generic.List[object]]::new()
    $reqs = @(Get-ReviewerRequests $IssueComments)
    $reqReviewers = @($reqs | ForEach-Object { $_.reviewer })

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
    } elseif ($reqReviewers -contains 'amazon-q') {
        $sources.Add([ordered]@{ source = 'amazon-q'; execution_state = 'PENDING' })
    }

    $cursor = @($Reviews | Where-Object {
        Test-ExactLogin $_.author.login @('cursor[bot]')
    })
    foreach ($r in $cursor) {
        $st = if ($r.commit.oid -ne $HeadSha) { 'STALE' } else { 'APPROVAL_ONLY' }
        $sources.Add([ordered]@{ source = 'cursor'; execution_state = $st; commit = $r.commit.oid; github_state = $r.state })
    }

    $codeRabbitLogins = @('coderabbitai[bot]')
    $crSkipPhrases = @(
        'does not receive automatic reviews',
        'fewer than 10 stars',
        'Review skipped',
        'Bot user detected',
        'Draft PRs are not automatically reviewed',
        'Draft PR not reviewed'
    )
    $crRatePhrases = @('Review rate limited')
    $crReviews = @($Reviews | Where-Object { Test-ExactLogin $_.author.login $codeRabbitLogins })
    $crComments = @($IssueComments | Where-Object { Test-ExactLogin $_.user.login $codeRabbitLogins } |
        Sort-Object { $_.updated_at })
    $crOnHead = @($crReviews | Where-Object { $_.commit.oid -eq $HeadSha })
    $crLastCommentBody = ''
    if ($crComments.Count -gt 0) {
        $crLastCommentBody = [string]$crComments[-1].body
    }
    if ($crOnHead.Count -gt 0) {
        $r = $crOnHead[-1]
        $body = [string]$r.body
        if (Test-BodyMatch $body $crRatePhrases) {
            $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = 'RATE_LIMITED'; commit = $r.commit.oid })
        } elseif (Test-BodyMatch $body $crSkipPhrases) {
            $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = 'SKIPPED'; commit = $r.commit.oid })
        } elseif ($r.state -eq 'APPROVED') {
            $sources.Add([ordered]@{
                source           = 'coderabbit'
                execution_state  = 'NO_FINDINGS'
                commit           = $r.commit.oid
                github_state     = $r.state
            })
        } else {
            $sources.Add([ordered]@{
                source           = 'coderabbit'
                execution_state  = 'SUBSTANTIVE'
                commit           = $r.commit.oid
                github_state     = $r.state
            })
        }
    } elseif ($crComments.Count -gt 0 -and (Test-BodyMatch $crLastCommentBody $crRatePhrases)) {
        $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = 'RATE_LIMITED' })
    } elseif ($crComments.Count -gt 0 -and (Test-BodyMatch $crLastCommentBody $crSkipPhrases)) {
        $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = 'SKIPPED' })
    } elseif ($crReviews.Count -gt 0) {
        $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = 'STALE'; commit = $crReviews[-1].commit.oid })
    } elseif ($crComments.Count -gt 0 -or ($reqReviewers -contains 'coderabbit')) {
        $sources.Add([ordered]@{ source = 'coderabbit'; execution_state = 'PENDING' })
    }

    $sourceryLogins = @('sourcery-ai[bot]')
    $so = @($IssueComments | Where-Object { Test-ExactLogin $_.user.login $sourceryLogins } |
        Sort-Object { $_.updated_at } | Select-Object -Last 1)
    if ($so.Count -gt 0) {
        $body = [string]$so[0].body
        if (Test-BodyMatch $body @('diff characters', 'quota', '6 days', '6 hours')) {
            $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'RATE_LIMITED' })
        } elseif (Test-BodyMatch $body @("Reviewer's Guide", 'review_guide')) {
            $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'SUMMARY_ONLY' })
        } else {
            $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'PENDING' })
        }
    } elseif ($reqReviewers -contains 'sourcery') {
        $sources.Add([ordered]@{ source = 'sourcery'; execution_state = 'PENDING' })
    }

    $sonarLogins = @('sonarqubecloud[bot]', 'sonarcloud[bot]')
    $sonar = @($IssueComments | Where-Object { Test-ExactLogin $_.user.login $sonarLogins })
    if ($sonar.Count -gt 0) {
        $sources.Add([ordered]@{ source = 'sonarcloud'; execution_state = 'STATIC_ANALYSIS' })
    }

    $countsForFinal = @('SUBSTANTIVE', 'NO_FINDINGS')
    $onHeadSubstantive = @($sources | Where-Object {
        $countsForFinal -contains $_.execution_state -and $_.commit -eq $HeadSha
    })

    $humanApprovedOnHead = @($Reviews | Where-Object {
        $_.state -eq 'APPROVED' -and $_.commit.oid -eq $HeadSha -and
        ($_.author.login -notmatch '\[bot\]$')
    }).Count -gt 0
    $copilotEligible = ($onHeadSubstantive | Where-Object { $_.source -eq 'copilot' }).Count -gt 0
    $iterativeOnHead = ($onHeadSubstantive | Where-Object { $_.source -eq 'coderabbit' }).Count -gt 0

    return [ordered]@{
        review_sources             = $sources
        reviewer_requests          = $reqs
        substantive_review_on_head = ($onHeadSubstantive.Count -gt 0 -or $humanApprovedOnHead)
        final_review_gate_eligible = ($copilotEligible -or $humanApprovedOnHead)
        iterative_review_on_head   = $iterativeOnHead
    }
}

if ($SelfTest) {
    $td = Join-Path $PSScriptRoot "testdata/pr-classify-review-sources"
    $head = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    function Read-JsonFile([string] $Path) {
        return (Get-Content -Raw $Path | ConvertFrom-Json)
    }
    function Assert-True([string] $Name, [bool] $Cond) {
        if (-not $Cond) {
            Write-Error "pr-classify-review-sources: self-test failed: $Name"
            exit 1
        }
    }
    $skip = Invoke-Classify $head (Read-JsonFile "$td/reviews-empty.json") (Read-JsonFile "$td/comments-skip.json")
    Assert-True "skip" (@($skip.review_sources | Where-Object { $_.source -eq "coderabbit" -and $_.execution_state -eq "SKIPPED" }).Count -gt 0)
    $rate = Invoke-Classify $head (Read-JsonFile "$td/reviews-empty.json") (Read-JsonFile "$td/comments-rate-limited.json")
    Assert-True "rate" (@($rate.review_sources | Where-Object { $_.source -eq "coderabbit" -and $_.execution_state -eq "RATE_LIMITED" }).Count -gt 0)
    $cr = Invoke-Classify $head (Read-JsonFile "$td/reviews-coderabbit-approved.json") (Read-JsonFile "$td/comments-empty.json")
    Assert-True "cr_approved" ($cr.iterative_review_on_head -eq $true -and $cr.final_review_gate_eligible -eq $false)
    $cp = Invoke-Classify $head (Read-JsonFile "$td/reviews-copilot-commented.json") (Read-JsonFile "$td/comments-empty.json")
    Assert-True "copilot" ($cp.final_review_gate_eligible -eq $true)
    $aq = Invoke-Classify $head (Read-JsonFile "$td/reviews-amazon-q-stale.json") (Read-JsonFile "$td/comments-empty.json")
    Assert-True "aq_stale" (@($aq.review_sources | Where-Object { $_.source -eq "amazon-q" -and $_.execution_state -eq "STALE" }).Count -gt 0)
    $tr = Invoke-Classify $head (Read-JsonFile "$td/reviews-empty.json") (Read-JsonFile "$td/comments-triggers.json")
    $cmds = @($tr.reviewer_requests | ForEach-Object { $_.request_command })
    Assert-True "full_review_cmd" ($cmds -contains "@coderabbitai full review")
    Assert-True "q_review_cmd" ($cmds -contains "/q review")
    Assert-True "no_embedded_sourcery" (-not ($cmds -contains "@sourcery-ai review"))
    Assert-True "cr_pending" (@($tr.review_sources | Where-Object { $_.source -eq "coderabbit" -and $_.execution_state -eq "PENDING" }).Count -gt 0)
    Assert-True "aq_pending" (@($tr.review_sources | Where-Object { $_.source -eq "amazon-q" -and $_.execution_state -eq "PENDING" }).Count -gt 0)
    Write-Host "pr-classify-review-sources: self-test OK"
    exit 0
}

return Invoke-Classify $HeadSha $Reviews $IssueComments

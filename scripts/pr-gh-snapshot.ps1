# Deterministic live PR snapshot for orchestration (read-only GitHub).
# Usage: pwsh -File scripts/pr-gh-snapshot.ps1 -Repo owner/name -PrNumber 243
# Prints JSON to stdout.

param(
    [Parameter(Mandatory = $true)]
    [string] $Repo,
    [Parameter(Mandatory = $true)]
    [int] $PrNumber
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Error "pr-gh-snapshot: gh CLI required"
    exit 1
}

$prJson = gh pr view $PrNumber --repo $Repo --json number,title,isDraft,baseRefName,headRefName,headRefOid,body,reviewRequests,reviews,statusCheckRollup | ConvertFrom-Json
$issueComments = @(gh api "repos/$Repo/issues/$PrNumber/comments" --paginate | ConvertFrom-Json)
if ($issueComments.Count -eq 1 -and $null -eq $issueComments[0].id) { $issueComments = @() }

$owner, $name = $Repo.Split("/", 2)
$gql = @"
query(`$owner: String!, `$name: String!, `$number: Int!) {
  repository(owner: `$owner, name: `$name) {
    pullRequest(number: `$number) {
      reviewThreads(first: 100) {
        nodes { isResolved isOutdated path }
      }
    }
  }
}
"@
$threadsRaw = gh api graphql -f query=$gql -f owner=$owner -f name=$name -F number=$PrNumber | ConvertFrom-Json
$nodes = $threadsRaw.data.repository.pullRequest.reviewThreads.nodes
if (-not $nodes) { $nodes = @() }

$unresolved = @($nodes | Where-Object { -not $_.isResolved }).Count
$outdated = @($nodes | Where-Object { $_.isOutdated }).Count
$total = @($nodes).Count

$bodyOk = $false
$env:PR_BODY = $prJson.body
& "$PSScriptRoot/validate-pr-public-body.ps1" | Out-Null
if ($LASTEXITCODE -eq 0) { $bodyOk = $true }

$classify = & "$PSScriptRoot/pr-classify-review-sources.ps1" `
    -HeadSha $prJson.headRefOid `
    -Reviews $prJson.reviews `
    -IssueComments $issueComments `
    -StatusRollup $prJson.statusCheckRollup

$out = [ordered]@{
    repository                   = $Repo
    pr_number                    = $prJson.number
    title                        = $prJson.title
    is_draft                     = $prJson.isDraft
    base                         = $prJson.baseRefName
    head_branch                  = $prJson.headRefName
    head_sha                     = $prJson.headRefOid
    body_policy_ok               = $bodyOk
    review_threads               = [ordered]@{
        total      = $total
        unresolved = $unresolved
        outdated   = $outdated
    }
    review_requests              = $prJson.reviewRequests
    reviews                      = $prJson.reviews
    status_check_rollup          = $prJson.statusCheckRollup
    review_sources               = $classify.review_sources
    substantive_review_on_head   = $classify.substantive_review_on_head
    final_review_gate_eligible   = $classify.final_review_gate_eligible
}

$out | ConvertTo-Json -Depth 12 -Compress

exit 0

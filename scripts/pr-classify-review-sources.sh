#!/usr/bin/env bash
# Classify review execution state (bash). Args: head_sha reviews_json comments_json
# Prints JSON object. Used by pr-gh-snapshot.sh.

set -euo pipefail

head_sha="${1:-}"
reviews_json="${2:-[]}"
comments_json="${3:-[]}"

if [[ -z "$head_sha" ]] || ! command -v jq >/dev/null 2>&1; then
  echo '{"review_sources":[],"substantive_review_on_head":false,"final_review_gate_eligible":false}'
  exit 0
fi

jq -n \
  --arg head "$head_sha" \
  --argjson reviews "$reviews_json" \
  --argjson comments "$comments_json" \
  '
  def matchp($text; $pat): ($text // "") | index($pat) != null;
  def body_has($text; $pats):
    reduce $pats[] as $p (false; . or matchp($text; $p));

  ($reviews | map(select(.author.login | test("copilot";"i")))) as $copilot |
  ($reviews | map(select(.author.login | test("amazon-q";"i")))) as $aq |
  ($reviews | map(select(.author.login | test("cursor";"i")))) as $cursor |
  ($comments | map(select(.user.login | test("coderabbit";"i")))) as $cr_c |
  ($comments | map(select(.user.login | test("sourcery";"i")))) as $so_c |
  ($comments | map(select(.user.login | test("sonar";"i")))) as $sonar_c |

  ([]) as $src |
  ($src
    + if ($copilot | length) == 0 then [{source:"copilot",execution_state:"MISSING",commit:""}]
      elif (($copilot | map(select(.commit.oid == $head)) | length) == 0)
      then [{source:"copilot",execution_state:"STALE",commit:$copilot[-1].commit.oid}]
      else [{source:"copilot",execution_state:"SUBSTANTIVE",commit:($copilot | map(select(.commit.oid == $head))[-1].commit.oid),github_state:($copilot | map(select(.commit.oid == $head))[-1].state)}]
      end
    + if ($aq | length) > 0 then
        [{source:"amazon-q",execution_state:(if $aq[-1].commit.oid == $head then "SUBSTANTIVE" else "STALE" end),commit:$aq[-1].commit.oid,github_state:$aq[-1].state}]
      else [] end
    + ($cursor | map({
        source:"cursor",
        execution_state:(if .commit.oid != $head then "STALE"
          elif body_has(.body; ["Bugbot was not present","did not report findings that need human review"]) then "APPROVAL_ONLY"
          else "APPROVAL_ONLY" end),
        commit:.commit.oid,
        github_state:.state
      }))
    + if ($cr_c | length) > 0 then
        [{source:"coderabbit",execution_state:(if body_has(($cr_c | map(.body) | join(" ")); ["does not receive automatic reviews","fewer than 10 stars","Review skipped"]) then "SKIPPED" else "PENDING" end)}]
      else [] end
    + if ($so_c | length) > 0 then
        [{source:"sourcery",execution_state:(if body_has(($so_c | map(.body) | join(" ")); ["diff characters","quota","6 days"]) then "RATE_LIMITED"
          elif body_has(($so_c | map(.body) | join(" ")); ["Reviewer'\''s Guide","review_guide"]) then "SUMMARY_ONLY"
          else "PENDING" end)}]
      else [] end
    + if ($sonar_c | length) > 0 then [{source:"sonarcloud",execution_state:"STATIC_ANALYSIS"}] else [] end
  ) as $sources |

  {
    review_sources: $sources,
    substantive_review_on_head: ($sources | any(.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and any(.commit == $head)),
    final_review_gate_eligible: ($sources | any(.source == "copilot" and (.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and .commit == $head))
  }
  '

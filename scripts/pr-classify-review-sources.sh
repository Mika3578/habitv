#!/usr/bin/env bash
# Classify review execution state (bash).
# Args: head_sha reviews_json_or_file comments_json_or_file
# Prints JSON object. Used by pr-gh-snapshot.sh.

set -euo pipefail

head_sha="${1:-}"
reviews_arg="${2:-[]}"
comments_arg="${3:-[]}"

if [[ -z "$head_sha" ]] || ! command -v jq >/dev/null 2>&1; then
  echo '{"review_sources":[],"substantive_review_on_head":false,"final_review_gate_eligible":false}'
  exit 0
fi

if [[ -f "$reviews_arg" ]]; then
  reviews_json="$(cat "$reviews_arg")"
else
  reviews_json="$reviews_arg"
fi
if [[ -f "$comments_arg" ]]; then
  comments_json="$(cat "$comments_arg")"
else
  comments_json="$comments_arg"
fi

jq -n \
  --arg head "$head_sha" \
  --argjson reviews "$reviews_json" \
  --argjson comments "$comments_json" \
  '
  def matchp($text; $pat): ($text // "") | index($pat) != null;
  def body_has($text; $pats):
    reduce $pats[] as $p (false; . or matchp($text; $p));
  def is_copilot_login($login):
    ($login // "") | ascii_downcase | . == "copilot-pull-request-reviewer[bot]"
      or . == "copilot-pull-request-reviewer"
      or . == "copilot" or . == "github-copilot[bot]";

  ($reviews | map(select(is_copilot_login(.author.login)))) as $copilot |
  ($reviews | map(select(.author.login | test("amazon-q";"i")))) as $aq |
  ($reviews | map(select(.author.login | test("^cursor(\\[bot\\])?$";"i")))) as $cursor |
  ($comments | map(select(.user.login | test("coderabbit";"i")))) as $cr_c |
  ($comments | map(select(.user.login | test("sourcery";"i")))) as $so_c |
  ($comments | map(select(.user.login | test("sonar";"i")))) as $sonar_c |

  ([]) as $src |
  ($src
    + if ($copilot | length) == 0 then [{source:"copilot",execution_state:"MISSING",commit:""}]
      elif (($copilot | map(select(.commit.oid == $head)) | length) == 0)
      then [{source:"copilot",execution_state:"STALE",commit:$copilot[-1].commit.oid}]
      else
        (($copilot | map(select(.commit.oid == $head))[-1]) as $r |
          [{source:"copilot",
            execution_state:(if ($r.state == "COMMENTED" or $r.state == "CHANGES_REQUESTED") then "SUBSTANTIVE" else "NO_FINDINGS" end),
            commit:$r.commit.oid,
            github_state:$r.state}])
      end
    + if ($aq | length) > 0 then
        [{source:"amazon-q",execution_state:(if $aq[-1].commit.oid == $head then "SUBSTANTIVE" else "STALE" end),commit:$aq[-1].commit.oid,github_state:$aq[-1].state}]
      else [] end
    + ($cursor | map({
        source:"cursor",
        execution_state:(if .commit.oid != $head then "STALE" else "APPROVAL_ONLY" end),
        commit:.commit.oid,
        github_state:.state
      }))
    + if ($cr_c | length) > 0 then
        [{source:"coderabbit",execution_state:(if body_has(($cr_c | map(.body) | join(" ")); ["does not receive automatic reviews","fewer than 10 stars","Review skipped"]) then "SKIPPED" else "PENDING" end)}]
      else [] end
    + if ($so_c | length) > 0 then
        [{source:"sourcery",execution_state:(if body_has(($so_c | map(.body) | join(" ")); ["diff characters","quota","6 days","6 hours"]) then "RATE_LIMITED"
          elif body_has(($so_c | map(.body) | join(" ")); ["Reviewer'\''s Guide","review_guide"]) then "SUMMARY_ONLY"
          else "PENDING" end)}]
      else [] end
    + if ($sonar_c | length) > 0 then [{source:"sonarcloud",execution_state:"STATIC_ANALYSIS"}] else [] end
  ) as $sources |

  {
    review_sources: $sources,
    substantive_review_on_head: ($sources | any((.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and .commit == $head)),
    final_review_gate_eligible: ($sources | any(.source == "copilot" and (.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and .commit == $head))
  }
  '

#!/usr/bin/env bash
# Classify review execution state (bash).
# Args: head_sha reviews_json_or_file comments_json_or_file
# Prints JSON object. Used by pr-gh-snapshot.sh.

set -euo pipefail

head_sha="${1:-}"
reviews_arg="${2:-[]}"
comments_arg="${3:-[]}"

if [[ -z "$head_sha" ]] || ! command -v jq >/dev/null 2>&1; then
  echo '{"review_sources":[],"substantive_review_on_head":false,"final_review_gate_eligible":false,"iterative_review_on_head":false}'
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
      or . == "github-copilot[bot]";
  def is_amazon_q_login($login):
    ($login // "") | ascii_downcase | . == "amazon-q-developer[bot]"
      or . == "amazon-q[bot]";
  def is_cursor_login($login):
    ($login // "") | ascii_downcase | . == "cursor[bot]";
  def is_coderabbit_login($login):
    ($login // "") | ascii_downcase | . == "coderabbitai[bot]";
  def is_sourcery_login($login):
    ($login // "") | ascii_downcase | . == "sourcery-ai[bot]";
  def is_sonar_login($login):
    ($login // "") | ascii_downcase | . == "sonarqubecloud[bot]"
      or . == "sonarcloud[bot]";

  def cr_skip_pats: [
    "does not receive automatic reviews",
    "fewer than 10 stars",
    "Review skipped",
    "Bot user detected",
    "Draft PRs are not automatically reviewed",
    "Draft PR not reviewed"
  ];

  ($reviews | map(select(is_copilot_login(.author.login)))) as $copilot |
  ($reviews | map(select(is_amazon_q_login(.author.login)))) as $aq |
  ($reviews | map(select(is_cursor_login(.author.login)))) as $cursor |
  ($reviews | map(select(is_coderabbit_login(.author.login)))) as $cr_r |
  ($comments | map(select(is_coderabbit_login(.user.login)))) as $cr_c |
  ($comments | map(select(is_sourcery_login(.user.login)))) as $so_c |
  ($comments | map(select(is_sonar_login(.user.login)))) as $sonar_c |
  ($reviews | map(select(
      (.state == "APPROVED")
      and (.commit.oid == $head)
      and (((.author.login // "") | test("\\[bot\\]$")) | not)
    ))) as $human_approved |

  ([]) as $src |
  ($src
    + if ($copilot | length) == 0 then [{source:"copilot",execution_state:"MISSING",commit:""}]
      elif (($copilot | map(select(.commit.oid == $head)) | length) == 0)
      then [{source:"copilot",execution_state:"STALE",commit:$copilot[-1].commit.oid}]
      else
        (($copilot | map(select(.commit.oid == $head))[-1]) as $r |
          [{source:"copilot",
            execution_state:(if $r.state == "APPROVED" then "NO_FINDINGS" else "SUBSTANTIVE" end),
            commit:$r.commit.oid,
            github_state:$r.state}])
      end
    + if ($aq | length) > 0 then
        (($aq[-1]) as $r |
          [{source:"amazon-q",
            execution_state:(if $r.commit.oid != $head then "STALE"
              elif $r.state == "APPROVED" then "NO_FINDINGS"
              else "SUBSTANTIVE" end),
            commit:$r.commit.oid,
            github_state:$r.state}])
      else [] end
    + ($cursor | map({
        source:"cursor",
        execution_state:(if .commit.oid != $head then "STALE" else "APPROVAL_ONLY" end),
        commit:.commit.oid,
        github_state:.state
      }))
    + (
        (($cr_r | map(select(.commit.oid == $head))) as $cr_head |
         ($cr_c[-1].body // "") as $cr_last_comment |
         if ($cr_head | length) > 0 then
           (($cr_head[-1]) as $r |
            if body_has(($r.body // ""); cr_skip_pats) then
              [{source:"coderabbit",execution_state:"SKIPPED",commit:$r.commit.oid}]
            elif $r.state == "APPROVED" then
              [{source:"coderabbit",execution_state:"NO_FINDINGS",commit:$r.commit.oid,github_state:$r.state}]
            else
              [{source:"coderabbit",execution_state:"SUBSTANTIVE",commit:$r.commit.oid,github_state:$r.state}]
            end)
         elif ($cr_c | length) > 0 and body_has($cr_last_comment; cr_skip_pats) then
           [{source:"coderabbit",execution_state:"SKIPPED"}]
         elif ($cr_r | length) > 0 then
           [{source:"coderabbit",execution_state:"STALE",commit:$cr_r[-1].commit.oid}]
         elif ($cr_c | length) > 0 then
           [{source:"coderabbit",execution_state:"PENDING"}]
         else [] end)
      )
    + if ($so_c | length) > 0 then
        (($so_c[-1].body) as $so_body |
          [{source:"sourcery",execution_state:(if body_has($so_body; ["diff characters","quota","6 days","6 hours"]) then "RATE_LIMITED"
            elif body_has($so_body; ["Reviewer'\''s Guide","review_guide"]) then "SUMMARY_ONLY"
            else "PENDING" end)}])
      else [] end
    + if ($sonar_c | length) > 0 then [{source:"sonarcloud",execution_state:"STATIC_ANALYSIS"}] else [] end
  ) as $sources |

  {
    review_sources: $sources,
    substantive_review_on_head: (
      ($sources | any((.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and .commit == $head))
      or (($human_approved | length) > 0)
    ),
    final_review_gate_eligible: (
      ($sources | any(.source == "copilot" and (.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and .commit == $head))
      or (($human_approved | length) > 0)
    ),
    iterative_review_on_head: (
      ($sources | any(.source == "coderabbit" and (.execution_state == "SUBSTANTIVE" or .execution_state == "NO_FINDINGS") and .commit == $head))
    )
  }
  '

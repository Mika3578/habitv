#!/usr/bin/env python3
"""Validate habitv-update-manifest.properties points at the newest local SNAPSHOT jars."""

from __future__ import print_function

import argparse
import os
import re
import sys


BUILD_ID_PATTERN = re.compile(r"-(\d{8}\.\d{6}-\d+)(?:-all)?\.jar$")
OBSOLETE_HOST_PATTERN = re.compile(r"dabiboo|free\.fr", re.IGNORECASE)
VALID_ENTRY_TYPES = frozenset(["plugin", "tool"])


def fail(errors, message):
    errors.append(message)


def build_sort_key(filename):
    """Return a sortable key; timestamped Maven SNAPSHOT jars outrank plain *-SNAPSHOT.jar names."""
    match = BUILD_ID_PATTERN.search(filename)
    if not match:
        return (0, "", -1, -1, 0, filename)

    build_id = match.group(1)
    parts = build_id.split("-", 1)
    if len(parts) != 2:
        return (0, "", -1, -1, 0, filename)

    date_time, build_number = parts
    if "." not in date_time:
        return (0, "", -1, -1, 0, filename)

    date_part, time_part = date_time.split(".", 1)
    try:
        all_penalty = 1 if filename.endswith("-all.jar") else 0
        return (1, date_part, int(time_part), int(build_number), -all_penalty, filename)
    except ValueError:
        return (0, "", -1, -1, 0, filename)


def newest_jar_in_directory(version_dir):
    jars = []
    for name in os.listdir(version_dir):
        if not name.endswith(".jar"):
            continue
        if name.endswith("-sources.jar") or name.endswith("-javadoc.jar"):
            continue
        jars.append(name)
    if not jars:
        return None
    return max(jars, key=build_sort_key)


def parse_manifest_line(line):
    """Parse one manifest line: type|groupId|artifactId|version|packaging|relativeUrl[|checksum]."""
    parts = line.split("|")
    if len(parts) < 6:
        return None
    if parts[0] not in VALID_ENTRY_TYPES:
        return None

    entry_type = parts[0]
    group_id = parts[1]
    artifact_id = parts[2]
    version = parts[3]
    packaging = parts[4]
    relative_url = parts[5]
    checksum = parts[6] if len(parts) > 6 else None

    return {
        "type": entry_type,
        "groupId": group_id,
        "artifactId": artifact_id,
        "version": version,
        "packaging": packaging,
        "relativeUrl": relative_url.replace("\\", "/"),
        "checksum": checksum,
    }


def parse_manifest(manifest_path, errors):
    entries = []
    if not os.path.isfile(manifest_path):
        fail(errors, "Missing manifest {}".format(manifest_path))
        return entries

    with open(manifest_path, "r", encoding="utf-8") as handle:
        for line_number, raw_line in enumerate(handle, start=1):
            line = raw_line.strip()
            if not line or line.startswith("#"):
                continue
            if OBSOLETE_HOST_PATTERN.search(line):
                fail(
                    errors,
                    "Obsolete host reference in manifest line {}: {}".format(
                        line_number, line
                    ),
                )
                continue

            parsed = parse_manifest_line(line)
            if parsed is None:
                fail(
                    errors,
                    "Invalid manifest line {}: {}".format(line_number, line),
                )
                continue

            parsed["line"] = line_number
            entries.append(parsed)

    return entries


def validate_manifest_entries(repository_root, entries, errors):
    for entry in entries:
        relative_url = entry["relativeUrl"]
        target_path = os.path.join(repository_root, *relative_url.split("/"))
        if not os.path.isfile(target_path):
            fail(
                errors,
                "Manifest line {} references missing file: {}".format(
                    entry["line"], relative_url
                ),
            )
            continue

        if entry["type"] != "plugin":
            continue

        version_dir = os.path.dirname(target_path)
        manifest_jar = os.path.basename(target_path)
        newest_jar = newest_jar_in_directory(version_dir)
        if newest_jar is None:
            fail(
                errors,
                "No plugin jars found for manifest line {} in {}".format(
                    entry["line"], version_dir
                ),
            )
            continue

        if manifest_jar != newest_jar:
            fail(
                errors,
                (
                    "Stale manifest entry for {0}: manifest points to {1}, "
                    "newest local jar is {2}"
                ).format(entry["artifactId"], manifest_jar, newest_jar),
            )


def parse_args():
    parser = argparse.ArgumentParser(
        description=(
            "Validate habitv-update-manifest.properties freshness and file presence."
        )
    )
    parser.add_argument(
        "repository_root",
        help="Path to generated static repository root.",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    repository_root = os.path.abspath(args.repository_root)
    errors = []

    manifest_path = os.path.join(
        repository_root, "habitv-update-manifest.properties"
    )
    entries = parse_manifest(manifest_path, errors)
    validate_manifest_entries(repository_root, entries, errors)

    if errors:
        for error in errors:
            print("ERROR:", error, file=sys.stderr)
        return 1

    print(
        "Manifest freshness validation passed: {} entries checked in {}".format(
            len(entries), manifest_path
        )
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())

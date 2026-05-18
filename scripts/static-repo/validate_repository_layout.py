#!/usr/bin/env python3
"""Validate static repository layout expected by Habitv update discovery."""

from __future__ import print_function

import argparse
import os
import re
import sys


HREF_PATTERN = re.compile(
    r"""<a\s+[^>]*href\s*=\s*["']([^"']+)["']""",
    re.IGNORECASE,
)


def fail(errors, message):
    errors.append(message)


def read_file(path):
    with open(path, "r", encoding="utf-8") as handle:
        return handle.read()


def get_index_hrefs(directory, errors):
    index_path = os.path.join(directory, "index.html")
    if not os.path.isfile(index_path):
        fail(errors, "Missing index.html in {}".format(directory))
        return set()
    html = read_file(index_path)
    hrefs = set(HREF_PATTERN.findall(html))
    if not hrefs:
        fail(
            errors,
            "index.html in {} has no <a href> entries".format(directory),
        )
    return hrefs


def require_href(directory, hrefs, expected, errors):
    if expected not in hrefs:
        fail(
            errors,
            "index.html in {} does not contain href '{}'".format(directory, expected),
        )


def require_file(path, errors):
    if not os.path.isfile(path):
        fail(errors, "Missing file {}".format(path))
        return False
    return True


def require_dir(path, errors):
    if not os.path.isdir(path):
        fail(errors, "Missing directory {}".format(path))
        return False
    return True


def load_plugin_ids(repository_root, errors):
    plugins_path = os.path.join(repository_root, "plugins.txt")
    if not require_file(plugins_path, errors):
        return set()
    content = read_file(plugins_path)
    lines = [line.strip() for line in content.splitlines()]
    plugin_ids = [line for line in lines if line and not line.startswith("#")]
    if not plugin_ids:
        fail(errors, "plugins.txt is empty (no plugin artifact ids)")
    return set(plugin_ids)


def validate_group_layout(repository_root, plugin_ids, errors):
    com_dir = os.path.join(repository_root, "com")
    dabi_dir = os.path.join(com_dir, "dabi")
    habitv_dir = os.path.join(dabi_dir, "habitv")
    if not (
        require_dir(com_dir, errors)
        and require_dir(dabi_dir, errors)
        and require_dir(habitv_dir, errors)
    ):
        return

    root_hrefs = get_index_hrefs(repository_root, errors)
    com_hrefs = get_index_hrefs(com_dir, errors)
    dabi_hrefs = get_index_hrefs(dabi_dir, errors)
    habitv_hrefs = get_index_hrefs(habitv_dir, errors)

    require_href(repository_root, root_hrefs, "com/", errors)
    require_href(com_dir, com_hrefs, "dabi/", errors)
    require_href(dabi_dir, dabi_hrefs, "habitv/", errors)

    artifact_dirs = sorted(
        name
        for name in os.listdir(habitv_dir)
        if os.path.isdir(os.path.join(habitv_dir, name))
    )
    if not artifact_dirs:
        fail(errors, "No artifacts found under {}".format(habitv_dir))
        return

    for artifact in artifact_dirs:
        require_href(habitv_dir, habitv_hrefs, artifact + "/", errors)
        artifact_dir = os.path.join(habitv_dir, artifact)
        artifact_hrefs = get_index_hrefs(artifact_dir, errors)

        version_dirs = sorted(
            name
            for name in os.listdir(artifact_dir)
            if os.path.isdir(os.path.join(artifact_dir, name))
        )
        if not version_dirs:
            fail(errors, "No version directories in {}".format(artifact_dir))
            continue

        for version in version_dirs:
            require_href(artifact_dir, artifact_hrefs, version + "/", errors)
            version_dir = os.path.join(artifact_dir, version)
            version_hrefs = get_index_hrefs(version_dir, errors)
            payload_files = sorted(
                name
                for name in os.listdir(version_dir)
                if os.path.isfile(os.path.join(version_dir, name))
                and (name.endswith(".jar") or name.endswith(".zip"))
            )
            if artifact in plugin_ids:
                if not payload_files:
                    fail(
                        errors,
                        "No .jar/.zip payload for plugin in {}".format(version_dir),
                    )
                    continue
                for payload_file in payload_files:
                    require_href(version_dir, version_hrefs, payload_file, errors)


def validate_tools_layout(repository_root, errors):
    tools_dir = os.path.join(repository_root, "tools")
    if not os.path.isdir(tools_dir):
        return

    root_hrefs = get_index_hrefs(repository_root, errors)
    tools_hrefs = get_index_hrefs(tools_dir, errors)
    require_href(repository_root, root_hrefs, "tools/", errors)

    tool_names = sorted(
        name
        for name in os.listdir(tools_dir)
        if os.path.isdir(os.path.join(tools_dir, name))
    )
    for tool_name in tool_names:
        require_href(tools_dir, tools_hrefs, tool_name + "/", errors)
        tool_dir = os.path.join(tools_dir, tool_name)
        tool_hrefs = get_index_hrefs(tool_dir, errors)
        version_dirs = sorted(
            name
            for name in os.listdir(tool_dir)
            if os.path.isdir(os.path.join(tool_dir, name))
        )
        for version in version_dirs:
            require_href(tool_dir, tool_hrefs, version + "/", errors)
            version_dir = os.path.join(tool_dir, version)
            version_hrefs = get_index_hrefs(version_dir, errors)
            zip_files = sorted(
                name
                for name in os.listdir(version_dir)
                if os.path.isfile(os.path.join(version_dir, name))
                and name.endswith(".zip")
            )
            if not zip_files:
                fail(
                    errors,
                    "No .zip files in tool version directory {}".format(
                        version_dir
                    ),
                )
                continue
            for zip_name in zip_files:
                require_href(version_dir, version_hrefs, zip_name, errors)


def parse_args():
    parser = argparse.ArgumentParser(
        description=(
            "Validate static repository layout expected by Habitv updater."
        )
    )
    parser.add_argument(
        "repository_root",
        help=(
            "Path to generated static repository root "
            "(contains plugins.txt and com/dabi/habitv)."
        ),
    )
    return parser.parse_args()


def main():
    args = parse_args()
    repository_root = os.path.abspath(args.repository_root)
    errors = []

    if not require_dir(repository_root, errors):
        for error in errors:
            print("ERROR:", error, file=sys.stderr)
        return 1

    plugin_ids = load_plugin_ids(repository_root, errors)
    validate_group_layout(repository_root, plugin_ids, errors)
    validate_tools_layout(repository_root, errors)

    if errors:
        for error in errors:
            print("ERROR:", error, file=sys.stderr)
        return 1

    print(
        "Static repository layout validation passed: {}".format(
            repository_root
        )
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())

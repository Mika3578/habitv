#!/usr/bin/env python3
"""Offline tests for validate_manifest_freshness.py."""

from __future__ import print_function

import os
import shutil
import subprocess
import sys
import tempfile
import unittest


SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
VALIDATOR = os.path.join(SCRIPT_DIR, "validate_manifest_freshness.py")

sys.path.insert(0, SCRIPT_DIR)
from validate_manifest_freshness import (  # noqa: E402
    build_sort_key,
    newest_jar_in_directory,
    parse_manifest_line,
)


class ValidateManifestFreshnessTest(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp(prefix="habitv-manifest-test-")

    def tearDown(self):
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def write_manifest(self, lines):
        manifest_path = os.path.join(
            self.temp_dir, "habitv-update-manifest.properties"
        )
        with open(manifest_path, "w", encoding="utf-8") as handle:
            handle.write("\n".join(lines) + "\n")

    def write_jar(self, relative_path):
        target_path = os.path.join(self.temp_dir, *relative_path.split("/"))
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        with open(target_path, "wb") as handle:
            handle.write(b"jar")

    def run_validator(self):
        completed = subprocess.run(
            [sys.executable, VALIDATOR, self.temp_dir],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        self.last_stderr = completed.stderr.decode("utf-8", errors="replace")
        return completed.returncode

    def test_accepts_newest_snapshot_jar(self):
        self.write_jar(
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample-4.1.0-20260519.221134-1.jar"
        )
        self.write_jar(
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample-4.1.0-20260530.144853-2.jar"
        )
        self.write_manifest(
            [
                "# comment",
                "plugin|com.dabi.habitv|sample|4.1.0-SNAPSHOT|jar|"
                "com/dabi/habitv/sample/4.1.0-SNAPSHOT/"
                "sample-4.1.0-20260530.144853-2.jar",
            ]
        )
        self.assertEqual(0, self.run_validator())

    def test_accepts_manifest_with_optional_checksum(self):
        self.write_jar(
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample-4.1.0-20260530.144853-2.jar"
        )
        self.write_manifest(
            [
                "plugin|com.dabi.habitv|sample|4.1.0-SNAPSHOT|jar|"
                "com/dabi/habitv/sample/4.1.0-SNAPSHOT/"
                "sample-4.1.0-20260530.144853-2.jar|deadbeef",
            ]
        )
        self.assertEqual(0, self.run_validator())

    def test_parse_manifest_line_keeps_checksum_separate(self):
        entry = parse_manifest_line(
            "plugin|com.dabi.habitv|sample|4.1.0-SNAPSHOT|jar|"
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample.jar|abc123"
        )
        self.assertIsNotNone(entry)
        self.assertEqual(
            entry["relativeUrl"],
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample.jar",
        )
        self.assertEqual(entry["checksum"], "abc123")

    def test_rejects_stale_snapshot_jar(self):
        self.write_jar(
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample-4.1.0-20260519.221134-1.jar"
        )
        self.write_jar(
            "com/dabi/habitv/sample/4.1.0-SNAPSHOT/sample-4.1.0-20260530.144853-2.jar"
        )
        self.write_manifest(
            [
                "plugin|com.dabi.habitv|sample|4.1.0-SNAPSHOT|jar|"
                "com/dabi/habitv/sample/4.1.0-SNAPSHOT/"
                "sample-4.1.0-20260519.221134-1.jar",
            ]
        )
        self.assertNotEqual(0, self.run_validator())

    def test_rejects_missing_manifest_target(self):
        self.write_manifest(
            [
                "plugin|com.dabi.habitv|sample|4.1.0-SNAPSHOT|jar|"
                "com/dabi/habitv/sample/4.1.0-SNAPSHOT/missing.jar",
            ]
        )
        self.assertNotEqual(0, self.run_validator())
        self.assertIn("missing file", self.last_stderr)

    def test_rejects_missing_manifest_file(self):
        self.assertNotEqual(0, self.run_validator())
        self.assertIn("Missing manifest", self.last_stderr)

    def test_non_timestamped_snapshot_jar_sorts_below_timestamped(self):
        version_dir = os.path.join(
            self.temp_dir, "com", "dabi", "habitv", "sample", "4.1.0-SNAPSHOT"
        )
        os.makedirs(version_dir)
        plain = "sample-4.1.0-SNAPSHOT.jar"
        timestamped = "sample-4.1.0-20260530.144853-2.jar"
        for name in (plain, timestamped):
            with open(os.path.join(version_dir, name), "wb") as handle:
                handle.write(b"jar")

        self.assertLess(build_sort_key(plain), build_sort_key(timestamped))
        self.assertEqual(timestamped, newest_jar_in_directory(version_dir))

    def test_timestamp_tie_breaks_deterministically_on_filename(self):
        version_dir = os.path.join(
            self.temp_dir, "com", "dabi", "habitv", "sample", "4.1.0-SNAPSHOT"
        )
        os.makedirs(version_dir)
        plain = "sample-4.1.0-20260530.144853-2.jar"
        all_jar = "sample-4.1.0-20260530.144853-2-all.jar"
        for name in (plain, all_jar):
            with open(os.path.join(version_dir, name), "wb") as handle:
                handle.write(b"jar")

        self.assertGreater(build_sort_key(plain), build_sort_key(all_jar))
        self.assertEqual(plain, newest_jar_in_directory(version_dir))


if __name__ == "__main__":
    unittest.main()

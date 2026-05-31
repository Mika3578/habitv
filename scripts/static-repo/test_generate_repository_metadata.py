#!/usr/bin/env python3
"""Offline tests for generate_repository_metadata.py SNAPSHOT jar selection."""

from __future__ import print_function

import os
import shutil
import sys
import tempfile
import unittest


SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, SCRIPT_DIR)

from generate_repository_metadata import latest_jar  # noqa: E402
from validate_manifest_freshness import build_sort_key  # noqa: E402


class GenerateRepositoryMetadataSnapshotTest(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp(prefix="habitv-metadata-test-")

    def tearDown(self):
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def write_jar(self, filename):
        path = os.path.join(self.temp_dir, filename)
        with open(path, "wb") as handle:
            handle.write(b"jar")

    def test_build_10_sorts_newer_than_build_9(self):
        build_9 = "sample-4.1.0-20260530.144853-9.jar"
        build_10 = "sample-4.1.0-20260530.144853-10.jar"
        self.assertLess(build_sort_key(build_9), build_sort_key(build_10))

        for name in (build_9, build_10):
            self.write_jar(name)
        self.assertEqual(build_10, latest_jar(self.temp_dir))

    def test_timestamped_outranks_plain_snapshot_jar(self):
        plain = "sample-4.1.0-SNAPSHOT.jar"
        timestamped = "sample-4.1.0-20260530.144853-2.jar"
        for name in (plain, timestamped):
            self.write_jar(name)
        self.assertEqual(timestamped, latest_jar(self.temp_dir))

    def test_plain_jar_outranks_all_jar_for_same_build(self):
        plain = "sample-4.1.0-20260530.144853-2.jar"
        all_jar = "sample-4.1.0-20260530.144853-2-all.jar"
        for name in (plain, all_jar):
            self.write_jar(name)
        self.assertEqual(plain, latest_jar(self.temp_dir))

    def test_ignores_sources_and_javadoc_jars(self):
        main_jar = "sample-4.1.0-20260530.144853-2.jar"
        for name in (
            main_jar,
            "sample-4.1.0-20260530.144853-2-sources.jar",
            "sample-4.1.0-20260530.144853-2-javadoc.jar",
        ):
            self.write_jar(name)
        self.assertEqual(main_jar, latest_jar(self.temp_dir))


if __name__ == "__main__":
    unittest.main()

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Local NOVO19 protected-replay helper for Habitv (personal tooling)."""
from __future__ import print_function

import argparse
import base64
import os
import re
import subprocess
import sys
import uuid

try:
    import requests
except ImportError:
    sys.stderr.write("Python requests module is required\n")
    sys.exit(1)

REDBEE = (
    "https://exposure.api.redbee.live/v2/customer/OuestFrance/businessunit/novoplus"
)
WV_UUID = "edef8ba979d64acea3c827dcd51d21ed"
UA = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
)


def fail(message):
    sys.stderr.write(message + "\n")
    sys.exit(1)


def report_download_progress(percent):
    value = max(0.0, min(100.0, float(percent)))
    sys.stdout.write("[download] %.1f%%\n" % value)
    sys.stdout.flush()


def parse_percent_from_line(line):
    match = re.search(r"(\d+(?:\.\d+)?)\s*%", line)
    if not match:
        return None
    try:
        return float(match.group(1))
    except ValueError:
        return None


def run_command_with_progress(cmd, progress_floor, progress_ceiling):
    process = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        universal_newlines=True,
        bufsize=1,
    )
    last_reported = progress_floor
    if progress_floor > 0:
        report_download_progress(progress_floor)
    output_chunks = []
    if process.stdout is not None:
        for line in process.stdout:
            if line is None:
                continue
            output_chunks.append(line)
            percent = parse_percent_from_line(line)
            if percent is not None:
                scaled = progress_floor + (percent / 100.0) * (
                    progress_ceiling - progress_floor
                )
                if scaled >= last_reported + 0.5 or scaled >= progress_ceiling - 0.5:
                    report_download_progress(scaled)
                    last_reported = scaled
    completed = process.wait()
    output = "".join(output_chunks)
    if completed != 0:
        fail("External command failed:\n" + output)
    return output


def resolve_device_path():
    for key in (
        "NOVO19_DEVICE_PATH",
        "WVD_PATH",
        "TF1_DEVICE_PATH",
        "habitv.novo19.devicePath",
    ):
        value = os.environ.get(key, "").strip()
        if value and os.path.isfile(value):
            return value
    fail(
        "Local device file is required "
        "(set NOVO19_DEVICE_PATH, WVD_PATH, or TF1_DEVICE_PATH)"
    )


def kid_hex(value):
    hex_attr = getattr(value, "hex", None)
    if callable(hex_attr):
        return value.hex()
    if isinstance(hex_attr, str):
        return hex_attr
    return str(value)


def authenticate(session):
    device_id = str(uuid.uuid4())
    response = session.post(
        REDBEE + "/auth/anonymous",
        json={
            "deviceId": device_id,
            "device": {"deviceId": device_id, "name": "WEB", "type": "WEB"},
        },
        timeout=60,
    )
    response.raise_for_status()
    token = response.json().get("sessionToken")
    if not token:
        fail("Missing Redbee session token")
    return token


def fetch_widevine_dash(session, token, asset_id):
    response = session.get(
        REDBEE + "/entitlement/%s/play" % asset_id,
        headers={"Authorization": "Bearer " + token},
        timeout=60,
    )
    response.raise_for_status()
    for fmt in response.json().get("formats") or []:
        drm = fmt.get("drm") or {}
        if fmt.get("format") == "DASH" and "com.widevine.alpha" in drm:
            return fmt
    fail("No protected DASH format available for this asset")


def extract_pssh(mpd_text):
    block = re.search(
        r'<ContentProtection[^>]*schemeIdUri=["\']urn:uuid:'
        + WV_UUID
        + r'["\'][^>]*>([\s\S]*?)</ContentProtection>',
        mpd_text,
        re.I,
    )
    if block:
        match = re.search(
            r"<(?:cenc:)?pssh[^>]*>([A-Za-z0-9+/=]+)</(?:cenc:)?pssh>",
            block.group(0),
            re.I,
        )
        if match:
            return match.group(1)
    for match in re.finditer(
        r"<(?:cenc:)?pssh[^>]*>([A-Za-z0-9+/=]+)</(?:cenc:)?pssh>",
        mpd_text,
        re.I,
    ):
        try:
            if WV_UUID in base64.b64decode(match.group(1)).hex():
                return match.group(1)
        except Exception:
            pass
    fail("Widevine init data not found in manifest")


def extract_keys(device_path, pssh_b64, license_url, token):
    try:
        from pywidevine.cdm import Cdm
        from pywidevine.device import Device
        from pywidevine.pssh import PSSH
    except ImportError:
        fail("pywidevine is required (pip install pywidevine)")

    device = Device.load(device_path)
    cdm = Cdm.from_device(device)
    session_id = cdm.open()
    try:
        challenge = cdm.get_license_challenge(session_id, PSSH(pssh_b64))
        headers = {
            "User-Agent": UA,
            "Content-Type": "application/octet-stream",
            "Authorization": "Bearer " + token,
        }
        response = requests.post(license_url, data=challenge, headers=headers, timeout=60)
        if response.status_code != 200:
            response = requests.post(
                license_url,
                data=challenge,
                headers={
                    "User-Agent": UA,
                    "Content-Type": "application/octet-stream",
                },
                timeout=60,
            )
        if response.status_code != 200:
            fail("License request failed with HTTP %s" % response.status_code)
        cdm.parse_license(session_id, response.content)
        keys = []
        for entry in cdm.get_keys(session_id):
            if entry.type == "CONTENT":
                keys.append({"kid": kid_hex(entry.kid), "key": kid_hex(entry.key)})
        if not keys:
            fail("No content keys returned")
        return keys
    finally:
        cdm.close(session_id)


def save_name_from_output(output_path):
    """Habitv writes to *.ext.tmp; N_m3u8DL-RE needs a bare save name."""
    name = os.path.basename(output_path)
    if name.lower().endswith(".tmp"):
        name = name[:-4]
    root, ext = os.path.splitext(name)
    if ext:
        return root
    return name


def download_with_n_m3u8dl(re_binary, mpd_url, keys, output_path, ffmpeg, mp4decrypt):
    output_path = os.path.abspath(output_path)
    output_dir = os.path.dirname(output_path)
    output_name = save_name_from_output(output_path)
    if output_dir and not os.path.isdir(output_dir):
        os.makedirs(output_dir)
    tmp_dir = os.environ.get("NOVO19_HELPER_TMP", "").strip()
    if not tmp_dir:
        tmp_dir = os.path.join(os.path.expanduser("~"), "habitv", "tmp", "novo19-helper")
    if not os.path.isdir(tmp_dir):
        os.makedirs(tmp_dir)
    cmd = [
        re_binary,
        mpd_url,
        "--auto-select",
        "-M",
        "format=mp4",
        "--save-dir",
        output_dir,
        "--save-name",
        output_name,
        "--tmp-dir",
        tmp_dir,
        "--thread-count",
        "8",
        "--no-log",
    ]
    if ffmpeg:
        cmd.extend(["--ffmpeg-binary-path", ffmpeg])
    if mp4decrypt and os.path.isfile(mp4decrypt):
        cmd.extend(["--decryption-binary-path", mp4decrypt])
    for entry in keys:
        cmd.extend(["--key", entry["kid"] + ":" + entry["key"]])
    run_command_with_progress(cmd, 30.0, 99.0)
    if os.path.isfile(output_path) and os.path.getsize(output_path) > 0:
        return
    candidates = [
        os.path.join(output_dir, output_name + ".mp4"),
        os.path.join(output_dir, output_name + ".MP4"),
        # leftover from previous *.mp4.tmp mishandling
        os.path.join(output_dir, output_name + ".mp4.mp4"),
    ]
    for candidate in candidates:
        if os.path.isfile(candidate) and os.path.getsize(candidate) > 0:
            if candidate != output_path:
                if os.path.isfile(output_path):
                    os.remove(output_path)
                os.replace(candidate, output_path)
            return
    fail("Downloader finished without creating " + output_path)


def parse_args():
    parser = argparse.ArgumentParser(description="Download protected NOVO19 replay")
    parser.add_argument("--asset-id", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--ffmpeg", default=os.environ.get("FFMPEG", "ffmpeg"))
    parser.add_argument(
        "--n-m3u8dl-re",
        default=os.environ.get("N_M3U8DL_RE", "").strip(),
    )
    parser.add_argument(
        "--mp4decrypt",
        default=os.environ.get("MP4DECRYPT", "").strip(),
    )
    return parser.parse_args()


def main():
    try:
        import warnings

        warnings.filterwarnings("ignore", message="urllib3 .* doesn't match a supported version")
    except Exception:
        pass
    args = parse_args()
    if not args.n_m3u8dl_re:
        fail("Configure N_M3U8DL_RE for NOVO19 helper download")
    if not os.path.isfile(args.n_m3u8dl_re):
        fail("N_m3u8DL-RE binary not found")

    device_path = resolve_device_path()
    mp4decrypt = args.mp4decrypt
    if not mp4decrypt:
        sibling = os.path.join(os.path.dirname(args.n_m3u8dl_re), "mp4decrypt.exe")
        if os.path.isfile(sibling):
            mp4decrypt = sibling

    session = requests.Session()
    session.headers.update({"User-Agent": UA})

    report_download_progress(1.0)
    token = authenticate(session)
    report_download_progress(10.0)
    dash = fetch_widevine_dash(session, token, args.asset_id)
    mpd_url = dash["mediaLocator"]
    license_url = dash["drm"]["com.widevine.alpha"]["licenseServerUrl"]
    report_download_progress(15.0)
    mpd_text = session.get(mpd_url, timeout=60).text
    pssh = extract_pssh(mpd_text)
    report_download_progress(20.0)
    keys = extract_keys(device_path, pssh, license_url, token)
    report_download_progress(30.0)
    download_with_n_m3u8dl(
        args.n_m3u8dl_re,
        mpd_url,
        keys,
        args.output,
        args.ffmpeg,
        mp4decrypt,
    )
    report_download_progress(100.0)
    print("NOVO19 helper download completed: " + args.output)


if __name__ == "__main__":
    main()

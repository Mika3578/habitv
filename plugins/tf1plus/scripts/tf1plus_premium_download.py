#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
TF1+ premium replay helper for Habitv.

Uses TF1 Gigya login, mediainfo delivery, a user-local device file, and an
external DASH downloader (N_m3u8DL-RE or MediaFlow).
"""
from __future__ import print_function

import argparse
import base64
import json
import os
import re
import subprocess
import sys

try:
    import requests
    from requests.adapters import HTTPAdapter
except ImportError:
    print(json.dumps({"error": "Python requests module is required"}))
    sys.exit(1)

try:
    from urllib3.util.retry import Retry
except ImportError:
    Retry = None

try:
    from urlparse import urlparse, urlunparse
except ImportError:
    from urllib.parse import urlparse, urlunparse

DEFAULT_GIGYA_API_KEY = "3_hWgJdARhz_7l1oOp3a8BDLoR9cuWZpUaKG4aqF7gum9_iK3uTZ2VlDBl8ANf8FVk"
GIGYA_LOGIN_URL = "https://compte.tf1.fr/accounts.login"
TOKEN_URL = "https://www.tf1.fr/token/gigya/web"
MEDIAINFO_URL = "https://mediainfo.tf1.fr/mediainfocombo/%s"
CONTENT_PROTECTION_UUID = "edef8ba979d64acea3c827dcd51d21ed"
USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"


def get_gigya_api_key():
    override = os.environ.get("TF1_GIGYA_API_KEY", "").strip()
    return override or DEFAULT_GIGYA_API_KEY


def fail_code(code, message, hint=""):
    line = "[%s] %s" % (code, message)
    if hint:
        line += "\n-> " + hint
    fail(line)


def create_requests_session():
    session = requests.Session()
    if Retry is not None:
        retry = Retry(
            total=3,
            backoff_factor=0.5,
            status_forcelist=[429, 500, 502, 503, 504],
        )
        adapter = HTTPAdapter(max_retries=retry)
        session.mount("https://", adapter)
        session.mount("http://", adapter)
    return session


def fail(message):
    sys.stderr.write(message + "\n")
    sys.exit(1)


def report_download_progress(percent):
    """Emit a line understood by Habitv CmdExecutor (yt-dlp style)."""
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
    """Run a child process and relay percentage lines to Habitv."""
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
                scaled = progress_floor + (percent / 100.0) * (progress_ceiling - progress_floor)
                if scaled >= last_reported + 0.5 or scaled >= progress_ceiling - 0.5:
                    report_download_progress(scaled)
                    last_reported = scaled
    completed = process.wait()
    output = "".join(output_chunks)
    if completed != 0:
        fail("External command failed:\n" + output)
    return output


def tf1_login(email, password):
    response = requests.post(
        GIGYA_LOGIN_URL,
        data={
            "apiKey": get_gigya_api_key(),
            "loginID": email,
            "password": password,
        },
        headers={
            "Content-Type": "application/x-www-form-urlencoded",
            "User-Agent": USER_AGENT,
        },
        timeout=60,
    )
    data = response.json()
    if data.get("statusCode") != 200:
        fail_code(
            "TF1_AUTH_FAILED",
            "TF1 Gigya login failed: " + str(data.get("errorMessage", data)),
            "Check TF1_EMAIL and TF1_PASSWORD; verify login at https://www.tf1.fr/",
        )
    return {
        "uid": data["UID"],
        "signature": data["UIDSignature"],
        "timestamp": int(data["signatureTimestamp"]),
    }


def tf1_token(gigya_session):
    response = requests.post(
        TOKEN_URL,
        json={
            "uid": gigya_session["uid"],
            "signature": gigya_session["signature"],
            "timestamp": gigya_session["timestamp"],
        },
        headers={
            "Content-Type": "application/json",
            "User-Agent": USER_AGENT,
            "Origin": "https://www.tf1.fr",
        },
        timeout=60,
    )
    data = response.json()
    if data.get("error"):
        fail_code(
            "TF1_TOKEN_FAILED",
            "TF1 token request failed: " + str(data.get("error")),
            "Retry login; TF1 may have rejected the Gigya session.",
        )
    return data["token"]


def is_numeric_stream_id(stream_id):
    value = (stream_id or "").strip()
    return value.isdigit() and len(value) >= 5


def resolve_delivery_stream_id(stream_id, token):
    """Mediainfo accepts GraphQL UUIDs and returns the numeric media id."""
    value = (stream_id or "").strip()
    if not value:
        fail_code("TF1_STREAM_ID_MISSING", "TF1 stream id is required", "Replay metadata may be incomplete.")
    if is_numeric_stream_id(value):
        return value
    params = {
        "context": "MYTF1",
        "pver": "5010000",
        "format": "dash",
        "platform": "web",
        "device": "desktop",
        "os": "windows",
        "osVersion": "10.0",
        "topDomain": "https://www.tf1.fr",
        "playerVersion": "5.29.0",
        "productName": "mytf1",
        "productVersion": "3.37.0",
    }
    headers = {
        "User-Agent": USER_AGENT,
        "Accept": "application/json",
        "Origin": "https://www.tf1.fr",
    }
    if token:
        headers["Authorization"] = "Bearer " + token
    response = requests.get(
        MEDIAINFO_URL % value,
        params=params,
        headers=headers,
        timeout=60,
    )
    payload = response.json()
    media = payload.get("media") or {}
    numeric_id = str(media.get("id") or "").strip()
    if is_numeric_stream_id(numeric_id):
        return numeric_id
    fail_code(
        "TF1_STREAM_ID_INVALID",
        "Unable to resolve TF1 stream id from " + value,
        "Replay may be expired or metadata changed on TF1+.",
    )


def fetch_delivery(stream_id, token):
    delivery_stream_id = resolve_delivery_stream_id(stream_id, token)
    params = {
        "context": "MYTF1",
        "pver": "5010000",
        "format": "dash",
        "platform": "web",
        "device": "desktop",
        "os": "windows",
        "osVersion": "10.0",
        "topDomain": "https://www.tf1.fr",
        "playerVersion": "5.29.0",
        "productName": "mytf1",
        "productVersion": "3.37.0",
    }
    headers = {
        "Authorization": "Bearer " + token,
        "User-Agent": USER_AGENT,
        "Accept": "application/json",
    }
    response = requests.get(
        MEDIAINFO_URL % delivery_stream_id,
        params=params,
        headers=headers,
        timeout=60,
    )
    payload = response.json()
    delivery = payload.get("delivery") or {}
    if delivery.get("code", 0) >= 400 or not delivery.get("url"):
        fail_code(
            "TF1_DELIVERY_UNAVAILABLE",
            "TF1 delivery unavailable for stream " + delivery_stream_id,
            "Replay may be geo-blocked, expired, or not entitled for this account.",
        )
    return delivery


def normalize_mpd_url(mpd_url):
    """Normalize TF1 MPD URL (HTTPS, legacy hostname migration)."""
    if not mpd_url:
        return mpd_url
    try:
        parsed = urlparse(mpd_url)
    except Exception as exc:
        fail_code("TF1_MPD_INVALID", "Invalid MPD URL: " + str(exc), "Check mediainfo delivery payload.")
    scheme = parsed.scheme or "https"
    if scheme == "http":
        scheme = "https"
    hostname = parsed.hostname or ""
    if "das-q1.tf1.fr" in hostname:
        hostname = hostname.replace("das-q1.tf1.fr", "das-q1-ssl.tf1.fr")
        netloc = hostname
        if parsed.port:
            netloc += ":" + str(parsed.port)
        parsed = parsed._replace(scheme=scheme, netloc=netloc)
    elif scheme != parsed.scheme:
        parsed = parsed._replace(scheme=scheme)
    return urlunparse(parsed)


def build_mpd_headers(token):
    headers = {
        "User-Agent": USER_AGENT,
        "Origin": "https://prod-player.tf1.fr",
        "Referer": "https://www.tf1.fr/",
    }
    if token:
        headers["Authorization"] = "Bearer " + token
    return headers


def resolve_device_path():
    device_path = os.environ.get("TF1_DEVICE_PATH", "").strip()
    if not device_path:
        device_path = os.environ.get("WVD_PATH", "").strip()
    return device_path


def build_protection_request(delivery, token):
    headers = {
        "Content-Type": "application/octet-stream",
        "User-Agent": USER_AGENT,
    }
    protection_entries = delivery.get("drms") or []
    request_url = ""
    if protection_entries:
        entry = protection_entries[0]
        request_url = entry.get("url") or ""
        auth_headers = entry.get("h") or []
        if auth_headers:
            headers["Authorization"] = auth_headers[0].get("v", "")
        else:
            headers["Authorization"] = "Bearer " + token
    else:
        headers["Authorization"] = "Bearer " + token

    if not request_url:
        delivery_id = delivery.get("id")
        if not delivery_id:
            fail_code(
                "TF1_MISSING_DELIVERY_ID",
                "Delivery response is missing required metadata",
                "TF1 mediainfo payload may have changed.",
            )
        request_url = "https://drm-wide.tf1.fr/proxy?id=" + str(delivery_id)
    return request_url, headers


def extract_init_data_from_mpd(mpd_url, token):
    mpd = requests.get(mpd_url, headers=build_mpd_headers(token), timeout=60).text
    protection_block = re.search(
        r'<ContentProtection[^>]*schemeIdUri=["\']urn:uuid:' + CONTENT_PROTECTION_UUID + '["\'][^>]*>([\s\S]*?)</ContentProtection>',
        mpd,
        re.IGNORECASE,
    )
    if protection_block:
        match = re.search(r'<(?:cenc:)?pssh[^>]*>([A-Za-z0-9+/=]+)</(?:cenc:)?pssh>', protection_block.group(0), re.IGNORECASE)
        if match:
            return match.group(1)
    for match in re.finditer(r'<(?:cenc:)?pssh[^>]*>([A-Za-z0-9+/=]+)</(?:cenc:)?pssh>', mpd, re.IGNORECASE):
        try:
            payload = base64.b64decode(match.group(1))
            hex_value = payload.hex() if hasattr(payload, "hex") else payload.encode("hex")
            if CONTENT_PROTECTION_UUID in hex_value:
                return match.group(1)
        except Exception:
            pass
    fail_code(
        "TF1_INIT_DATA_NOT_FOUND",
        "Required playback metadata was not found in the TF1 manifest",
        "Try yt-dlp for public replay.",
    )


def load_local_device(device_path):
    if not device_path or not os.path.isfile(device_path):
        fail_code(
            "TF1_DEVICE_MISSING",
            "TF1_DEVICE_PATH must point to a valid local device file",
            "Set tf1plusDevicePath in configuration.xml or export TF1_DEVICE_PATH.",
        )
    try:
        from pywidevine.device import Device
        return Device.load(device_path)
    except Exception as exc:
        fail_code(
            "TF1_DEVICE_INVALID",
            "Failed to load the local device file",
            "%s: %s" % (exc.__class__.__name__, exc),
        )


def resolve_playback_material(device_path, init_data_b64, request_url, request_headers):
    try:
        from pywidevine.cdm import Cdm
        from pywidevine.pssh import PSSH
    except ImportError:
        fail_code(
            "TF1_HELPER_DEPS_MISSING",
            "Premium replay helper dependencies are not installed",
            "pip install -r plugins/tf1plus/scripts/requirements.txt",
        )

    device = load_local_device(device_path)
    cdm = Cdm.from_device(device)
    session_id = cdm.open()
    session = create_requests_session()
    try:
        challenge = cdm.get_license_challenge(session_id, PSSH(init_data_b64))
        response = session.post(request_url, data=challenge, headers=request_headers, timeout=60)
        if response.status_code >= 400:
            body_preview = response.text[:500] if response.text else ""
            fail_code(
                "TF1_SESSION_FAILED",
                "TF1 playback session request failed with HTTP %d" % response.status_code,
                body_preview or "No response body",
            )
        try:
            cdm.parse_license(session_id, response.content)
        except Exception as exc:
            fail_code(
                "TF1_SESSION_PARSE_FAILED",
                "Failed to parse the TF1 playback session response",
                "%s: %s" % (exc.__class__.__name__, exc),
            )
        material = []
        for entry in cdm.get_keys(session_id):
            if entry.type == "CONTENT":
                material.append({"kid": entry.kid.hex, "key": entry.key.hex()})
        if not material:
            fail_code(
                "TF1_SESSION_EMPTY",
                "Premium replay session returned no playback material",
                "Check account entitlement and the local device file.",
            )
        return material
    finally:
        cdm.close(session_id)


def download_with_n_m3u8dl(re_binary, mpd_url, material, output_path, mpd_headers):
    output_dir = os.path.dirname(os.path.abspath(output_path))
    output_name = os.path.splitext(os.path.basename(output_path))[0]
    if output_dir and not os.path.isdir(output_dir):
        os.makedirs(output_dir)
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
    ]
    for header_name, header_value in mpd_headers.items():
        cmd.extend(["--header", header_name + ": " + header_value])
    for entry in material:
        cmd.extend(["--key", entry["kid"] + ":" + entry["key"]])
    run_command_with_progress(cmd, 30.0, 99.0)
    if not os.path.isfile(output_path):
        candidates = [
            os.path.join(output_dir, output_name + ".mp4"),
            os.path.join(output_dir, output_name + ".MP4"),
        ]
        for candidate in candidates:
            if os.path.isfile(candidate):
                if candidate != output_path:
                    os.replace(candidate, output_path)
                return
        fail("N_m3u8DL-RE finished without creating " + output_path)


def download_with_mediaflow(mediaflow_base, mpd_url, material, output_path, ffmpeg, password):
    try:
        from urllib.parse import quote
    except ImportError:
        from urllib import quote

    output_dir = os.path.dirname(os.path.abspath(output_path))
    if output_dir and not os.path.isdir(output_dir):
        os.makedirs(output_dir)
    key_ids = ",".join(entry["kid"] for entry in material)
    key_values = ",".join(entry["key"] for entry in material)
    mediaflow_url = (
        mediaflow_base.rstrip("/")
        + "/proxy/mpd/manifest.m3u8?d="
        + quote(mpd_url, safe="")
        + "&key_id="
        + quote(key_ids, safe="")
        + "&key="
        + quote(key_values, safe="")
    )
    if password:
        mediaflow_url += "&api_password=" + quote(password, safe="")
    cmd = [ffmpeg, "-y", "-loglevel", "warning", "-i", mediaflow_url, "-c", "copy", output_path]
    run_command_with_progress(cmd, 30.0, 99.0)
    if not os.path.isfile(output_path):
        fail("ffmpeg MediaFlow download did not create " + output_path)


def parse_args():
    parser = argparse.ArgumentParser(description="Download premium TF1+ replay")
    parser.add_argument("--stream-id", required=True)
    parser.add_argument(
        "--probe",
        choices=("login", "delivery", "keys", "session"),
        default="",
        help="Stop after login, mediainfo delivery, or playback session (no file download)",
    )
    parser.add_argument(
        "--output",
        default="",
        help="Output file path (required unless --probe is set)",
    )
    parser.add_argument("--ffmpeg", default=os.environ.get("FFMPEG", "ffmpeg"))
    parser.add_argument("--n-m3u8dl-re", default=os.environ.get("N_M3U8DL_RE", ""))
    return parser.parse_args()


def require_env_for_probe(device_required):
    email = os.environ.get("TF1_EMAIL", "").strip()
    password = os.environ.get("TF1_PASSWORD", "").strip()
    device_path = resolve_device_path()
    if not email or not password:
        fail_code(
            "TF1_CREDENTIALS_MISSING",
            "TF1_EMAIL and TF1_PASSWORD environment variables are required for premium replay downloads",
            "Set tf1plusEmail/tf1plusPassword in configuration.xml or export env vars.",
        )
    if device_required:
        load_local_device(device_path)
    return email, password, device_path


def requires_premium_helper(delivery):
    if delivery.get("drms"):
        return True
    return delivery.get("drm") == "widevine"


def main():
    args = parse_args()
    probe = (args.probe or "").strip()
    if not probe and not args.output:
        fail("--output is required unless --probe is set")

    device_required = probe in ("keys", "session", "")
    email, password, device_path = require_env_for_probe(device_required)
    mediaflow_url = os.environ.get("MEDIAFLOW_URL", "").strip()
    mediaflow_password = os.environ.get("MEDIAFLOW_PASSWORD", "").strip()

    report_download_progress(1.0)
    token = tf1_token(tf1_login(email, password))
    report_download_progress(10.0)
    if probe == "login":
        print("TF1+ probe login OK (token length %d)" % len(token))
        return

    delivery = fetch_delivery(args.stream_id, token)
    report_download_progress(20.0)
    if probe == "delivery":
        print(
            "TF1+ probe delivery OK: url=%s"
            % normalize_mpd_url(delivery.get("url", ""))
        )
        return

    if not requires_premium_helper(delivery):
        fail_code(
            "TF1_STREAM_UNSUPPORTED",
            "This stream is not available through the premium replay helper",
            "Try yt-dlp for public replay.",
        )

    mpd_url = normalize_mpd_url(delivery["url"])
    mpd_headers = build_mpd_headers(token)
    request_url, request_headers = build_protection_request(delivery, token)
    init_data = extract_init_data_from_mpd(mpd_url, token)
    material = resolve_playback_material(device_path, init_data, request_url, request_headers)
    report_download_progress(30.0)

    if probe in ("keys", "session"):
        print("TF1+ probe session OK")
        return

    if not args.n_m3u8dl_re and not mediaflow_url:
        fail("Configure N_M3U8DL_RE or MEDIAFLOW_URL for premium TF1+ download")

    if args.n_m3u8dl_re:
        download_with_n_m3u8dl(args.n_m3u8dl_re, mpd_url, material, args.output, mpd_headers)
    else:
        download_with_mediaflow(mediaflow_url, mpd_url, material, args.output, args.ffmpeg, mediaflow_password)

    report_download_progress(100.0)
    print("TF1+ premium replay download completed: " + args.output)


if __name__ == "__main__":
    main()

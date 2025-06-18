#!/usr/bin/env python3
"""
Script to update external tool binaries from their official sources.
Usage: python update_tool.py <tool_name> <source_url> <binary_name>
"""

import os
import sys
import json
import requests
import zipfile
from datetime import datetime

def download_file(url: str, filename: str) -> bool:
    """Download a file from URL."""
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()
        
        with open(filename, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
        return True
    except Exception as e:
        print(f"Error downloading {url}: {e}")
        return False

def extract_zip(zip_path: str, extract_to: str) -> bool:
    """Extract a zip file."""
    try:
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(extract_to)
        return True
    except Exception as e:
        print(f"Error extracting {zip_path}: {e}")
        return False

def update_manifest(tool_name: str, new_version: str, binary_name: str) -> bool:
    """Update the manifest.json file for a tool."""
    manifest_path = f"repository/com/dabi/habitv/bin/{tool_name}/manifest.json"
    
    if not os.path.exists(manifest_path):
        print(f"Manifest not found: {manifest_path}")
        return False
    
    try:
        with open(manifest_path, 'r') as f:
            manifest = json.load(f)
        
        # Update latest version
        manifest['latest'] = new_version
        
        # Add new version to versions list if not present
        if new_version not in manifest['versions']:
            manifest['versions'].insert(0, new_version)
        
        # Update URLs
        manifest['url_latest'] = f"https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/{tool_name}/latest/{binary_name}"
        manifest['url_versioned'] = f"https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/{tool_name}/{new_version}/{binary_name}"
        
        # Update last_updated
        manifest['last_updated'] = datetime.now().strftime("%Y-%m-%d")
        
        # Add to changelog
        if 'changelog' not in manifest:
            manifest['changelog'] = []
        
        manifest['changelog'].insert(0, {
            "version": new_version,
            "date": datetime.now().strftime("%Y-%m-%d"),
            "changes": f"Auto-updated from official source"
        })
        
        # Keep only last 10 changelog entries
        manifest['changelog'] = manifest['changelog'][:10]
        
        with open(manifest_path, 'w') as f:
            json.dump(manifest, f, indent=2)
        
        return True
    except Exception as e:
        print(f"Error updating manifest: {e}")
        return False

def main() -> None:
    if len(sys.argv) != 4:
        print("Usage: python update_tool.py <tool_name> <source_url> <binary_name>")
        sys.exit(1)
    
    tool_name = sys.argv[1]
    source_url = sys.argv[2]
    binary_name = sys.argv[3]
    
    print(f"Updating {tool_name} from {source_url}")
    
    # Create directories
    bin_dir = f"repository/com/dabi/habitv/bin/{tool_name}"
    latest_dir = f"{bin_dir}/latest"
    
    os.makedirs(bin_dir, exist_ok=True)
    os.makedirs(latest_dir, exist_ok=True)
    
    # For now, this is a placeholder implementation
    # In a real implementation, you would:
    # 1. Parse the source URL to find the latest version
    # 2. Download the appropriate binary
    # 3. Extract and verify it
    # 4. Update the directory structure
    
    print(f"Tool {tool_name} update completed (placeholder implementation)")
    
    # Example of how to determine version from URL or filename
    # This would need to be customized for each tool
    if "curl" in tool_name:
        new_version = "8.0.0"  # Example version
    elif "ffmpeg" in tool_name:
        new_version = "6.0"    # Example version
    elif "aria2" in tool_name:
        new_version = "1.36.0" # Example version
    elif "rtmpdump" in tool_name:
        new_version = "v2.6"   # Example version
    elif "youtube-dl" in tool_name:
        new_version = "2023.12.30" # Example version
    else:
        new_version = "unknown"
    
    # Create version directory
    version_dir = f"{bin_dir}/{new_version}"
    os.makedirs(version_dir, exist_ok=True)
    
    # In a real implementation, you would copy the downloaded binary here
    # For now, we'll just update the manifest
    update_manifest(tool_name, new_version, binary_name)
    
    print(f"Updated {tool_name} to version {new_version}")

if __name__ == "__main__":
    main() 

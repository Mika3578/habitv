#!/usr/bin/env python3
"""
Script to verify that all URLs in manifest files are correctly formatted.
"""

import os
import json
import glob

def verify_manifest_urls():
    """Verify all manifest.json files have correct URLs."""
    bin_dir = "repository/com/dabi/habitv/bin"
    
    if not os.path.exists(bin_dir):
        print(f"Error: {bin_dir} directory not found")
        return False
    
    all_correct = True
    
    # Find all manifest.json files
    manifest_files = glob.glob(f"{bin_dir}/*/manifest.json")
    
    for manifest_file in manifest_files:
        tool_name = os.path.basename(os.path.dirname(manifest_file))
        print(f"\nChecking {tool_name}...")
        
        try:
            with open(manifest_file, 'r') as f:
                manifest = json.load(f)
            
            # Check required fields
            required_fields = ['latest', 'versions', 'url_latest', 'url_versioned']
            for field in required_fields:
                if field not in manifest:
                    print(f"  ❌ Missing required field: {field}")
                    all_correct = False
            
            # Check URL format
            base_url = "https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin"
            
            if 'url_latest' in manifest:
                expected_latest = f"{base_url}/{tool_name}/latest"
                if not manifest['url_latest'].startswith(expected_latest):
                    print(f"  ❌ Incorrect latest URL format: {manifest['url_latest']}")
                    all_correct = False
                else:
                    print(f"  ✅ Latest URL: {manifest['url_latest']}")
            
            if 'url_versioned' in manifest:
                expected_versioned = f"{base_url}/{tool_name}/{manifest.get('latest', 'unknown')}"
                if not manifest['url_versioned'].startswith(expected_versioned):
                    print(f"  ❌ Incorrect versioned URL format: {manifest['url_versioned']}")
                    all_correct = False
                else:
                    print(f"  ✅ Versioned URL: {manifest['url_versioned']}")
            
            # Check that latest version is in versions list
            if 'latest' in manifest and 'versions' in manifest:
                if manifest['latest'] not in manifest['versions']:
                    print(f"  ❌ Latest version {manifest['latest']} not in versions list")
                    all_correct = False
                else:
                    print(f"  ✅ Latest version {manifest['latest']} found in versions list")
            
        except Exception as e:
            print(f"  ❌ Error reading {manifest_file}: {e}")
            all_correct = False
    
    return all_correct

def verify_file_structure():
    """Verify that all referenced files exist."""
    bin_dir = "repository/com/dabi/habitv/bin"
    all_correct = True
    
    print("\nVerifying file structure...")
    
    # Find all manifest.json files
    manifest_files = glob.glob(f"{bin_dir}/*/manifest.json")
    
    for manifest_file in manifest_files:
        tool_name = os.path.basename(os.path.dirname(manifest_file))
        
        try:
            with open(manifest_file, 'r') as f:
                manifest = json.load(f)
            
            # Check latest directory exists
            latest_dir = f"{bin_dir}/{tool_name}/latest"
            if not os.path.exists(latest_dir):
                print(f"  ❌ Latest directory missing: {latest_dir}")
                all_correct = False
            
            # Check versioned directory exists
            if 'latest' in manifest:
                version_dir = f"{bin_dir}/{tool_name}/{manifest['latest']}"
                if not os.path.exists(version_dir):
                    print(f"  ❌ Version directory missing: {version_dir}")
                    all_correct = False
                else:
                    print(f"  ✅ Version directory exists: {version_dir}")
                
                # Check for additional files if specified in manifest
                if 'files' in manifest:
                    for file_name in manifest['files']:
                        file_path = f"{version_dir}/{file_name}"
                        if not os.path.exists(file_path):
                            print(f"  ❌ Additional file missing: {file_path}")
                            all_correct = False
                        else:
                            print(f"  ✅ Additional file exists: {file_path}")
            
        except Exception as e:
            print(f"  ❌ Error checking {manifest_file}: {e}")
            all_correct = False
    
    return all_correct

def main():
    print("Verifying habiTv BIN repository structure...")
    
    urls_correct = verify_manifest_urls()
    structure_correct = verify_file_structure()
    
    print("\n" + "="*50)
    if urls_correct and structure_correct:
        print("✅ All verifications passed!")
        print("The BIN repository is correctly configured.")
    else:
        print("❌ Some verifications failed!")
        print("Please check the errors above.")
    
    return urls_correct and structure_correct

if __name__ == "__main__":
    main() 
# Copilot Prompt: habiTv External Tool Binary Refactoring

## 🎯 **OBJECTIVE**
Refactor the entire habiTv codebase to use the new centralized external tool binary organization. All external tools are now located at `repository/com/dabi/habitv/bin/[tool]/[version]/[tool].exe` with a `latest` directory and `manifest.json` for each tool.

## 📋 **CURRENT STRUCTURE**
```
repository/com/dabi/habitv/bin/
├── [tool]/
│   ├── latest/
│   │   └── [tool].exe          # Symlink to latest version
│   ├── [version]/
│   │   └── [tool].exe          # Specific version binary
│   └── manifest.json           # Tool metadata and URLs
```

## 🔍 **SEARCH AND REFACTOR TASKS**

### 1. **Search for External Tool References**
Search the entire codebase for:
- **Java files**: Look for `.exe` references, binary paths, tool detection logic
- **Python files**: Check for hardcoded tool paths, download logic, version checking
- **Configuration files**: XML, JSON, properties files with tool paths
- **Documentation**: README, help files, config samples
- **Scripts**: Batch files, shell scripts, PowerShell scripts
- **Plugin code**: Any plugin-specific tool references

**Search patterns to find:**
```bash
# Direct binary references
ffmpeg.exe, curl.exe, aria2c.exe, youtube-dl.exe, rtmpdump.exe
ffmpeg, curl, aria2c, youtube-dl, rtmpdump

# Path patterns
bin/, tools/, external/, downloads/
temp/, cache/, downloads/

# Version checking
version, Version, VERSION
check.*version, get.*version, compare.*version

# Download logic
download, Download, DOWNLOAD
url, URL, Url
http://, https://
```

### 2. **Refactor Code to Use New Structure**

#### **A. Create Centralized Tool Manager**
Create a new utility class `ExternalToolManager` that:
- Reads `manifest.json` files from `repository/com/dabi/habitv/bin/[tool]/manifest.json`
- Provides methods for:
  - Getting latest version path: `getLatestPath(toolName)`
  - Getting specific version path: `getVersionPath(toolName, version)`
  - Checking if tool exists: `toolExists(toolName)`
  - Getting available versions: `getAvailableVersions(toolName)`
  - Getting tool metadata: `getToolMetadata(toolName)`

#### **B. Update All Tool References**
Replace all hardcoded paths with calls to the new manager:

**Before:**
```java
// Old hardcoded paths
String ffmpegPath = "tools/ffmpeg.exe";
String curlPath = "bin/curl/curl.exe";
String downloadUrl = "https://example.com/ffmpeg.exe";
```

**After:**
```java
// New centralized approach
String ffmpegPath = ExternalToolManager.getLatestPath("ffmpeg");
String curlPath = ExternalToolManager.getLatestPath("curl");
String downloadUrl = ExternalToolManager.getToolMetadata("ffmpeg").getLatestUrl();
```

#### **C. Update Plugin Code**
For each plugin that uses external tools:
- Replace hardcoded binary paths
- Use the new `ExternalToolManager` for tool detection
- Update any download/update logic to use manifest URLs
- Remove any local tool management code

#### **D. Update Configuration Files**
- Replace any hardcoded tool paths in config files
- Update default configurations to use new structure
- Remove any legacy tool path settings

### 3. **Implement Manifest.json Integration**

#### **A. Manifest Structure**
Each `manifest.json` contains:
```json
{
  "name": "tool-name",
  "latest": "version-string",
  "versions": ["version1", "version2", ...],
  "url_latest": "https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/tool/latest/tool.exe",
  "url_versioned": "https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/tool/version/tool.exe",
  "description": "Tool description",
  "homepage": "https://tool-homepage.com",
  "license": "License info"
}
```

#### **B. Manifest Reading Logic**
Implement robust manifest reading with:
- Error handling for missing manifests
- Fallback to latest directory if manifest unavailable
- Validation of manifest structure
- Caching for performance

### 4. **Remove Legacy Code**

#### **A. Clean Up Old References**
- Remove any hardcoded tool paths
- Delete legacy tool management code
- Remove old download/update logic
- Clean up temporary directories or cache references

#### **B. Update Documentation**
- Update all README files
- Update help documentation
- Update configuration examples
- Update installation guides

### 5. **Add/Update Tests**

#### **A. Unit Tests**
- Test `ExternalToolManager` functionality
- Test manifest.json reading
- Test tool path resolution
- Test version checking logic

#### **B. Integration Tests**
- Test tool detection in plugins
- Test configuration loading
- Test error handling scenarios

#### **C. Validation Tests**
- Verify all tools are accessible
- Verify manifest files are valid
- Verify URLs are working

## 🛠️ **IMPLEMENTATION GUIDELINES**

### **Code Style**
- Use consistent naming conventions
- Add proper error handling
- Include logging for debugging
- Add JavaDoc comments for public methods

### **Error Handling**
- Graceful fallback if tool not found
- Clear error messages for missing manifests
- Logging for debugging tool issues
- Validation of tool executability

### **Performance**
- Cache manifest data where appropriate
- Lazy loading of tool information
- Efficient path resolution

### **Backward Compatibility**
- Maintain existing plugin interfaces
- Provide migration path for old configurations
- Keep existing tool detection methods working

## 📝 **COMMIT MESSAGES**
Use clear commit messages with the prefix `[bin refactor]`:

```
[bin refactor] Create ExternalToolManager for centralized tool management
[bin refactor] Update ffmpeg plugin to use new binary structure
[bin refactor] Remove legacy tool download logic
[bin refactor] Update documentation for new binary organization
[bin refactor] Add tests for ExternalToolManager
[bin refactor] Update configuration files to use new paths
```

## 🔍 **VERIFICATION CHECKLIST**

After refactoring, verify:
- [ ] All external tools are accessible via new paths
- [ ] No hardcoded tool paths remain in codebase
- [ ] All plugins work with new tool structure
- [ ] Configuration files use new paths
- [ ] Documentation is updated
- [ ] Tests pass
- [ ] Error handling works correctly
- [ ] Performance is acceptable
- [ ] No legacy code remains

## 🚀 **DELIVERABLES**

1. **ExternalToolManager class** - Centralized tool management
2. **Updated plugin code** - All plugins using new structure
3. **Updated configuration files** - Using new paths
4. **Updated documentation** - Reflecting new organization
5. **Comprehensive tests** - Validating new functionality
6. **Clean codebase** - No legacy references

## ⚠️ **IMPORTANT NOTES**

- **Preserve Maven artifacts** in `repository/com/dabi/habitv/` - these are needed for plugins
- **Test thoroughly** - External tools are critical for application functionality
- **Document changes** - Clear migration guide for developers
- **Incremental approach** - Refactor one plugin at a time to minimize risk
- **Backup before changes** - Ensure you can rollback if needed

---

**Use this prompt to systematically refactor the entire codebase to use the new centralized external tool binary organization. Focus on safety, thoroughness, and maintaining functionality while modernizing the codebase.** 
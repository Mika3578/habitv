# Quick Reference: habiTv Bin Refactoring

## 🎯 **Goal**
Refactor codebase to use centralized external tools at `repository/com/dabi/habitv/bin/[tool]/[version]/[tool].exe`

## 🔍 **Key Search Terms**
```bash
# Find all tool references
ffmpeg.exe, curl.exe, aria2c.exe, youtube-dl.exe, rtmpdump.exe
bin/, tools/, external/, downloads/
version, Version, VERSION
download, Download, DOWNLOAD
```

## 🛠️ **Core Implementation**

### **1. Create ExternalToolManager**
```java
public class ExternalToolManager {
    public static String getLatestPath(String toolName)
    public static String getVersionPath(String toolName, String version)
    public static boolean toolExists(String toolName)
    public static List<String> getAvailableVersions(String toolName)
    public static ToolMetadata getToolMetadata(String toolName)
}
```

### **2. Replace Hardcoded Paths**
```java
// OLD
String ffmpegPath = "tools/ffmpeg.exe";

// NEW
String ffmpegPath = ExternalToolManager.getLatestPath("ffmpeg");
```

### **3. Use Manifest.json**
```json
{
  "name": "ffmpeg",
  "latest": "6.1",
  "versions": ["6.1", "5.1"],
  "url_latest": "https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/ffmpeg/latest/ffmpeg.exe"
}
```

## 📝 **Commit Format**
```
[bin refactor] Brief description of changes
```

## ✅ **Verification**
- [ ] All tools accessible via new paths
- [ ] No hardcoded paths remain
- [ ] All plugins work
- [ ] Tests pass
- [ ] Documentation updated

## ⚠️ **Important**
- Preserve Maven artifacts in `repository/com/dabi/habitv/`
- Test thoroughly before committing
- Refactor incrementally (one plugin at a time) 
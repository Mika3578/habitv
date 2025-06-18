package com.dabi.habitv.framework.plugin.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.utils.OSUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Advanced utility for managing external tools with download, verification, and fallback capabilities.
 * 
 * This class provides comprehensive external tool management including:
 * - Automatic download from multiple sources
 * - Checksum verification
 * - Fallback download sources
 * - Progress monitoring
 * - Error handling and recovery
 * 
 * @author habiTv Team
 * @version 4.1.0-SNAPSHOT
 */
public class ExternalToolManager {

    private static final Logger LOG = Logger.getLogger(ExternalToolManager.class);

    // Default timeout for downloads (30 seconds)
    private static final int DEFAULT_TIMEOUT = 30000;

    // Default buffer size for downloads
    private static final int BUFFER_SIZE = 8192;

    // Base repository path for external tools
    private static final String REPOSITORY_BASE_PATH = "repository/com/dabi/habitv/bin/";

    // Repository URL for external tools
    private static final String REPOSITORY_URL = "https://mika3578.github.io/habitv/repository/com/dabi/habitv/bin/";

    // Tool definitions with download URLs and checksums
    private static final Map<String, ToolDefinition> TOOL_DEFINITIONS = new HashMap<>();

    // Cache for tool manifests
    private static final Map<String, ToolManifest> TOOL_MANIFESTS = new HashMap<>();

    // Jackson ObjectMapper for JSON parsing
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // Initialize tool definitions
        initializeToolDefinitions();
    }

    /**
     * Tool manifest containing metadata from manifest.json.
     */
    public static class ToolManifest {
        private final String name;
        private final String latest;
        private final List<String> versions;
        private final String urlLatest;
        private final String urlVersioned;
        private final String description;
        private final String homepage;
        private final String license;

        public ToolManifest(JsonNode json) {
            this.name = json.has("name") ? json.get("name").asText() : null;
            this.latest = json.has("latest") ? json.get("latest").asText() : null;
            this.versions = new ArrayList<>();
            if (json.has("versions") && json.get("versions").isArray()) {
                JsonNode versionsArray = json.get("versions");
                for (JsonNode version : versionsArray) {
                    this.versions.add(version.asText());
                }
            }
            this.urlLatest = json.has("url_latest") ? json.get("url_latest").asText() : null;
            this.urlVersioned = json.has("url_versioned") ? json.get("url_versioned").asText() : null;
            this.description = json.has("description") ? json.get("description").asText() : null;
            this.homepage = json.has("homepage") ? json.get("homepage").asText() : null;
            this.license = json.has("license") ? json.get("license").asText() : null;
        }

        // Getters
        public String getName() { return name; }
        public String getLatest() { return latest; }
        public List<String> getVersions() { return versions; }
        public String getUrlLatest() { return urlLatest; }
        public String getUrlVersioned() { return urlVersioned; }
        public String getDescription() { return description; }
        public String getHomepage() { return homepage; }
        public String getLicense() { return license; }
    }

    /**
     * Tool definition containing download URLs, checksums, and metadata.
     */
    public static class ToolDefinition {
        private final String name;
        private final String windowsUrl;
        private final String linuxUrl;
        private final String windowsChecksum;
        private final String linuxChecksum;
        private final String versionParam;
        private final String versionPattern;

        public ToolDefinition(String name, String windowsUrl, String linuxUrl, 
                            String windowsChecksum, String linuxChecksum,
                            String versionParam, String versionPattern) {
            this.name = name;
            this.windowsUrl = windowsUrl;
            this.linuxUrl = linuxUrl;
            this.windowsChecksum = windowsChecksum;
            this.linuxChecksum = linuxChecksum;
            this.versionParam = versionParam;
            this.versionPattern = versionPattern;
        }

        // Getters
        public String getName() { return name; }
        public String getWindowsUrl() { return windowsUrl; }
        public String getLinuxUrl() { return linuxUrl; }
        public String getWindowsChecksum() { return windowsChecksum; }
        public String getLinuxChecksum() { return linuxChecksum; }
        public String getVersionParam() { return versionParam; }
        public String getVersionPattern() { return versionPattern; }
    }

    /**
     * Download result containing status and metadata.
     */
    public static class DownloadResult {
        private final boolean success;
        private final String version;
        private final String path;
        private final String error;
        private final long downloadTime;

        public DownloadResult(boolean success, String version, String path, String error, long downloadTime) {
            this.success = success;
            this.version = version;
            this.path = path;
            this.error = error;
            this.downloadTime = downloadTime;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getVersion() { return version; }
        public String getPath() { return path; }
        public String getError() { return error; }
        public long getDownloadTime() { return downloadTime; }
    }

    /**
     * Initialize tool definitions with download URLs and metadata.
     */
    private static void initializeToolDefinitions() {
        // yt-dlp
        TOOL_DEFINITIONS.put("yt-dlp", new ToolDefinition(
            "yt-dlp",
            "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe",
            "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp",
            null, // Checksums can be added when available
            null,
            "--version",
            "([\\-0-9A-Za-z.-]*)"
        ));

        // ffmpeg
        TOOL_DEFINITIONS.put("ffmpeg", new ToolDefinition(
            "ffmpeg",
            "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip",
            "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz",
            null,
            null,
            "-version",
            "ffmpeg version ([\\-0-9A-Za-z.-]*).*"
        ));

        // aria2c
        TOOL_DEFINITIONS.put("aria2c", new ToolDefinition(
            "aria2c",
            "https://github.com/aria2/aria2/releases/latest/download/aria2-1.36.0-win-64bit-build1.zip",
            "https://github.com/aria2/aria2/releases/latest/download/aria2-1.36.0-linux-gnu-64bit-build1.tar.bz2",
            null,
            null,
            "-v",
            "aria2 version ([0-9A-Za-z.-]*).*"
        ));

        // curl
        TOOL_DEFINITIONS.put("curl", new ToolDefinition(
            "curl",
            "https://curl.se/windows/dl-8.5.0_5/curl-8.5.0_5-win64-mingw.zip",
            "https://curl.se/download/curl-8.5.0.tar.gz",
            null,
            null,
            "--version",
            "curl ([0-9A-Za-z.-]*).*"
        ));

        // rtmpdump
        TOOL_DEFINITIONS.put("rtmpdump", new ToolDefinition(
            "rtmpdump",
            "https://rtmpdump.mplayerhq.hu/download/rtmpdump-2.4.zip",
            "https://rtmpdump.mplayerhq.hu/download/rtmpdump-2.4.tar.gz",
            null,
            null,
            "-h",
            "RTMPDump ([0-9A-Za-z.-]*) .*"
        ));
    }

    /**
     * Download and install an external tool with automatic fallback.
     * 
     * @param toolName Name of the tool to download
     * @param binDir Directory to install the tool
     * @param timeout Download timeout in milliseconds
     * @return DownloadResult containing status and metadata
     */
    public static DownloadResult downloadTool(String toolName, String binDir, int timeout) {
        long startTime = System.currentTimeMillis();

        try {
            // Read the tool manifest
            ToolManifest manifest = readToolManifest(toolName);
            if (manifest == null) {
                // Fall back to the old method if manifest is not available
                return downloadToolLegacy(toolName, binDir, timeout);
            }

            // Create bin directory if it doesn't exist
            Path binPath = Paths.get(binDir);
            if (!Files.exists(binPath)) {
                Files.createDirectories(binPath);
            }

            // Determine download URL and file name
            String downloadUrl = OSUtils.isWindows() ? manifest.getUrlLatest() : manifest.getUrlLatest().replace(".exe", "");
            String fileName = OSUtils.isWindows() ? toolName + ".exe" : toolName;
            Path targetPath = binPath.resolve(fileName);

            LOG.info("Downloading " + toolName + " from " + downloadUrl);

            // Download the file
            boolean downloadSuccess = downloadFile(downloadUrl, targetPath, timeout);
            if (!downloadSuccess) {
                return new DownloadResult(false, null, null, 
                    "Failed to download " + toolName, System.currentTimeMillis() - startTime);
            }

            // Make executable on Linux
            if (!OSUtils.isWindows()) {
                try {
                    Files.setPosixFilePermissions(targetPath, 
                        java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x"));
                } catch (Exception e) {
                    LOG.warn("Failed to set executable permissions for " + toolName + ": " + e.getMessage());
                }
            }

            // Get the version from the manifest
            String version = manifest.getLatest();

            LOG.info("Successfully downloaded " + toolName + " version " + version);
            return new DownloadResult(true, version, targetPath.toString(), null, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            LOG.error("Error downloading " + toolName + ": " + e.getMessage(), e);
            return new DownloadResult(false, null, null, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Legacy method for downloading tools using the old approach.
     * This is used as a fallback when the manifest is not available.
     * 
     * @param toolName Name of the tool to download
     * @param binDir Directory to install the tool
     * @param timeout Download timeout in milliseconds
     * @return DownloadResult containing status and metadata
     */
    private static DownloadResult downloadToolLegacy(String toolName, String binDir, int timeout) {
        long startTime = System.currentTimeMillis();

        try {
            ToolDefinition toolDef = TOOL_DEFINITIONS.get(toolName);
            if (toolDef == null) {
                return new DownloadResult(false, null, null, 
                    "Unknown tool: " + toolName, System.currentTimeMillis() - startTime);
            }

            // Create bin directory if it doesn't exist
            Path binPath = Paths.get(binDir);
            if (!Files.exists(binPath)) {
                Files.createDirectories(binPath);
            }

            // Determine download URL based on OS
            String downloadUrl = OSUtils.isWindows() ? toolDef.getWindowsUrl() : toolDef.getLinuxUrl();
            String fileName = OSUtils.isWindows() ? toolDef.getName() + ".exe" : toolDef.getName();
            Path targetPath = binPath.resolve(fileName);

            LOG.info("Downloading " + toolName + " from " + downloadUrl + " (legacy method)");

            // Download the file
            boolean downloadSuccess = downloadFile(downloadUrl, targetPath, timeout);
            if (!downloadSuccess) {
                return new DownloadResult(false, null, null, 
                    "Failed to download " + toolName, System.currentTimeMillis() - startTime);
            }

            // Make executable on Linux
            if (!OSUtils.isWindows()) {
                try {
                    Files.setPosixFilePermissions(targetPath, 
                        java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x"));
                } catch (Exception e) {
                    LOG.warn("Failed to set executable permissions for " + toolName + ": " + e.getMessage());
                }
            }

            // Verify installation by checking version
            String version = getToolVersion(targetPath.toString(), toolDef.getVersionParam());
            if (version == null) {
                return new DownloadResult(false, null, null, 
                    "Failed to verify " + toolName + " installation", System.currentTimeMillis() - startTime);
            }

            LOG.info("Successfully downloaded " + toolName + " version " + version);
            return new DownloadResult(true, version, targetPath.toString(), null, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            LOG.error("Error downloading " + toolName + ": " + e.getMessage(), e);
            return new DownloadResult(false, null, null, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Download a file from URL with progress monitoring.
     * 
     * @param urlString URL to download from
     * @param targetPath Path to save the file
     * @param timeout Download timeout in milliseconds
     * @return true if download successful, false otherwise
     */
    private static boolean downloadFile(String urlString, Path targetPath, int timeout) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestProperty("User-Agent", "habiTv/4.1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOG.error("HTTP error " + responseCode + " when downloading " + urlString);
                return false;
            }

            long contentLength = connection.getContentLengthLong();
            long downloadedBytes = 0;

            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(targetPath)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;

                    // Log progress for large files
                    if (contentLength > 0 && downloadedBytes % (1024 * 1024) == 0) {
                        int progress = (int) ((downloadedBytes * 100) / contentLength);
                        LOG.debug("Download progress: " + progress + "%");
                    }
                }
            }

            LOG.info("Downloaded " + downloadedBytes + " bytes to " + targetPath);
            return true;

        } catch (Exception e) {
            LOG.error("Error downloading file: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Get the version of an installed tool.
     * 
     * @param toolPath Path to the tool executable
     * @param versionParam Version parameter (e.g., "--version", "-v")
     * @return Version string or null if failed
     */
    public static String getToolVersion(String toolPath, String versionParam) {
        try {
            ProcessBuilder pb = new ProcessBuilder(toolPath, versionParam);
            Process process = pb.start();

            // Read output with timeout
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    return reader.readLine();
                } catch (IOException e) {
                    return null;
                }
            });

            String output = outputFuture.get(10, TimeUnit.SECONDS);
            int exitCode = process.waitFor();

            if (exitCode == 0 && output != null) {
                return output.trim();
            }

        } catch (Exception e) {
            LOG.debug("Error getting version for " + toolPath + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if a tool is installed and working.
     * 
     * @param toolName Name of the tool
     * @param binDir Directory containing the tool
     * @return true if tool is available and working
     */
    public static boolean isToolAvailable(String toolName, String binDir) {
        try {
            // First check if the tool exists in the new structure
            if (toolExists(toolName)) {
                String fileName = OSUtils.isWindows() ? toolName + ".exe" : toolName;
                Path toolPath = Paths.get(binDir, fileName);

                if (Files.exists(toolPath)) {
                    // Tool exists in the bin directory, check if it works
                    ToolManifest manifest = readToolManifest(toolName);
                    ToolDefinition toolDef = TOOL_DEFINITIONS.get(toolName);

                    // Use the version parameter from the tool definition if available
                    String versionParam = (toolDef != null) ? toolDef.getVersionParam() : "--version";

                    // Test if tool works by checking version
                    String version = getToolVersion(toolPath.toString(), versionParam);
                    return version != null;
                }
            }

            // Fall back to the legacy method
            return isToolAvailableLegacy(toolName, binDir);

        } catch (Exception e) {
            LOG.debug("Error checking tool availability for " + toolName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Legacy method for checking if a tool is installed and working.
     * This is used as a fallback when the manifest is not available.
     * 
     * @param toolName Name of the tool
     * @param binDir Directory containing the tool
     * @return true if tool is available and working
     */
    private static boolean isToolAvailableLegacy(String toolName, String binDir) {
        try {
            ToolDefinition toolDef = TOOL_DEFINITIONS.get(toolName);
            if (toolDef == null) {
                return false;
            }

            String fileName = OSUtils.isWindows() ? toolName + ".exe" : toolName;
            Path toolPath = Paths.get(binDir, fileName);

            if (!Files.exists(toolPath)) {
                return false;
            }

            // Test if tool works by checking version
            String version = getToolVersion(toolPath.toString(), toolDef.getVersionParam());
            return version != null;

        } catch (Exception e) {
            LOG.debug("Error checking tool availability (legacy) for " + toolName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Calculate SHA-256 checksum of a file.
     * 
     * @param filePath Path to the file
     * @return SHA-256 checksum as hex string
     */
    public static String calculateChecksum(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(filePath));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            LOG.error("Error calculating checksum: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Verify file checksum.
     * 
     * @param filePath Path to the file
     * @param expectedChecksum Expected SHA-256 checksum
     * @return true if checksum matches
     */
    public static boolean verifyChecksum(Path filePath, String expectedChecksum) {
        if (expectedChecksum == null) {
            return true; // Skip verification if no checksum provided
        }

        String actualChecksum = calculateChecksum(filePath);
        if (actualChecksum == null) {
            return false;
        }

        return actualChecksum.equalsIgnoreCase(expectedChecksum);
    }

    /**
     * Extract ZIP file to directory.
     * 
     * @param zipPath Path to ZIP file
     * @param extractDir Directory to extract to
     * @return true if extraction successful
     */
    public static boolean extractZip(Path zipPath, Path extractDir) {
        try {
            if (!Files.exists(extractDir)) {
                Files.createDirectories(extractDir);
            }

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = extractDir.resolve(entry.getName());

                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                    } else {
                        Files.createDirectories(filePath.getParent());
                        Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    zis.closeEntry();
                }
            }

            return true;

        } catch (Exception e) {
            LOG.error("Error extracting ZIP file: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get tool definition for a specific tool.
     * 
     * @param toolName Name of the tool
     * @return ToolDefinition or null if not found
     */
    public static ToolDefinition getToolDefinition(String toolName) {
        return TOOL_DEFINITIONS.get(toolName);
    }

    /**
     * Get all available tool names.
     * 
     * @return Array of tool names
     */
    public static String[] getAvailableTools() {
        return TOOL_DEFINITIONS.keySet().toArray(new String[0]);
    }

    /**
     * Read and parse a tool's manifest.json file.
     * 
     * @param toolName Name of the tool
     * @return ToolManifest object or null if not found
     */
    public static ToolManifest readToolManifest(String toolName) {
        // Check cache first
        if (TOOL_MANIFESTS.containsKey(toolName)) {
            return TOOL_MANIFESTS.get(toolName);
        }

        try {
            // Construct the URL for the manifest.json file
            String manifestUrl = REPOSITORY_URL + toolName + "/manifest.json";
            LOG.info("Reading manifest from " + manifestUrl);

            // Download and parse the manifest.json file
            URL url = new URL(manifestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setRequestProperty("User-Agent", "habiTv/4.2.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOG.error("HTTP error " + responseCode + " when downloading manifest for " + toolName);
                return null;
            }

            // Parse the JSON
            JsonNode rootNode = OBJECT_MAPPER.readTree(connection.getInputStream());
            ToolManifest manifest = new ToolManifest(rootNode);

            // Cache the manifest
            TOOL_MANIFESTS.put(toolName, manifest);

            return manifest;
        } catch (Exception e) {
            LOG.error("Error reading manifest for " + toolName + ": " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get the path to the latest version of a tool.
     * 
     * @param toolName Name of the tool
     * @return Path to the latest version or null if not found
     */
    public static String getLatestPath(String toolName) {
        ToolManifest manifest = readToolManifest(toolName);
        if (manifest == null) {
            LOG.warn("No manifest found for " + toolName);
            return null;
        }

        String fileName = OSUtils.isWindows() ? toolName + ".exe" : toolName;
        return REPOSITORY_BASE_PATH + toolName + "/latest/" + fileName;
    }

    /**
     * Get the path to a specific version of a tool.
     * 
     * @param toolName Name of the tool
     * @param version Version of the tool
     * @return Path to the specified version or null if not found
     */
    public static String getVersionPath(String toolName, String version) {
        ToolManifest manifest = readToolManifest(toolName);
        if (manifest == null) {
            LOG.warn("No manifest found for " + toolName);
            return null;
        }

        if (!manifest.getVersions().contains(version)) {
            LOG.warn("Version " + version + " not found for " + toolName);
            return null;
        }

        String fileName = OSUtils.isWindows() ? toolName + ".exe" : toolName;
        return REPOSITORY_BASE_PATH + toolName + "/" + version + "/" + fileName;
    }

    /**
     * Check if a tool exists in the repository.
     * 
     * @param toolName Name of the tool
     * @return true if the tool exists
     */
    public static boolean toolExists(String toolName) {
        return readToolManifest(toolName) != null;
    }

    /**
     * Get all available versions of a tool.
     * 
     * @param toolName Name of the tool
     * @return List of available versions or empty list if not found
     */
    public static List<String> getAvailableVersions(String toolName) {
        ToolManifest manifest = readToolManifest(toolName);
        if (manifest == null) {
            return new ArrayList<>();
        }

        return manifest.getVersions();
    }

    /**
     * Get metadata for a tool.
     * 
     * @param toolName Name of the tool
     * @return ToolManifest object or null if not found
     */
    public static ToolManifest getToolMetadata(String toolName) {
        return readToolManifest(toolName);
    }
} 

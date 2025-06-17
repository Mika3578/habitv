package com.dabi.habitv.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.utils.HabitvLogger;

/**
 * Local version of RetrieverUtils to fix compilation issues.
 * This class provides utility methods for retrieving content from URLs.
 */
public final class RetrieverUtils {
    
    private static final Logger LOG = HabitvLogger.getLogger(RetrieverUtils.class);
    private static final int DEFAULT_TIMEOUT = 30000; // 30 seconds
    
    private RetrieverUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the content of a URL as a string.
     * 
     * @param url the URL to retrieve
     * @param proxy the proxy to use, or null for direct connection
     * @return the content of the URL as a string
     */
    public static String getUrlContent(String url, Proxy proxy) {
        try {
            HttpURLConnection connection = openConnection(url, proxy);
            prepareConnection(DEFAULT_TIMEOUT, connection);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (IOException e) {
            LOG.error("Failed to retrieve content from URL: " + url, e);
            return ""; // Return empty string instead of throwing exception for non-critical operation
        }
    }
    
    private static HttpURLConnection openConnection(String url, Proxy proxy) throws IOException {
        URL urlObj = new URL(url);
        if (proxy != null) {
            return (HttpURLConnection) urlObj.openConnection(proxy);
        } else {
            return (HttpURLConnection) urlObj.openConnection();
        }
    }
    
    private static void prepareConnection(Integer timeOut, HttpURLConnection connection) {
        try {
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }
}
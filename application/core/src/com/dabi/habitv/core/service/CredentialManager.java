package com.dabi.habitv.core.service;

import com.dabi.habitv.core.config.CredentialConfig;
import com.dabi.habitv.core.config.UserConfig;
import com.dabi.habitv.core.config.XMLUserConfig;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages credentials for various services that require authentication.
 */
public class CredentialManager {

    private static final Logger LOG = Logger.getLogger(CredentialManager.class);
    private static CredentialManager instance;
    private Map<String, CredentialConfig> credentials = new HashMap<>();
    private UserConfig userConfig;

    private CredentialManager() {
        // Private constructor for singleton
        try {
            this.userConfig = XMLUserConfig.initConfig();
            loadCredentialsFromConfig();
        } catch (Exception e) {
            LOG.error("Failed to initialize CredentialManager: " + e.getMessage(), e);
        }
    }

    public static synchronized CredentialManager getInstance() {
        if (instance == null) {
            instance = new CredentialManager();
        }
        return instance;
    }

    /**
     * Load credentials from configuration file
     */
    private void loadCredentialsFromConfig() {
        try {
            if (userConfig != null) {
                List<CredentialConfig> configCredentials = userConfig.getCredentials();
                credentials.clear();
                for (CredentialConfig credential : configCredentials) {
                    if (credential != null && credential.getService() != null) {
                        credentials.put(credential.getService().toLowerCase(), credential);
                    }
                }
                LOG.info("Loaded " + credentials.size() + " credentials from configuration");
            }
        } catch (Exception e) {
            LOG.error("Failed to load credentials from configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Save credentials to configuration file
     */
    private void saveCredentialsToConfig() {
        try {
            if (userConfig != null) {
                List<CredentialConfig> credentialsList = new ArrayList<>(credentials.values());
                userConfig.setCredentials(credentialsList);
                XMLUserConfig.saveConfig(userConfig);
                LOG.info("Saved " + credentials.size() + " credentials to configuration");
            }
        } catch (Exception e) {
            LOG.error("Failed to save credentials to configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Add or update a credential for a service.
     */
    public void addCredential(CredentialConfig credential) {
        if (credential != null && credential.getService() != null) {
            credentials.put(credential.getService().toLowerCase(), credential);
            LOG.info("Added credential for service: " + credential.getService());
            saveCredentialsToConfig();
        }
    }

    /**
     * Get credential for a specific service.
     */
    public CredentialConfig getCredential(String service) {
        if (service == null) {
            return null;
        }
        CredentialConfig credential = credentials.get(service.toLowerCase());
        if (credential != null && credential.getEnabled()) {
            return credential;
        }
        return null;
    }

    /**
     * Check if credentials are available for a service.
     */
    public boolean hasCredential(String service) {
        CredentialConfig credential = getCredential(service);
        return credential != null && credential.getUsername() != null && credential.getPassword() != null;
    }

    /**
     * Remove credential for a service.
     */
    public void removeCredential(String service) {
        if (service != null) {
            credentials.remove(service.toLowerCase());
            LOG.info("Removed credential for service: " + service);
            saveCredentialsToConfig();
        }
    }

    /**
     * Get all credentials.
     */
    public List<CredentialConfig> getAllCredentials() {
        return new ArrayList<>(credentials.values());
    }

    /**
     * Get enabled credentials only.
     */
    public List<CredentialConfig> getEnabledCredentials() {
        List<CredentialConfig> enabled = new ArrayList<>();
        for (CredentialConfig credential : credentials.values()) {
            if (credential.getEnabled()) {
                enabled.add(credential);
            }
        }
        return enabled;
    }

    /**
     * Clear all credentials.
     */
    public void clearCredentials() {
        credentials.clear();
        LOG.info("Cleared all credentials");
        saveCredentialsToConfig();
    }

    /**
     * Get username for a service.
     */
    public String getUsername(String service) {
        CredentialConfig credential = getCredential(service);
        return credential != null ? credential.getUsername() : null;
    }

    /**
     * Get password for a service.
     */
    public String getPassword(String service) {
        CredentialConfig credential = getCredential(service);
        return credential != null ? credential.getPassword() : null;
    }

    /**
     * Check if a service is enabled.
     */
    public boolean isServiceEnabled(String service) {
        CredentialConfig credential = getCredential(service);
        return credential != null && credential.getEnabled();
    }
} 
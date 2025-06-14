package com.dabi.habitv.core.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a credential configuration for authentication with various services.
 */
@XmlRootElement(name = "credential")
@XmlAccessorType(XmlAccessType.FIELD)
public class CredentialConfig {

    @XmlElement(name = "service")
    private String service;

    @XmlElement(name = "username")
    private String username;

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "enabled")
    private Boolean enabled = true;

    @XmlElement(name = "description")
    private String description;

    // Default constructor
    public CredentialConfig() {
    }

    // Constructor with parameters
    public CredentialConfig(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
        this.enabled = true;
    }

    // Getters and Setters
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CredentialConfig{" +
                "service='" + service + '\'' +
                ", username='" + username + '\'' +
                ", enabled=" + enabled +
                ", description='" + description + '\'' +
                '}';
    }
} 
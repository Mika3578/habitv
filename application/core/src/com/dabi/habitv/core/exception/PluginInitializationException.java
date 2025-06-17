package com.dabi.habitv.core.exception;

import com.dabi.habitv.api.plugin.exception.TechnicalException;

/**
 * Exception thrown when a plugin fails to initialize.
 * This can happen due to missing dependencies, invalid configuration, or other issues.
 */
public class PluginInitializationException extends TechnicalException {

    private static final long serialVersionUID = 1L;
    
    private final String pluginName;

    /**
     * Constructs a new PluginInitializationException with the specified plugin name and detail message.
     * 
     * @param pluginName the name of the plugin that failed to initialize
     * @param message the detail message
     */
    public PluginInitializationException(String pluginName, String message) {
        super(message);
        this.pluginName = pluginName;
    }

    /**
     * Constructs a new PluginInitializationException with the specified plugin name, detail message, and cause.
     * 
     * @param pluginName the name of the plugin that failed to initialize
     * @param message the detail message
     * @param cause the cause
     */
    public PluginInitializationException(String pluginName, String message, Throwable cause) {
        super(message, cause);
        this.pluginName = pluginName;
    }

    /**
     * Gets the name of the plugin that failed to initialize.
     * 
     * @return the plugin name
     */
    public String getPluginName() {
        return pluginName;
    }
}
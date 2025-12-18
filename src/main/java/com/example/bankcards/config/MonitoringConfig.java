package com.example.bankcards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for package-level monitoring.
 * <p>
 * Allows customization of which packages should be automatically monitored
 * without requiring code changes.
 */
@Configuration
@ConfigurationProperties(prefix = "app.monitoring")
@Getter
@Setter
public class MonitoringConfig {

    /**
     * Enable or disable package-level monitoring
     */
    private boolean enabled = true;

    /**
     * List of package patterns to monitor (supports wildcards)
     * Example: "com.example.bankcards.service.*"
     */
    private List<String> packages = new ArrayList<>(List.of(
            "com.example.bankcards.service",
            "com.example.bankcards.controller",
            "com.example.bankcards.repository"
    ));

    /**
     * Whether to include repository layer in monitoring
     * (may generate high volume of metrics)
     */
    private boolean includeRepositoryLayer = false;

    /**
     * Whether to include private methods in monitoring
     */
    private boolean includePrivateMethods = false;

    /**
     * Minimum execution time in milliseconds to record
     * (helps reduce metric cardinality for very fast methods)
     */
    private long minExecutionTimeMs = 0;
}

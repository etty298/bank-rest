package com.example.bankcards.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for package-level monitoring aspect.
 * <p>
 * This configuration class manages settings for the {@link com.example.bankcards.metrics.PackageMonitoringAspect}
 * which automatically monitors method execution time across entire packages.
 * <p>
 * Configuration properties under {@code app.monitoring} prefix:
 * <ul>
 *   <li>{@code enabled} - Enable or disable package-level monitoring (default: true)</li>
 *   <li>{@code include-repository-layer} - Include repository methods in monitoring (default: false)</li>
 *   <li>{@code include-private-methods} - Monitor private methods (default: false, currently unused)</li>
 *   <li>{@code min-execution-time-ms} - Minimum execution time to record metrics (default: 0)</li>
 * </ul>
 * <p>
 * <strong>Example configuration in application.yml:</strong>
 * <pre>
 * app:
 *   monitoring:
 *     enabled: true
 *     include-repository-layer: false
 *     min-execution-time-ms: 100
 * </pre>
 * <p>
 * <strong>Note:</strong> Repository layer monitoring is disabled by default to prevent
 * metric cardinality explosion, as repository methods are typically high-volume.
 *
 * @see com.example.bankcards.metrics.PackageMonitoringAspect
 */
@Configuration
@ConfigurationProperties(prefix = "app.monitoring")
@Getter
@Setter
public class MonitoringConfig {

    /**
     * Enable or disable package-level monitoring.
     * <p>
     * When set to {@code false}, the {@link com.example.bankcards.metrics.PackageMonitoringAspect}
     * will not be loaded at all.
     * <p>
     * Default: {@code true}
     */
    private boolean enabled = true;

    /**
     * List of package patterns to monitor (supports wildcards).
     * <p>
     * This property is currently unused and reserved for future implementation
     * of dynamic package selection.
     * <p>
     * Example: {@code "com.example.bankcards.service.*"}
     * <p>
     * Default: service, controller, and repository packages
     */
    private List<String> packages = new ArrayList<>(List.of(
            "com.example.bankcards.service",
            "com.example.bankcards.controller",
            "com.example.bankcards.repository"
    ));

    /**
     * Whether to include repository layer in monitoring.
     * <p>
     * Repository methods are typically called frequently and execute quickly,
     * which can lead to high metric cardinality and storage costs. It's recommended
     * to keep this disabled unless you specifically need repository-level metrics.
     * <p>
     * Default: {@code false}
     */
    private boolean includeRepositoryLayer = false;

    /**
     * Whether to include private methods in monitoring.
     * <p>
     * This property is currently unused and reserved for future implementation.
     * AspectJ pointcuts currently match all method visibilities.
     * <p>
     * Default: {@code false}
     */
    private boolean includePrivateMethods = false;

    /**
     * Minimum execution time in milliseconds to record metrics.
     * <p>
     * Methods that execute faster than this threshold will not generate metrics.
     * This helps reduce metric cardinality and storage costs by filtering out
     * very fast methods that may not be interesting for monitoring.
     * <p>
     * Set to {@code 0} to record all method executions regardless of duration.
     * <p>
     * Example: Setting to {@code 100} will only record methods taking 100ms or longer.
     * <p>
     * Default: {@code 0} (record all methods)
     */
    private long minExecutionTimeMs = 0;
}

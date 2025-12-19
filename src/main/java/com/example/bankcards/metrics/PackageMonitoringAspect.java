package com.example.bankcards.metrics;

import com.example.bankcards.config.MonitoringConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static io.micrometer.core.aop.TimedAspect.EXCEPTION_TAG;

/**
 * Aspect for automatically monitoring method execution time across entire packages.
 * <p>
 * This aspect creates timing metrics for all methods in configured packages without
 * requiring explicit {@link MonitoringTimed} annotation on each method. It uses AspectJ
 * pointcut expressions to intercept method calls in service, controller, and repository layers.
 * <p>
 * Key features:
 * <ul>
 *   <li>Automatic monitoring of entire packages using {@code @Pointcut} expressions</li>
 *   <li>Configurable inclusion/exclusion of specific layers (service, controller, repository)</li>
 *   <li>Execution time threshold filtering to reduce metric cardinality</li>
 *   <li>Exception tracking in metric tags</li>
 *   <li>Layer-based metric tagging for easy filtering</li>
 * </ul>
 * <p>
 * Configuration is managed through {@link MonitoringConfig} properties:
 * <ul>
 *   <li>{@code app.monitoring.enabled} - Enable/disable monitoring</li>
 *   <li>{@code app.monitoring.include-repository-layer} - Include repository methods</li>
 *   <li>{@code app.monitoring.min-execution-time-ms} - Minimum time threshold</li>
 * </ul>
 * <p>
 * Metrics are published to Prometheus with the name {@code method_execution} and include
 * tags for class, method, layer, and exception type.
 *
 * @see MonitoringConfig
 * @see MonitoringTimed
 * @see io.micrometer.core.instrument.Timer
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PackageMonitoringAspect {

    public static final String PACKAGE_METRIC_NAME = "method_execution";
    private final MeterRegistry registry;
    private final MonitoringConfig monitoringConfig;
    private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint = pjp ->
            Tags.of(
                    "class", pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                    "method", pjp.getStaticPart().getSignature().getName()
            );

    /**
     * Pointcut for all service layer methods.
     * <p>
     * Matches all methods in classes under the {@code com.example.bankcards.service} package
     * and its sub-packages.
     * <p>
     * <strong>Pattern:</strong> {@code execution(* com.example.bankcards.service..*.*(..))}
     */
    @Pointcut("execution(* com.example.bankcards.service..*.*(..))")
    public void serviceLayer() {
    }

    /**
     * Pointcut for all controller layer methods.
     * <p>
     * Matches all methods in classes under the {@code com.example.bankcards.controller} package
     * and its sub-packages.
     * <p>
     * <strong>Pattern:</strong> {@code execution(* com.example.bankcards.controller..*.*(..))}
     */
    @Pointcut("execution(* com.example.bankcards.controller..*.*(..))")
    public void controllerLayer() {
    }

    /**
     * Pointcut for all repository layer methods.
     * <p>
     * Matches all methods in classes under the {@code com.example.bankcards.repository} package
     * and its sub-packages. This pointcut is optional and controlled by configuration.
     * <p>
     * <strong>Pattern:</strong> {@code execution(* com.example.bankcards.repository..*.*(..))}
     * <p>
     * <strong>Note:</strong> Repository monitoring is disabled by default to avoid high metric volume.
     */
    @Pointcut("execution(* com.example.bankcards.repository..*.*(..))")
    public void repositoryLayer() {
    }

    /**
     * Combined pointcut for service and controller layers.
     * <p>
     * This pointcut combines {@link #serviceLayer()} and {@link #controllerLayer()}
     * to monitor both layers without including repository methods.
     */
    @Pointcut("serviceLayer() || controllerLayer()")
    public void monitoredLayersWithoutRepository() {
    }

    /**
     * Measures execution time for service and controller layer methods.
     * <p>
     * This advice wraps method execution with a timer to record execution time metrics.
     * It automatically adds tags for class, method, layer, and exception information.
     * <p>
     * Repository methods are handled separately by {@link #monitorRepositoryMethod(ProceedingJoinPoint)}.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     * @see #recordMetric(ProceedingJoinPoint)
     */
    @Around("monitoredLayersWithoutRepository()")
    public Object monitorPackageMethod(ProceedingJoinPoint pjp) throws Throwable {
        return recordMetric(pjp);
    }

    /**
     * Measures execution time for repository layer methods (configurable).
     * <p>
     * This advice wraps repository method execution with a timer, but only records metrics
     * if {@code app.monitoring.include-repository-layer} is set to {@code true}.
     * <p>
     * Repository monitoring is disabled by default because repository methods are typically
     * high-volume and fast, which can lead to metric cardinality explosion.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     * @see MonitoringConfig#isIncludeRepositoryLayer()
     */
    @Around("repositoryLayer()")
    public Object monitorRepositoryMethod(ProceedingJoinPoint pjp) throws Throwable {
        if (!monitoringConfig.isIncludeRepositoryLayer()) {
            return pjp.proceed();
        }
        return recordMetric(pjp);
    }

    /**
     * Records execution time and creates a timer metric for the intercepted method.
     * <p>
     * This method performs the actual metric recording with the following features:
     * <ul>
     *   <li>Starts a timer before method execution</li>
     *   <li>Tracks exceptions and includes exception type in metric tags</li>
     *   <li>Applies execution time threshold filtering</li>
     *   <li>Adds layer, class, and method tags to the metric</li>
     * </ul>
     * <p>
     * If the execution time is below {@code app.monitoring.min-execution-time-ms},
     * the metric is not recorded to reduce storage overhead.
     * <p>
     * The metric name is {@code method_execution} with tags:
     * <ul>
     *   <li>{@code layer} - The application layer (service, controller, repository)</li>
     *   <li>{@code class} - Full qualified class name</li>
     *   <li>{@code method} - Method name</li>
     *   <li>{@code exception} - Exception class name or "none"</li>
     * </ul>
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     * @see MonitoringConfig#getMinExecutionTimeMs()
     */
    private Object recordMetric(ProceedingJoinPoint pjp) throws Throwable {
        Timer.Sample sample = Timer.start(registry);
        String exceptionClass = "none";
        String layer = determineLayer(pjp);
        long startTime = System.currentTimeMillis();

        try {
            return pjp.proceed();
        } catch (Exception ex) {
            exceptionClass = ex.getClass().getSimpleName();
            throw ex;
        } finally {
            try {
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Only record if execution time exceeds minimum threshold
                if (executionTime >= monitoringConfig.getMinExecutionTimeMs()) {
                    sample.stop(
                            Timer.builder(PACKAGE_METRIC_NAME)
                                    .description("Execution time of methods in monitored packages")
                                    .tag("layer", layer)
                                    .tag(EXCEPTION_TAG, exceptionClass)
                                    .tags(tagsBasedOnJoinPoint.apply(pjp))
                                    .register(registry)
                    );
                    log.trace("Recorded metric for {}.{} ({}ms)", 
                             pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                             pjp.getStaticPart().getSignature().getName(),
                             executionTime);
                }
            } catch (Exception e) {
                log.warn("Failed to record metric for method: {}", pjp.getSignature(), e);
            }
        }
    }

    /**
     * Determines the application layer based on the package name.
     * <p>
     * Analyzes the declaring class name to identify which layer the method belongs to:
     * <ul>
     *   <li>controller - Methods in {@code *.controller.*} packages</li>
     *   <li>service - Methods in {@code *.service.*} packages</li>
     *   <li>repository - Methods in {@code *.repository.*} packages</li>
     *   <li>unknown - Methods in other packages</li>
     * </ul>
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @return the layer name as a string (controller, service, repository, or unknown)
     */
    private String determineLayer(ProceedingJoinPoint pjp) {
        String className = pjp.getStaticPart().getSignature().getDeclaringTypeName();
        
        if (className.contains(".controller.")) {
            return "controller";
        } else if (className.contains(".service.")) {
            return "service";
        } else if (className.contains(".repository.")) {
            return "repository";
        }
        
        return "unknown";
    }
}

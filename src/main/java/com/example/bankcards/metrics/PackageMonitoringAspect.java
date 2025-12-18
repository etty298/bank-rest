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
 * Aspect for automatically monitoring all methods in specific packages.
 * <p>
 * This aspect automatically creates metrics for all public methods in the configured packages
 * without requiring explicit annotation on each method.
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
     * Pointcut for all service layer methods
     */
    @Pointcut("execution(* com.example.bankcards.controller..*.*(..))")
    public void controllerLayer() {
    }

    /**
     * Around advice that measures execution time for all methods in monitored packages
     * (excluding repository if configured)
     */
    @Around("controllerLayer()")
    public Object monitorPackageMethod(ProceedingJoinPoint pjp) throws Throwable {
        return recordMetric(pjp);
    }

    /**
     * Records the execution metric for a method
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
     * Determines which layer the method belongs to based on package name
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

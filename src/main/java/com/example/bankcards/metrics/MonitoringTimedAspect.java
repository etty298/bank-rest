package com.example.bankcards.metrics;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.LongTaskTimer.Sample;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

import static io.micrometer.core.aop.TimedAspect.EXCEPTION_TAG;

/**
 * Aspect for monitoring method execution time using {@link MonitoringTimed} annotation.
 * <p>
 * This aspect intercepts methods annotated with {@code @MonitoringTimed} and creates
 * timing metrics using Micrometer's {@link Timer} API. It provides fine-grained control
 * over individual method monitoring with support for custom metric names, percentiles,
 * histograms, and long-running task tracking.
 * <p>
 * Key features:
 * <ul>
 *   <li>Method-level monitoring with {@code @MonitoringTimed} annotation</li>
 *   <li>Customizable metric names and descriptions</li>
 *   <li>Support for percentiles and histogram publishing</li>
 *   <li>Long-running task support via {@link LongTaskTimer}</li>
 *   <li>Exception tracking in metric tags</li>
 *   <li>Optional method execution logging</li>
 * </ul>
 * <p>
 * <strong>Usage example:</strong>
 * <pre>
 * {@code @MonitoringTimed}
 * public void myMethod() {
 *     // Method implementation
 * }
 *
 * {@code @MonitoringTimed(value = "custom_metric", histogram = true)}
 * public void anotherMethod() {
 *     // Method implementation
 * }
 * </pre>
 * <p>
 * Metrics are published to Prometheus with the configured name (default: {@code method_timed})
 * and include tags for class, method, and exception type.
 * <p>
 * This implementation is based on Micrometer's {@link io.micrometer.core.aop.TimedAspect}.
 *
 * @see MonitoringTimed
 * @see Timer
 * @see LongTaskTimer
 * @see PackageMonitoringAspect
 */
@Aspect
@Slf4j
@Component
public class MonitoringTimedAspect {

    /**
     * Default metric name used when no custom name is specified in the annotation.
     * <p>
     * Value: {@code method_timed}
     */
    public static final String DEFAULT_METRIC_NAME = "method_timed";
    private final MeterRegistry registry;
    private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint;

    /**
     * Creates a new MonitoringTimedAspect with the specified meter registry.
     * <p>
     * The aspect automatically configures default tags to include class and method names
     * for all recorded metrics.
     *
     * @param registry the meter registry to use for publishing metrics
     */
    public MonitoringTimedAspect(MeterRegistry registry) {
        this.registry = registry;
        this.tagsBasedOnJoinPoint = pjp ->
                Tags.of(
                        "class",
                        pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                        "method",
                        pjp.getStaticPart().getSignature().getName());
    }

    /**
     * Processes the method execution with a standard timer.
     * <p>
     * This method starts a timer before method execution, records the result,
     * and stops the timer afterward. Exception information is captured in the
     * metric tags.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @param timed the MonitoringTimed annotation with configuration
     * @param metricName the name to use for the metric
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    private Object processWithTimer(ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName)
            throws Throwable {
        Timer.Sample sample = Timer.start(registry);
        String exceptionClass = "none";

        try {
            return pjp.proceed();
        } catch (Exception ex) {
            exceptionClass = ex.getClass().getSimpleName();
            throw ex;
        } finally {
            try {
                sample.stop(
                        Timer.builder(metricName)
                                .description(timed.description().isEmpty() ? null : timed.description())
                                .tags(timed.extraTags())
                                .tags(EXCEPTION_TAG, exceptionClass)
                                .tags(tagsBasedOnJoinPoint.apply(pjp))
                                .publishPercentileHistogram(timed.histogram())
                                .publishPercentiles(timed.percentiles().length == 0 ? null : timed.percentiles())
                                .register(registry));
            } catch (Exception e) {
                // ignoring on purpose
            }
        }
    }

    /**
     * Processes the method execution with a long task timer.
     * <p>
     * Long task timers are designed for tracking long-running operations that may
     * take minutes or hours. Unlike standard timers, they report duration while
     * the task is still running.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @param timed the MonitoringTimed annotation with configuration
     * @param metricName the name to use for the metric
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     * @see LongTaskTimer
     */
    private Object processWithLongTaskTimer(
            ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName) throws Throwable {

        Optional<Sample> sample = buildLongTaskTimer(pjp, timed, metricName).map(LongTaskTimer::start);

        try {
            return pjp.proceed();
        } finally {
            try {
                sample.ifPresent(LongTaskTimer.Sample::stop);
            } catch (Exception e) {
                // ignoring on purpose
            }
        }
    }

    /**
     * Builds a long task timer with error handling.
     * <p>
     * This method creates a {@link LongTaskTimer} safely, ensuring that any exceptions
     * during timer creation do not disrupt the application flow. If timer creation fails,
     * an empty Optional is returned.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @param timed the MonitoringTimed annotation with configuration
     * @param metricName the name to use for the metric
     * @return an Optional containing the LongTaskTimer, or empty if creation failed
     */
    private Optional<LongTaskTimer> buildLongTaskTimer(
            ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName) {
        try {
            return Optional.of(
                    LongTaskTimer.builder(metricName)
                            .description(timed.description().isEmpty() ? null : timed.description())
                            .tags(timed.extraTags())
                            .tags(tagsBasedOnJoinPoint.apply(pjp))
                            .register(registry));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Times the method execution using either standard or long task timer.
     * <p>
     * This method delegates to either {@link #processWithTimer} or
     * {@link #processWithLongTaskTimer} based on the annotation configuration.
     * If logging is enabled in the annotation, it also logs the method result.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @param timed the MonitoringTimed annotation with configuration
     * @param metricName the name to use for the metric
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    private Object timeThisMethod(ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName)
            throws Throwable {
        Object response;
        if (!timed.longTask()) {
            response = processWithTimer(pjp, timed, metricName);
        } else {
            response = processWithLongTaskTimer(pjp, timed, metricName);
        }
        if (timed.loggingEnabled()) {
            log.info("{} {}", response, pjp.getArgs());
        }
        return response;
    }

    /**
     * Generates the metric name from the annotation or uses the default.
     * <p>
     * If a custom name is specified in the {@code @MonitoringTimed} annotation,
     * it is used. Otherwise, the default name {@code method_timed} is returned.
     *
     * @param pjp the proceeding join point (unused but kept for consistency)
     * @param timed the MonitoringTimed annotation with configuration
     * @return the metric name to use
     */
    private String generateMetricName(ProceedingJoinPoint pjp, MonitoringTimed timed) {
        if (!timed.value().isEmpty()) {
            return timed.value();
        }
        return DEFAULT_METRIC_NAME;
    }

    /**
     * Around advice that intercepts methods annotated with {@code @MonitoringTimed}.
     * <p>
     * This is the main entry point for the aspect. It extracts the annotation,
     * generates the metric name, and delegates to {@link #timeThisMethod} for
     * actual timing.
     * <p>
     * If the annotation is not found (which should not happen), a warning is logged
     * and the method proceeds without timing.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("@annotation(com.example.bankcards.metrics.MonitoringTimed)")
    public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        MonitoringTimed timed = method.getAnnotation(MonitoringTimed.class);
        if (timed == null) {
            log.warn("MonitoringTimed annotation not found on method: {}", method.getName());
            return pjp.proceed();
        }
        final String metricName = generateMetricName(pjp, timed);
        log.debug("Timing method: {} with metric name: {}", method.getName(), metricName);
        return timeThisMethod(pjp, timed, metricName);
    }

    /**
     * Public method for programmatic method timing.
     * <p>
     * This method can be called directly to time a method execution when you have
     * access to the {@link ProceedingJoinPoint} and {@link MonitoringTimed} annotation.
     * <p>
     * This is primarily used internally but exposed for advanced use cases.
     *
     * @param pjp the proceeding join point representing the intercepted method
     * @param timed the MonitoringTimed annotation with configuration
     * @return the result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    public Object timeThisMethod(ProceedingJoinPoint pjp, MonitoringTimed timed) throws Throwable {
        final String metricName = generateMetricName(pjp, timed);
        return timeThisMethod(pjp, timed, metricName);
    }
}
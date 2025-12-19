package com.example.bankcards.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MonitoringTimedAspect - Unit Tests")
class MonitoringTimedAspectTest {

    private MeterRegistry meterRegistry;
    private MonitoringTimedAspect aspect;
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setup() {
        meterRegistry = new SimpleMeterRegistry();
        aspect = new MonitoringTimedAspect(meterRegistry);
        joinPoint = mock(ProceedingJoinPoint.class);
    }

    @Test
    @DisplayName("Should create timer metric when method is annotated with @MonitoringTimed")
    void shouldCreateTimerMetricForAnnotatedMethod() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("annotatedMethod");
        MethodSignature signature = mock(MethodSignature.class);
        Signature staticSignature = mock(Signature.class);
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(mock(org.aspectj.lang.JoinPoint.StaticPart.class));
        when(joinPoint.getStaticPart().getSignature()).thenReturn(staticSignature);
        when(staticSignature.getDeclaringTypeName()).thenReturn("com.example.TestService");
        when(staticSignature.getName()).thenReturn("annotatedMethod");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.timedMethod(joinPoint);

        // Then
        assertEquals("result", result);
        
        Timer timer = meterRegistry.find("method_timed")
                .tag("class", "com.example.TestService")
                .tag("method", "annotatedMethod")
                .timer();
        
        assertNotNull(timer, "Timer metric should be created");
        assertEquals(1, timer.count(), "Timer should record one execution");
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS) > 0, "Timer should record execution time");
    }

    @Test
    @DisplayName("Should use custom metric name when specified in annotation")
    void shouldUseCustomMetricName() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("methodWithCustomName");
        MethodSignature signature = mock(MethodSignature.class);
        Signature staticSignature = mock(Signature.class);
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(mock(org.aspectj.lang.JoinPoint.StaticPart.class));
        when(joinPoint.getStaticPart().getSignature()).thenReturn(staticSignature);
        when(staticSignature.getDeclaringTypeName()).thenReturn("com.example.TestService");
        when(staticSignature.getName()).thenReturn("methodWithCustomName");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("result");

        // When
        aspect.timedMethod(joinPoint);

        // Then
        Timer timer = meterRegistry.find("custom_metric_name").timer();
        assertNotNull(timer, "Timer with custom name should be created");
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("Should record exception in metric tags")
    void shouldRecordExceptionInMetricTags() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("annotatedMethod");
        MethodSignature signature = mock(MethodSignature.class);
        Signature staticSignature = mock(Signature.class);
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(mock(org.aspectj.lang.JoinPoint.StaticPart.class));
        when(joinPoint.getStaticPart().getSignature()).thenReturn(staticSignature);
        when(staticSignature.getDeclaringTypeName()).thenReturn("com.example.TestService");
        when(staticSignature.getName()).thenReturn("annotatedMethod");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        // When/Then
        assertThrows(RuntimeException.class, () -> aspect.timedMethod(joinPoint));
        
        Timer timer = meterRegistry.find("method_timed")
                .tag("exception", "RuntimeException")
                .timer();
        
        assertNotNull(timer, "Timer should record exception");
        assertEquals(1, timer.count());
    }

    // Test service class with annotated methods
    public static class TestService {
        
        @MonitoringTimed
        public void annotatedMethod() {
            // Test method
        }
        
        @MonitoringTimed(value = "custom_metric_name")
        public void methodWithCustomName() {
            // Test method
        }
    }
}

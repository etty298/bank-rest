package com.example.bankcards.metrics;

import com.example.bankcards.config.MonitoringConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PackageMonitoringAspect - Unit Tests")
class PackageMonitoringAspectTest {

    private MeterRegistry meterRegistry;
    private MonitoringConfig monitoringConfig;
    private PackageMonitoringAspect aspect;
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setup() {
        meterRegistry = new SimpleMeterRegistry();
        monitoringConfig = new MonitoringConfig();
        monitoringConfig.setEnabled(true);
        monitoringConfig.setIncludeRepositoryLayer(false);
        monitoringConfig.setMinExecutionTimeMs(0);
        
        aspect = new PackageMonitoringAspect(meterRegistry, monitoringConfig);
        joinPoint = mock(ProceedingJoinPoint.class);
    }

    @Test
    @DisplayName("Should create metric for service layer method")
    void shouldCreateMetricForServiceMethod() throws Throwable {
        // Given
        Signature signature = mock(Signature.class);
        org.aspectj.lang.JoinPoint.StaticPart staticPart = mock(org.aspectj.lang.JoinPoint.StaticPart.class);
        
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.bankcards.service.AuthService");
        when(signature.getName()).thenReturn("login");
        when(joinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.monitorPackageMethod(joinPoint);

        // Then
        assertEquals("result", result);
        
        Timer timer = meterRegistry.find("method_execution")
                .tag("class", "com.example.bankcards.service.AuthService")
                .tag("method", "login")
                .tag("layer", "service")
                .timer();
        
        assertNotNull(timer, "Timer metric should be created for service method");
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("Should create metric for controller layer method")
    void shouldCreateMetricForControllerMethod() throws Throwable {
        // Given
        Signature signature = mock(Signature.class);
        org.aspectj.lang.JoinPoint.StaticPart staticPart = mock(org.aspectj.lang.JoinPoint.StaticPart.class);
        
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.bankcards.controller.AuthController");
        when(signature.getName()).thenReturn("login");
        when(joinPoint.proceed()).thenReturn("result");

        // When
        aspect.monitorPackageMethod(joinPoint);

        // Then
        Timer timer = meterRegistry.find("method_execution")
                .tag("layer", "controller")
                .timer();
        
        assertNotNull(timer, "Timer metric should be created for controller method");
    }

    @Test
    @DisplayName("Should not create metric for repository when disabled")
    void shouldNotCreateMetricForRepositoryWhenDisabled() throws Throwable {
        // Given
        monitoringConfig.setIncludeRepositoryLayer(false);
        Signature signature = mock(Signature.class);
        org.aspectj.lang.JoinPoint.StaticPart staticPart = mock(org.aspectj.lang.JoinPoint.StaticPart.class);
        
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.bankcards.repository.UserRepository");
        when(signature.getName()).thenReturn("findByUsername");
        when(joinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.monitorRepositoryMethod(joinPoint);

        // Then
        assertEquals("result", result);
        verify(joinPoint, times(1)).proceed();
        
        Timer timer = meterRegistry.find("method_execution")
                .tag("layer", "repository")
                .timer();
        
        assertNull(timer, "Timer metric should NOT be created for repository when disabled");
    }

    @Test
    @DisplayName("Should create metric for repository when enabled")
    void shouldCreateMetricForRepositoryWhenEnabled() throws Throwable {
        // Given
        monitoringConfig.setIncludeRepositoryLayer(true);
        Signature signature = mock(Signature.class);
        org.aspectj.lang.JoinPoint.StaticPart staticPart = mock(org.aspectj.lang.JoinPoint.StaticPart.class);
        
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.bankcards.repository.UserRepository");
        when(signature.getName()).thenReturn("findByUsername");
        when(joinPoint.proceed()).thenReturn("result");

        // When
        aspect.monitorRepositoryMethod(joinPoint);

        // Then
        Timer timer = meterRegistry.find("method_execution")
                .tag("layer", "repository")
                .timer();
        
        assertNotNull(timer, "Timer metric should be created for repository when enabled");
    }

    @Test
    @DisplayName("Should record exception in metric tags")
    void shouldRecordExceptionInMetricTags() throws Throwable {
        // Given
        Signature signature = mock(Signature.class);
        org.aspectj.lang.JoinPoint.StaticPart staticPart = mock(org.aspectj.lang.JoinPoint.StaticPart.class);
        
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.bankcards.service.AuthService");
        when(signature.getName()).thenReturn("login");
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("Test exception"));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> aspect.monitorPackageMethod(joinPoint));
        
        Timer timer = meterRegistry.find("method_execution")
                .tag("exception", "IllegalArgumentException")
                .timer();
        
        assertNotNull(timer, "Timer should record exception");
        assertEquals(1, timer.count());
    }

    @Test
    @DisplayName("Should respect minimum execution time threshold")
    void shouldRespectMinimumExecutionTimeThreshold() throws Throwable {
        // Given
        monitoringConfig.setMinExecutionTimeMs(10000); // 10 seconds
        Signature signature = mock(Signature.class);
        org.aspectj.lang.JoinPoint.StaticPart staticPart = mock(org.aspectj.lang.JoinPoint.StaticPart.class);
        
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.bankcards.service.AuthService");
        when(signature.getName()).thenReturn("fastMethod");
        when(joinPoint.proceed()).thenReturn("result");

        // When
        aspect.monitorPackageMethod(joinPoint);

        // Then - fast method should not create metric due to threshold
        Timer timer = meterRegistry.find("method_execution")
                .tag("method", "fastMethod")
                .timer();
        
        assertNull(timer, "Timer should not be created for methods faster than threshold");
    }
}

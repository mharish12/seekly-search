package com.h12.seekly.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Spring Boot configuration properties for PostgreSQL search engine.
 * Supports externalized configuration via application.yml/properties.
 */
@Data
@Slf4j
@Validated
@Configuration
@ConfigurationProperties(prefix = "seekly.search.postgres")
public class SpringPostgresSearchConfig {

    /**
     * Database connection URL
     */
    @NotBlank(message = "Database URL is required")
    private String dbUrl;

    /**
     * Database username
     */
    @NotBlank(message = "Database username is required")
    private String dbUsername;

    /**
     * Database password
     */
    @NotBlank(message = "Database password is required")
    private String dbPassword;

    /**
     * Lucene index path
     */
    @NotBlank(message = "Lucene index path is required")
    private String luceneIndexPath = "./index";

    /**
     * Entity type for this search engine
     */
    @NotBlank(message = "Entity type is required")
    private String entityType;

    /**
     * Maximum connection pool size
     */
    @Min(value = 1, message = "Max pool size must be at least 1")
    @Max(value = 100, message = "Max pool size cannot exceed 100")
    private int maxPoolSize = 20;

    /**
     * Minimum idle connections
     */
    @Min(value = 0, message = "Min idle cannot be negative")
    private int minIdle = 5;

    /**
     * Connection timeout in milliseconds
     */
    @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
    private long connectionTimeout = 30000;

    /**
     * Idle timeout in milliseconds
     */
    @Min(value = 10000, message = "Idle timeout must be at least 10000ms")
    private long idleTimeout = 600000;

    /**
     * Maximum lifetime in milliseconds
     */
    @Min(value = 30000, message = "Max lifetime must be at least 30000ms")
    private long maxLifetime = 1800000;

    /**
     * Whether to enable metrics tracking
     */
    private boolean enableMetrics = true;

    /**
     * Whether to enable query performance tracking
     */
    private boolean enableQueryPerformance = true;

    /**
     * Auto-optimize index after this many operations
     */
    @Min(value = 100, message = "Auto optimize threshold must be at least 100")
    private long autoOptimizeThreshold = 1000;

    /**
     * Whether to enable auto-optimization
     */
    private boolean enableAutoOptimization = true;

    /**
     * Prometheus metrics configuration
     */
    private PrometheusConfig prometheus = new PrometheusConfig();

    /**
     * Health check configuration
     */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();

    /**
     * Prometheus metrics configuration
     */
    @Data
    public static class PrometheusConfig {
        /**
         * Whether to enable Prometheus metrics
         */
        private boolean enabled = true;

        /**
         * Metrics prefix for all search metrics
         */
        private String metricsPrefix = "seekly_search";

        /**
         * Whether to include JVM metrics
         */
        private boolean includeJvmMetrics = true;

        /**
         * Whether to include system metrics
         */
        private boolean includeSystemMetrics = true;

        /**
         * Metrics collection interval in seconds
         */
        @Min(value = 1, message = "Collection interval must be at least 1 second")
        private int collectionIntervalSeconds = 15;
    }

    /**
     * Health check configuration
     */
    @Data
    public static class HealthCheckConfig {
        /**
         * Whether to enable health checks
         */
        private boolean enabled = true;

        /**
         * Health check timeout in milliseconds
         */
        @Min(value = 1000, message = "Health check timeout must be at least 1000ms")
        private long timeoutMs = 5000;

        /**
         * Health check interval in milliseconds
         */
        @Min(value = 5000, message = "Health check interval must be at least 5000ms")
        private long intervalMs = 30000;
    }

    /**
     * Convert to PostgresSearchConfig for backward compatibility
     */
    public PostgresSearchConfig toPostgresSearchConfig() {
        return PostgresSearchConfig.builder()
                .dbUrl(dbUrl)
                .dbUsername(dbUsername)
                .dbPassword(dbPassword)
                .luceneIndexPath(luceneIndexPath)
                .entityType(entityType)
                .maxPoolSize(maxPoolSize)
                .minIdle(minIdle)
                .connectionTimeout(connectionTimeout)
                .idleTimeout(idleTimeout)
                .maxLifetime(maxLifetime)
                .enableMetrics(enableMetrics)
                .enableQueryPerformance(enableQueryPerformance)
                .autoOptimizeThreshold(autoOptimizeThreshold)
                .enableAutoOptimization(enableAutoOptimization)
                .build();
    }
}
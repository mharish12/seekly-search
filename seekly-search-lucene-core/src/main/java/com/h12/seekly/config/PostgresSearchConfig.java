package com.h12.seekly.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for PostgreSQL-based search engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgresSearchConfig {

    /**
     * PostgreSQL database URL
     */
    private String dbUrl;

    /**
     * Database username
     */
    private String dbUsername;

    /**
     * Database password
     */
    private String dbPassword;

    /**
     * Lucene index path
     */
    private String luceneIndexPath;

    /**
     * Entity type for this search engine
     */
    private String entityType;

    /**
     * Maximum connection pool size
     */
    @Builder.Default
    private int maxPoolSize = 20;

    /**
     * Minimum idle connections
     */
    @Builder.Default
    private int minIdle = 5;

    /**
     * Connection timeout in milliseconds
     */
    @Builder.Default
    private long connectionTimeout = 30000;

    /**
     * Idle timeout in milliseconds
     */
    @Builder.Default
    private long idleTimeout = 600000;

    /**
     * Maximum lifetime in milliseconds
     */
    @Builder.Default
    private long maxLifetime = 1800000;

    /**
     * Whether to enable metrics tracking
     */
    @Builder.Default
    private boolean enableMetrics = true;

    /**
     * Whether to enable query performance tracking
     */
    @Builder.Default
    private boolean enableQueryPerformance = true;

    /**
     * Auto-optimize index after this many operations
     */
    @Builder.Default
    private long autoOptimizeThreshold = 1000;

    /**
     * Whether to enable auto-optimization
     */
    @Builder.Default
    private boolean enableAutoOptimization = true;
}
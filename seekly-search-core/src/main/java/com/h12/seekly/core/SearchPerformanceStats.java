package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Overall performance statistics for the search engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPerformanceStats {

    /**
     * Total number of searches executed
     */
    private long totalSearches;

    /**
     * Total number of successful searches
     */
    private long successfulSearches;

    /**
     * Total number of failed searches
     */
    private long failedSearches;

    /**
     * Average search execution time in milliseconds
     */
    private double averageSearchTimeMs;

    /**
     * Median search execution time in milliseconds
     */
    private double medianSearchTimeMs;

    /**
     * 95th percentile search execution time in milliseconds
     */
    private double p95SearchTimeMs;

    /**
     * 99th percentile search execution time in milliseconds
     */
    private double p99SearchTimeMs;

    /**
     * Maximum search execution time in milliseconds
     */
    private long maxSearchTimeMs;

    /**
     * Minimum search execution time in milliseconds
     */
    private long minSearchTimeMs;

    /**
     * Total number of results returned
     */
    private long totalResultsReturned;

    /**
     * Average results per search
     */
    private double averageResultsPerSearch;

    /**
     * Total number of zero-result searches
     */
    private long zeroResultSearches;

    /**
     * Success rate percentage
     */
    private double successRate;

    /**
     * Average index size
     */
    private long averageIndexSize;

    /**
     * Current index size
     */
    private long currentIndexSize;

    /**
     * Number of indexed documents
     */
    private long indexedDocuments;

    /**
     * Number of deleted documents
     */
    private long deletedDocuments;

    /**
     * Number of updated documents
     */
    private long updatedDocuments;

    /**
     * Index optimization count
     */
    private long indexOptimizations;

    /**
     * Last index optimization time
     */
    private LocalDateTime lastOptimization;

    /**
     * Memory usage in bytes
     */
    private long memoryUsageBytes;

    /**
     * Disk usage in bytes
     */
    private long diskUsageBytes;

    /**
     * CPU usage percentage
     */
    private double cpuUsagePercentage;

    /**
     * Uptime in milliseconds
     */
    private long uptimeMs;

    /**
     * Start time of the search engine
     */
    private LocalDateTime startTime;

    /**
     * Last activity time
     */
    private LocalDateTime lastActivity;
}
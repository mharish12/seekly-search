package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Performance metrics for a specific query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryPerformance {

    /**
     * The query string
     */
    private String query;

    /**
     * Entity type being searched
     */
    private String entityType;

    /**
     * Number of times this query was executed
     */
    private long executionCount;

    /**
     * Average execution time in milliseconds
     */
    private double averageExecutionTimeMs;

    /**
     * Total execution time in milliseconds
     */
    private long totalExecutionTimeMs;

    /**
     * Average number of results returned
     */
    private double averageResultsReturned;

    /**
     * Total number of results returned
     */
    private long totalResultsReturned;

    /**
     * Number of zero-result executions
     */
    private long zeroResultCount;

    /**
     * Click-through rate for this query
     */
    private double clickThroughRate;

    /**
     * Average time users spend on results from this query
     */
    private long averageTimeOnResults;

    /**
     * First time this query was executed
     */
    private LocalDateTime firstExecuted;

    /**
     * Last time this query was executed
     */
    private LocalDateTime lastExecuted;

    /**
     * Success rate of this query
     */
    private double successRate;

    /**
     * Error count for this query
     */
    private long errorCount;
}
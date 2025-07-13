package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive metrics for search operations including performance, ranking,
 * and analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetrics {

    /**
     * Query that was executed
     */
    private String query;

    /**
     * Entity type being searched
     */
    private String entityType;

    /**
     * Total number of hits found
     */
    private long totalHits;

    /**
     * Number of results returned
     */
    private int resultsReturned;

    /**
     * Search execution time in milliseconds
     */
    private long searchTimeMs;

    /**
     * Index size at time of search
     */
    private long indexSize;

    /**
     * Timestamp when search was executed
     */
    private LocalDateTime timestamp;

    /**
     * User session ID (if available)
     */
    private String sessionId;

    /**
     * User ID (if available)
     */
    private String userId;

    /**
     * Search filters applied
     */
    private Map<String, Object> filters;

    /**
     * Search facets and their counts
     */
    private Map<String, Long> facets;

    /**
     * Top 10 results with their ranks
     */
    private List<RankedResult> top10Results;

    /**
     * Click-through rate for this query (if available)
     */
    private double clickThroughRate;

    /**
     * Average time spent on results from this query
     */
    private long averageTimeOnResults;

    /**
     * Number of times this query has been executed
     */
    private long queryFrequency;

    /**
     * Whether this is a zero-result query
     */
    private boolean zeroResults;

    /**
     * Query suggestions provided (if any)
     */
    private List<String> suggestions;

    /**
     * Error message if search failed
     */
    private String errorMessage;

    /**
     * Additional custom metrics
     */
    private Map<String, Object> customMetrics;

    /**
     * Represents a ranked result with basic information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankedResult {
        private String entityId;
        private String entityType;
        private int rank;
        private double score;
        private boolean clicked;
    }
}
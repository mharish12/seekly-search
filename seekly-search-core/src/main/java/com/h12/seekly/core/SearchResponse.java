package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response wrapper for search operations containing results, metadata, and
 * metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T extends SearchableEntity> {

    /**
     * List of search results
     */
    private List<SearchResult<T>> results;

    /**
     * Total number of hits found
     */
    private long totalHits;

    /**
     * Search execution time in milliseconds
     */
    private long searchTimeMs;

    /**
     * Query that was executed
     */
    private String query;

    /**
     * Entity type that was searched
     */
    private String entityType;

    /**
     * Search options used
     */
    private SearchOptions options;

    /**
     * Filters applied to the search
     */
    private Map<String, Object> filters;

    /**
     * Facets and their counts
     */
    private Map<String, Long> facets;

    /**
     * Search suggestions for the query
     */
    private List<String> suggestions;

    /**
     * Whether the search was successful
     */
    private boolean success;

    /**
     * Error message if search failed
     */
    private String errorMessage;

    /**
     * Timestamp when search was executed
     */
    private LocalDateTime timestamp;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
}
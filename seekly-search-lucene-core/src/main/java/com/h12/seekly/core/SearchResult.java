package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a search result with comprehensive metrics and ranking
 * information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult<T extends SearchableEntity> {

    /**
     * The searchable entity
     */
    private T entity;

    /**
     * Relevance score (0.0 to 1.0)
     */
    private double score;

    /**
     * Rank position in search results (1-based)
     */
    private int rank;

    /**
     * Whether this result was in top 10
     */
    private boolean inTop10;

    /**
     * Whether this result was in top 5
     */
    private boolean inTop5;

    /**
     * Whether this result was in top 3
     */
    private boolean inTop3;

    /**
     * Whether this result was the first result
     */
    private boolean isFirst;

    /**
     * Highlighted snippets from the search
     */
    private List<String> highlights;

    /**
     * Additional metadata about the search result
     */
    private Map<String, Object> metadata;

    /**
     * Timestamp when this result was generated
     */
    private LocalDateTime timestamp;

    /**
     * Query that produced this result
     */
    private String query;

    /**
     * Total number of results for this query
     */
    private long totalHits;

    /**
     * Time taken to generate this result (in milliseconds)
     */
    private long searchTimeMs;
}
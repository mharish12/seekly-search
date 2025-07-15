package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration options for search operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchOptions {

    /**
     * Maximum number of results to return
     */
    @Builder.Default
    private int maxResults = 20;

    /**
     * Offset for pagination
     */
    @Builder.Default
    private int offset = 0;

    /**
     * Whether to include highlights in results
     */
    @Builder.Default
    private boolean includeHighlights = true;

    /**
     * Maximum number of highlights per result
     */
    @Builder.Default
    private int maxHighlights = 3;

    /**
     * Fields to search in (if null, searches all fields)
     */
    private List<String> searchFields;

    /**
     * Fields to return in results (if null, returns all fields)
     */
    private List<String> returnFields;

    /**
     * Minimum relevance score threshold
     */
    @Builder.Default
    private double minScore = 0.0;

    /**
     * Whether to enable fuzzy matching
     */
    @Builder.Default
    private boolean fuzzyMatching = false;

    /**
     * Fuzzy matching edit distance (0-2)
     */
    @Builder.Default
    private int fuzzyDistance = 1;

    /**
     * Whether to enable wildcard matching
     */
    @Builder.Default
    private boolean wildcardMatching = false;

    /**
     * Whether to enable phrase matching
     */
    @Builder.Default
    private boolean phraseMatching = true;

    /**
     * Boost factor for exact matches
     */
    @Builder.Default
    private float exactMatchBoost = 2.0f;

    /**
     * Boost factor for phrase matches
     */
    @Builder.Default
    private float phraseMatchBoost = 1.5f;

    /**
     * Whether to include facets in response
     */
    @Builder.Default
    private boolean includeFacets = false;

    /**
     * Facet fields to include
     */
    private List<String> facetFields;

    /**
     * Whether to include suggestions in response
     */
    @Builder.Default
    private boolean includeSuggestions = false;

    /**
     * Maximum number of suggestions to return
     */
    @Builder.Default
    private int maxSuggestions = 5;

    /**
     * Whether to track metrics for this search
     */
    @Builder.Default
    private boolean trackMetrics = true;

    /**
     * User session ID for metrics tracking
     */
    private String sessionId;

    /**
     * User ID for metrics tracking
     */
    private String userId;
}
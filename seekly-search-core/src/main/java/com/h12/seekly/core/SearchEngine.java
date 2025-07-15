package com.h12.seekly.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main interface for the Seekly search engine framework.
 * Provides core search functionality with comprehensive metrics tracking.
 */
public interface SearchEngine<T extends SearchableEntity> {

    /**
     * Index a single entity for search
     */
    void index(T entity);

    /**
     * Index multiple entities for search
     */
    void indexBatch(List<T> entities);

    /**
     * Remove an entity from the search index
     */
    void removeFromIndex(String entityId);

    /**
     * Update an entity in the search index
     */
    void updateIndex(T entity);

    /**
     * Search for entities with basic query
     */
    SearchResponse<T> search(String query);

    /**
     * Search for entities with advanced options
     */
    SearchResponse<T> search(String query, SearchOptions options);

    /**
     * Search for entities with filters
     */
    SearchResponse<T> search(String query, Map<String, Object> filters);

    /**
     * Search for entities with filters and options
     */
    SearchResponse<T> search(String query, Map<String, Object> filters, SearchOptions options);

    /**
     * Get search suggestions for autocomplete
     */
    List<String> getSuggestions(String partialQuery, int maxSuggestions);

    /**
     * Get search metrics for a specific query
     */
    Optional<SearchMetric> getSearchMetrics(String query);

    /**
     * Get aggregated search metrics for a time period
     */
    List<SearchMetric> getSearchMetrics(String entityType, LocalDateTime from, LocalDateTime to);

    /**
     * Get top performing queries
     */
    List<QueryPerformance> getTopQueries(String entityType, int limit);

    /**
     * Get zero-result queries
     */
    List<String> getZeroResultQueries(String entityType, int limit);

    /**
     * Get search performance statistics
     */
    SearchPerformanceStats getPerformanceStats();

    /**
     * Clear the entire search index
     */
    void clearIndex();

    /**
     * Get index statistics
     */
    IndexStats getIndexStats();

    /**
     * Optimize the search index
     */
    void optimizeIndex();

    /**
     * Check if the search engine is healthy
     */
    boolean isHealthy();
}
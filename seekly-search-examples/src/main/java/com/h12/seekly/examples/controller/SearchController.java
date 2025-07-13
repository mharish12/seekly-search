package com.h12.seekly.examples.controller;

import com.h12.seekly.core.*;
import com.h12.seekly.examples.entities.Product;
import com.h12.seekly.metrics.SearchMetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Seekly search engine.
 * Provides HTTP endpoints for search operations with Prometheus metrics.
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchEngine<Product> searchEngine;
    private final SearchMetricsCollector metricsCollector;

    /**
     * Basic search endpoint
     */
    @GetMapping("/products")
    public ResponseEntity<SearchResponse<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int maxResults,
            @RequestParam(defaultValue = "0") int offset) {

        long startTime = System.currentTimeMillis();

        try {
            SearchOptions options = SearchOptions.builder()
                    .maxResults(maxResults)
                    .offset(offset)
                    .includeHighlights(true)
                    .trackMetrics(true)
                    .build();

            SearchResponse<Product> response = searchEngine.search(query, options);

            // Record metrics
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordSearch(
                    "product",
                    query,
                    duration,
                    response.getResults().size(),
                    0.0, // TODO: Calculate average score
                    response.isSuccess(),
                    response.getTotalHits() == 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Search failed for query: {}", query, e);

            // Record failed search metrics
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordSearch(
                    "product",
                    query,
                    duration,
                    0,
                    0.0,
                    false,
                    true);

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Advanced search with filters
     */
    @PostMapping("/products/advanced")
    public ResponseEntity<SearchResponse<Product>> advancedSearch(
            @RequestBody AdvancedSearchRequest request) {

        long startTime = System.currentTimeMillis();

        try {
            SearchOptions options = SearchOptions.builder()
                    .maxResults(request.getMaxResults())
                    .offset(request.getOffset())
                    .includeHighlights(request.isIncludeHighlights())
                    .fuzzyMatching(request.isFuzzyMatching())
                    .wildcardMatching(request.isWildcardMatching())
                    .phraseMatching(request.isPhraseMatching())
                    .includeFacets(request.isIncludeFacets())
                    .includeSuggestions(request.isIncludeSuggestions())
                    .trackMetrics(true)
                    .sessionId(request.getSessionId())
                    .userId(request.getUserId())
                    .build();

            SearchResponse<Product> response = searchEngine.search(
                    request.getQuery(),
                    request.getFilters(),
                    options);

            // Record metrics
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordSearch(
                    "product",
                    request.getQuery(),
                    duration,
                    response.getResults().size(),
                    0.0,
                    response.isSuccess(),
                    response.getTotalHits() == 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Advanced search failed for query: {}", request.getQuery(), e);

            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordSearch(
                    "product",
                    request.getQuery(),
                    duration,
                    0,
                    0.0,
                    false,
                    true);

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get search suggestions
     */
    @GetMapping("/products/suggestions")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam String partialQuery,
            @RequestParam(defaultValue = "5") int maxSuggestions) {

        try {
            List<String> suggestions = searchEngine.getSuggestions(partialQuery, maxSuggestions);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Failed to get suggestions for: {}", partialQuery, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Index a product
     */
    @PostMapping("/products")
    public ResponseEntity<Void> indexProduct(@RequestBody Product product) {

        long startTime = System.currentTimeMillis();

        try {
            searchEngine.index(product);

            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordIndexing("product", 1, duration);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to index product: {}", product.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Batch index products
     */
    @PostMapping("/products/batch")
    public ResponseEntity<Void> batchIndexProducts(@RequestBody List<Product> products) {

        long startTime = System.currentTimeMillis();

        try {
            searchEngine.indexBatch(products);

            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordIndexing("product", products.size(), duration);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to batch index {} products", products.size(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update a product
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable String id, @RequestBody Product product) {

        try {
            searchEngine.updateIndex(product);
            metricsCollector.recordUpdate("product", 1);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update product: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a product
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {

        try {
            searchEngine.removeFromIndex(id);
            metricsCollector.recordDeletion("product", 1);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete product: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get search performance statistics
     */
    @GetMapping("/stats/performance")
    public ResponseEntity<SearchPerformanceStats> getPerformanceStats() {

        try {
            SearchPerformanceStats stats = searchEngine.getPerformanceStats();
            metricsCollector.updatePerformanceStats(stats);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get performance stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get index statistics
     */
    @GetMapping("/stats/index")
    public ResponseEntity<IndexStats> getIndexStats() {

        try {
            IndexStats stats = searchEngine.getIndexStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get index stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top performing queries
     */
    @GetMapping("/stats/top-queries")
    public ResponseEntity<List<QueryPerformance>> getTopQueries(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<QueryPerformance> topQueries = searchEngine.getTopQueries("product", limit);
            return ResponseEntity.ok(topQueries);
        } catch (Exception e) {
            log.error("Failed to get top queries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get zero-result queries
     */
    @GetMapping("/stats/zero-result-queries")
    public ResponseEntity<List<String>> getZeroResultQueries(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<String> zeroResultQueries = searchEngine.getZeroResultQueries("product", limit);
            return ResponseEntity.ok(zeroResultQueries);
        } catch (Exception e) {
            log.error("Failed to get zero-result queries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Optimize the search index
     */
    @PostMapping("/optimize")
    public ResponseEntity<Void> optimizeIndex() {

        long startTime = System.currentTimeMillis();

        try {
            searchEngine.optimizeIndex();

            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordOptimization(duration);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to optimize index", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {

        try {
            boolean isHealthy = searchEngine.isHealthy();
            IndexStats stats = searchEngine.getIndexStats();

            Map<String, Object> health = Map.of(
                    "status", isHealthy ? "UP" : "DOWN",
                    "totalDocuments", stats.getTotalDocuments(),
                    "indexHealth", stats.getHealth().toString(),
                    "timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * Get metrics snapshot
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {

        try {
            Map<String, Object> metrics = metricsCollector.getMetricsSnapshot();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Failed to get metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request model for advanced search
     */
    public static class AdvancedSearchRequest {
        private String query;
        private Map<String, Object> filters;
        private int maxResults = 20;
        private int offset = 0;
        private boolean includeHighlights = true;
        private boolean fuzzyMatching = false;
        private boolean wildcardMatching = false;
        private boolean phraseMatching = true;
        private boolean includeFacets = false;
        private boolean includeSuggestions = false;
        private String sessionId;
        private String userId;

        // Getters and setters
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public boolean isIncludeHighlights() {
            return includeHighlights;
        }

        public void setIncludeHighlights(boolean includeHighlights) {
            this.includeHighlights = includeHighlights;
        }

        public boolean isFuzzyMatching() {
            return fuzzyMatching;
        }

        public void setFuzzyMatching(boolean fuzzyMatching) {
            this.fuzzyMatching = fuzzyMatching;
        }

        public boolean isWildcardMatching() {
            return wildcardMatching;
        }

        public void setWildcardMatching(boolean wildcardMatching) {
            this.wildcardMatching = wildcardMatching;
        }

        public boolean isPhraseMatching() {
            return phraseMatching;
        }

        public void setPhraseMatching(boolean phraseMatching) {
            this.phraseMatching = phraseMatching;
        }

        public boolean isIncludeFacets() {
            return includeFacets;
        }

        public void setIncludeFacets(boolean includeFacets) {
            this.includeFacets = includeFacets;
        }

        public boolean isIncludeSuggestions() {
            return includeSuggestions;
        }

        public void setIncludeSuggestions(boolean includeSuggestions) {
            this.includeSuggestions = includeSuggestions;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
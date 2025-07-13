package com.h12.seekly.metrics;

import com.h12.seekly.core.SearchMetrics;
import com.h12.seekly.core.SearchPerformanceStats;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.Histogram;
import io.micrometer.core.instrument.distribution.HistogramGauges;
import io.micrometer.core.instrument.distribution.HistogramSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Prometheus metrics collector for the Seekly search engine.
 * Provides comprehensive metrics for monitoring search performance and health.
 */
@Slf4j
//@Component
public class SearchMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final String metricsPrefix;

    // Counters
    private final Counter totalSearchesCounter;
    private final Counter successfulSearchesCounter;
    private final Counter failedSearchesCounter;
    private final Counter zeroResultSearchesCounter;
    private final Counter indexedDocumentsCounter;
    private final Counter deletedDocumentsCounter;
    private final Counter updatedDocumentsCounter;

    // Gauges
    private final Map<String, Gauge> entityTypeGauges = new ConcurrentHashMap<>();
    private final Gauge totalDocumentsGauge;
    private final Gauge indexSizeGauge;
    private final Gauge memoryUsageGauge;
    private final Gauge connectionPoolGauge;

    // Timers
    private final Timer searchTimer;
    private final Timer indexTimer;
    private final Timer optimizationTimer;

    // Histograms
    private final DistributionSummary searchResultsHistogram;
    private final DistributionSummary searchScoreHistogram;

    // Distribution summaries
    private final DistributionSummary searchTimeSummary;
    private final DistributionSummary indexSizeSummary;

    public SearchMetricsCollector(MeterRegistry meterRegistry, String metricsPrefix) {
        this.meterRegistry = meterRegistry;
        this.metricsPrefix = metricsPrefix != null ? metricsPrefix : "seekly_search";

        // Initialize counters
        this.totalSearchesCounter = Counter.builder(metricsPrefix + "_searches_total")
                .description("Total number of searches executed")
                .register(meterRegistry);

        this.successfulSearchesCounter = Counter.builder(metricsPrefix + "_searches_successful_total")
                .description("Total number of successful searches")
                .register(meterRegistry);

        this.failedSearchesCounter = Counter.builder(metricsPrefix + "_searches_failed_total")
                .description("Total number of failed searches")
                .register(meterRegistry);

        this.zeroResultSearchesCounter = Counter.builder(metricsPrefix + "_searches_zero_results_total")
                .description("Total number of searches with zero results")
                .register(meterRegistry);

        this.indexedDocumentsCounter = Counter.builder(metricsPrefix + "_documents_indexed_total")
                .description("Total number of documents indexed")
                .register(meterRegistry);

        this.deletedDocumentsCounter = Counter.builder(metricsPrefix + "_documents_deleted_total")
                .description("Total number of documents deleted")
                .register(meterRegistry);

        this.updatedDocumentsCounter = Counter.builder(metricsPrefix + "_documents_updated_total")
                .description("Total number of documents updated")
                .register(meterRegistry);

        // Initialize gauges
        this.totalDocumentsGauge = Gauge.builder(metricsPrefix + "_documents_total", this, SearchMetricsCollector::getTotalDocuments)
                .description("Total number of documents in index")
                .register(meterRegistry);

        this.indexSizeGauge = Gauge.builder(metricsPrefix + "_index_size_bytes", this, SearchMetricsCollector::getIndexSize)
                .description("Size of the search index in bytes")
                .register(meterRegistry);

        this.memoryUsageGauge = Gauge.builder(metricsPrefix + "_memory_usage_bytes", this, SearchMetricsCollector::getMemoryUsage)
                .description("Memory usage of the search engine in bytes")
                .register(meterRegistry);

        this.connectionPoolGauge = Gauge.builder(metricsPrefix + "_connection_pool_active", this, SearchMetricsCollector::getActiveConnections)
                .description("Number of active database connections")
                .register(meterRegistry);

        // Initialize timers
        this.searchTimer = Timer.builder(metricsPrefix + "_search_duration")
                .description("Search execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.indexTimer = Timer.builder(metricsPrefix + "_index_duration")
                .description("Indexing operation time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.optimizationTimer = Timer.builder(metricsPrefix + "_optimization_duration")
                .description("Index optimization time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Initialize histograms
        this.searchResultsHistogram = DistributionSummary.builder(metricsPrefix + "_search_results")
                .description("Distribution of search results count")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.searchScoreHistogram = DistributionSummary.builder(metricsPrefix + "_search_scores")
                .description("Distribution of search relevance scores")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Initialize distribution summaries
        this.searchTimeSummary = DistributionSummary.builder(metricsPrefix + "_search_time_summary")
                .description("Summary of search execution times")
                .baseUnit("milliseconds")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.indexSizeSummary = DistributionSummary.builder(metricsPrefix + "_index_size_summary")
                .description("Summary of index sizes")
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        log.info("Search metrics collector initialized with prefix: {}", metricsPrefix);
    }

    /**
     * Record a search operation
     */
    public void recordSearch(String entityType, String query, long durationMs, int resultsCount,
            double avgScore, boolean success, boolean zeroResults) {

        Tags tags = Tags.of("entity_type", entityType, "query", query);

        totalSearchesCounter.increment();
        searchTimer.record(durationMs, TimeUnit.MILLISECONDS);
        searchTimeSummary.record(durationMs);
        searchResultsHistogram.record(resultsCount);

        if (success) {
            successfulSearchesCounter.increment();
        } else {
            failedSearchesCounter.increment();
        }

        if (zeroResults) {
            zeroResultSearchesCounter.increment();
        }

        if (avgScore > 0) {
            searchScoreHistogram.record(avgScore);
        }

        // Record entity type specific metrics
        recordEntityTypeMetrics(entityType, resultsCount, durationMs);

        log.debug("Recorded search metrics: entityType={}, query={}, duration={}ms, results={}, success={}",
                entityType, query, durationMs, resultsCount, success);
    }

    /**
     * Record indexing operation
     */
    public void recordIndexing(String entityType, int documentCount, long durationMs) {
        Tags tags = Tags.of("entity_type", entityType);

        indexedDocumentsCounter.increment(documentCount);
        indexTimer.record(durationMs, TimeUnit.MILLISECONDS);

        log.debug("Recorded indexing metrics: entityType={}, documents={}, duration={}ms",
                entityType, documentCount, durationMs);
    }

    /**
     * Record document deletion
     */
    public void recordDeletion(String entityType, int documentCount) {
        deletedDocumentsCounter.increment(documentCount);

        log.debug("Recorded deletion metrics: entityType={}, documents={}", entityType, documentCount);
    }

    /**
     * Record document update
     */
    public void recordUpdate(String entityType, int documentCount) {
        updatedDocumentsCounter.increment(documentCount);

        log.debug("Recorded update metrics: entityType={}, documents={}", entityType, documentCount);
    }

    /**
     * Record index optimization
     */
    public void recordOptimization(long durationMs) {
        optimizationTimer.record(durationMs, TimeUnit.MILLISECONDS);

        log.debug("Recorded optimization metrics: duration={}ms", durationMs);
    }

    /**
     * Update performance statistics
     */
    public void updatePerformanceStats(SearchPerformanceStats stats) {
        // Update gauges with current values
        updateTotalDocuments(stats.getIndexedDocuments());
        updateIndexSize(stats.getCurrentIndexSize());
        updateMemoryUsage(stats.getMemoryUsageBytes());

        log.debug("Updated performance stats: totalDocs={}, indexSize={}, memoryUsage={}",
                stats.getIndexedDocuments(), stats.getCurrentIndexSize(), stats.getMemoryUsageBytes());
    }

    /**
     * Record search metrics
     */
    public void recordSearchMetrics(SearchMetrics metrics) {
        if (metrics != null) {
            recordSearch(
                    metrics.getEntityType(),
                    metrics.getQuery(),
                    metrics.getSearchTimeMs(),
                    metrics.getResultsReturned(),
                    0.0, // TODO: Calculate average score from results
                    metrics.getErrorMessage() == null,
                    metrics.isZeroResults());
        }
    }

    /**
     * Record entity type specific metrics
     */
    private void recordEntityTypeMetrics(String entityType, int resultsCount, long durationMs) {
        entityTypeGauges.computeIfAbsent(entityType + "_results",
                key -> Gauge.builder(metricsPrefix + "_entity_results", this, collector -> getEntityTypeResults(entityType))
                        .tag("entity_type", entityType)
                        .description("Number of results for entity type")
                        .register(meterRegistry));

        entityTypeGauges.computeIfAbsent(entityType + "_duration",
                key -> Gauge.builder(metricsPrefix + "_entity_duration", this, collector -> getEntityTypeDuration(entityType))
                        .tag("entity_type", entityType)
                        .description("Average search duration for entity type")
                        .register(meterRegistry));
    }

    // Gauge value providers
    private long totalDocuments = 0;
    private long indexSize = 0;
    private long memoryUsage = 0;
    private long activeConnections = 0;
    private final Map<String, Long> entityTypeResults = new ConcurrentHashMap<>();
    private final Map<String, Long> entityTypeDurations = new ConcurrentHashMap<>();

    private Long getTotalDocuments() {
        return totalDocuments;
    }

    private double getIndexSize() {
        return indexSize;
    }

    private double getMemoryUsage() {
        return memoryUsage;
    }

    private double getActiveConnections() {
        return activeConnections;
    }

    private double getEntityTypeResults(String entityType) {
        return entityTypeResults.getOrDefault(entityType, 0L);
    }

    private double getEntityTypeDuration(String entityType) {
        return entityTypeDurations.getOrDefault(entityType, 0L);
    }

    // Update methods
    public void updateTotalDocuments(long count) {
        this.totalDocuments = count;
    }

    public void updateIndexSize(long size) {
        this.indexSize = size;
    }

    public void updateMemoryUsage(long usage) {
        this.memoryUsage = usage;
    }

    public void updateActiveConnections(long connections) {
        this.activeConnections = connections;
    }

    public void updateEntityTypeResults(String entityType, long results) {
        this.entityTypeResults.put(entityType, results);
    }

    public void updateEntityTypeDuration(String entityType, long duration) {
        this.entityTypeDurations.put(entityType, duration);
    }

    /**
     * Get all metrics as a map for debugging
     */
    public Map<String, Object> getMetricsSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();

        snapshot.put("totalSearches", totalSearchesCounter.count());
        snapshot.put("successfulSearches", successfulSearchesCounter.count());
        snapshot.put("failedSearches", failedSearchesCounter.count());
        snapshot.put("zeroResultSearches", zeroResultSearchesCounter.count());
        snapshot.put("indexedDocuments", indexedDocumentsCounter.count());
        snapshot.put("deletedDocuments", deletedDocumentsCounter.count());
        snapshot.put("updatedDocuments", updatedDocumentsCounter.count());
        snapshot.put("totalDocuments", totalDocuments);
        snapshot.put("indexSize", indexSize);
        snapshot.put("memoryUsage", memoryUsage);
        snapshot.put("activeConnections", activeConnections);
        snapshot.put("entityTypeResults", new HashMap<>(entityTypeResults));
        snapshot.put("entityTypeDurations", new HashMap<>(entityTypeDurations));

        return snapshot;
    }
}
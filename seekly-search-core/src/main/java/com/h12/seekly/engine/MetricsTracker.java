package com.h12.seekly.engine;

import com.h12.seekly.core.SearchMetrics;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks and manages search metrics for the Seekly search engine.
 */
@Slf4j
public class MetricsTracker {

    private final Map<String, SearchMetrics> metricsByQuery = new ConcurrentHashMap<>();
    private final Map<String, List<SearchMetrics>> metricsByEntityType = new ConcurrentHashMap<>();
    private final Map<LocalDateTime, List<SearchMetrics>> metricsByTime = new ConcurrentHashMap<>();

    /**
     * Track search metrics for a specific query
     */
    public void trackMetrics(SearchMetrics metrics) {
        String query = metrics.getQuery();
        String entityType = metrics.getEntityType();
        LocalDateTime timestamp = metrics.getTimestamp();

        // Store by query
        metricsByQuery.put(query, metrics);

        // Store by entity type
        metricsByEntityType.computeIfAbsent(entityType, k -> new ArrayList<>()).add(metrics);

        // Store by time (hourly buckets)
        LocalDateTime hourBucket = timestamp.withMinute(0).withSecond(0).withNano(0);
        metricsByTime.computeIfAbsent(hourBucket, k -> new ArrayList<>()).add(metrics);

        log.debug("Tracked metrics for query: {} entityType: {} at: {}", query, entityType, timestamp);
    }

    /**
     * Get metrics for a specific query
     */
    public SearchMetrics getMetrics(String query) {
        return metricsByQuery.get(query);
    }

    /**
     * Get metrics for a specific entity type within a time range
     */
    public List<SearchMetrics> getMetrics(String entityType, LocalDateTime from, LocalDateTime to) {
        List<SearchMetrics> entityMetrics = metricsByEntityType.getOrDefault(entityType, Collections.emptyList());

        return entityMetrics.stream()
                .filter(metrics -> {
                    LocalDateTime timestamp = metrics.getTimestamp();
                    return !timestamp.isBefore(from) && !timestamp.isAfter(to);
                })
                .sorted(Comparator.comparing(SearchMetrics::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Get metrics within a time range
     */
    public List<SearchMetrics> getMetrics(LocalDateTime from, LocalDateTime to) {
        return metricsByTime.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime bucketTime = entry.getKey();
                    return !bucketTime.isBefore(from) && !bucketTime.isAfter(to);
                })
                .flatMap(entry -> entry.getValue().stream())
                .filter(metrics -> {
                    LocalDateTime timestamp = metrics.getTimestamp();
                    return !timestamp.isBefore(from) && !timestamp.isAfter(to);
                })
                .sorted(Comparator.comparing(SearchMetrics::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Get top performing queries by execution count
     */
    public List<String> getTopQueries(String entityType, int limit) {
        return metricsByEntityType.getOrDefault(entityType, Collections.emptyList()).stream()
                .collect(Collectors.groupingBy(SearchMetrics::getQuery, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get zero-result queries
     */
    public List<String> getZeroResultQueries(String entityType, int limit) {
        return metricsByEntityType.getOrDefault(entityType, Collections.emptyList()).stream()
                .filter(SearchMetrics::isZeroResults)
                .collect(Collectors.groupingBy(SearchMetrics::getQuery, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get average search time for a specific entity type
     */
    public double getAverageSearchTime(String entityType) {
        List<SearchMetrics> entityMetrics = metricsByEntityType.getOrDefault(entityType, Collections.emptyList());

        if (entityMetrics.isEmpty()) {
            return 0.0;
        }

        return entityMetrics.stream()
                .mapToLong(SearchMetrics::getSearchTimeMs)
                .average()
                .orElse(0.0);
    }

    /**
     * Get total searches for a specific entity type
     */
    public long getTotalSearches(String entityType) {
        return metricsByEntityType.getOrDefault(entityType, Collections.emptyList()).size();
    }

    /**
     * Clear old metrics (older than specified time)
     */
    public void clearOldMetrics(LocalDateTime cutoff) {
        // Clear from query map
        metricsByQuery.entrySet().removeIf(entry -> entry.getValue().getTimestamp().isBefore(cutoff));

        // Clear from entity type map
        metricsByEntityType
                .forEach((entityType, metrics) -> metrics.removeIf(metric -> metric.getTimestamp().isBefore(cutoff)));

        // Clear from time map
        metricsByTime.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));

        log.info("Cleared metrics older than: {}", cutoff);
    }

    /**
     * Get all metrics
     */
    public List<SearchMetrics> getAllMetrics() {
        return new ArrayList<>(metricsByQuery.values());
    }

    /**
     * Get metrics summary
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalMetrics = metricsByQuery.size();
        summary.put("totalMetrics", totalMetrics);

        Set<String> entityTypes = metricsByEntityType.keySet();
        summary.put("entityTypes", entityTypes);

        if (!entityTypes.isEmpty()) {
            Map<String, Long> searchesByType = new HashMap<>();
            Map<String, Double> avgTimeByType = new HashMap<>();

            for (String entityType : entityTypes) {
                searchesByType.put(entityType, getTotalSearches(entityType));
                avgTimeByType.put(entityType, getAverageSearchTime(entityType));
            }

            summary.put("searchesByType", searchesByType);
            summary.put("averageTimeByType", avgTimeByType);
        }

        return summary;
    }
}
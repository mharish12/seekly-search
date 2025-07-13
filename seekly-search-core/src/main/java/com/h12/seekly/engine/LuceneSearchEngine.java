package com.h12.seekly.engine;

import com.h12.seekly.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
//import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Lucene-based implementation of the Seekly search engine.
 * Provides comprehensive search functionality with advanced metrics tracking.
 */
@Slf4j
public class LuceneSearchEngine<T extends SearchableEntity> implements SearchEngine<T> {

    private final Directory directory;
    private final Analyzer analyzer;
    private final Path indexPath;
    private final String entityType;
    private final MetricsTracker metricsTracker;

    // Performance tracking
    private final AtomicLong totalSearches = new AtomicLong(0);
    private final AtomicLong successfulSearches = new AtomicLong(0);
    private final AtomicLong failedSearches = new AtomicLong(0);
    private final AtomicLong totalSearchTime = new AtomicLong(0);
    private final AtomicLong totalResultsReturned = new AtomicLong(0);
    private final AtomicLong zeroResultSearches = new AtomicLong(0);

    // Query performance tracking
    private final Map<String, QueryPerformance> queryPerformanceMap = new ConcurrentHashMap<>();

    // Index statistics
    private final AtomicLong indexedDocuments = new AtomicLong(0);
    private final AtomicLong deletedDocuments = new AtomicLong(0);
    private final AtomicLong updatedDocuments = new AtomicLong(0);
    private final AtomicLong indexOptimizations = new AtomicLong(0);
    private LocalDateTime lastOptimization = LocalDateTime.now();
    private LocalDateTime startTime = LocalDateTime.now();

    public LuceneSearchEngine(String indexPath, String entityType) throws IOException {
        this.indexPath = Paths.get(indexPath);
        this.entityType = entityType;
        this.directory = FSDirectory.open(this.indexPath);
        this.analyzer = new StandardAnalyzer();
        this.metricsTracker = new MetricsTracker();

        // Create index if it doesn't exist
        if (!DirectoryReader.indexExists(directory)) {
            createIndex();
        }

        log.info("LuceneSearchEngine initialized for entity type: {} at path: {}", entityType, indexPath);
    }

    @Override
    public void index(T entity) {
        try (IndexWriter writer = createIndexWriter()) {
            Document doc = createDocument(entity);
            writer.addDocument(doc);
            writer.commit();
            indexedDocuments.incrementAndGet();
            log.debug("Indexed entity: {} with ID: {}", entity.getEntityType(), entity.getId());
        } catch (IOException e) {
            log.error("Failed to index entity: {}", entity.getId(), e);
            throw new RuntimeException("Failed to index entity", e);
        }
    }

    @Override
    public void indexBatch(List<T> entities) {
        try (IndexWriter writer = createIndexWriter()) {
            for (T entity : entities) {
                Document doc = createDocument(entity);
                writer.addDocument(doc);
            }
            writer.commit();
            indexedDocuments.addAndGet(entities.size());
            log.debug("Indexed {} entities of type: {}", entities.size(), entityType);
        } catch (IOException e) {
            log.error("Failed to index batch of {} entities", entities.size(), e);
            throw new RuntimeException("Failed to index batch", e);
        }
    }

    @Override
    public void removeFromIndex(String entityId) {
        try (IndexWriter writer = createIndexWriter()) {
            writer.deleteDocuments(new Term("id", entityId));
            writer.commit();
            deletedDocuments.incrementAndGet();
            log.debug("Removed entity with ID: {} from index", entityId);
        } catch (IOException e) {
            log.error("Failed to remove entity: {}", entityId, e);
            throw new RuntimeException("Failed to remove entity", e);
        }
    }

    @Override
    public void updateIndex(T entity) {
        try (IndexWriter writer = createIndexWriter()) {
            Document doc = createDocument(entity);
            writer.updateDocument(new Term("id", entity.getId()), doc);
            writer.commit();
            updatedDocuments.incrementAndGet();
            log.debug("Updated entity: {} with ID: {}", entity.getEntityType(), entity.getId());
        } catch (IOException e) {
            log.error("Failed to update entity: {}", entity.getId(), e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    @Override
    public SearchResponse<T> search(String query) {
        return search(query, SearchOptions.builder().build());
    }

    @Override
    public SearchResponse<T> search(String query, SearchOptions options) {
        return search(query, Collections.emptyMap(), options);
    }

    @Override
    public SearchResponse<T> search(String query, Map<String, Object> filters) {
        return search(query, filters, SearchOptions.builder().build());
    }

    @Override
    public SearchResponse<T> search(String query, Map<String, Object> filters, SearchOptions options) {
        long startTime = System.currentTimeMillis();
        totalSearches.incrementAndGet();

        try {
            Query luceneQuery = buildQuery(query, options);
//            Filter filter = buildFilter(filters);

            SearchResponse<T> response = executeSearch(luceneQuery, options);

            long searchTime = System.currentTimeMillis() - startTime;
            response.setSearchTimeMs(searchTime);
            response.setQuery(query);
            response.setEntityType(entityType);
            response.setOptions(options);
            response.setFilters(filters);
            response.setSuccess(true);
            response.setTimestamp(LocalDateTime.now());

            // Update metrics
            totalSearchTime.addAndGet(searchTime);
            successfulSearches.incrementAndGet();
            totalResultsReturned.addAndGet(response.getResults().size());

            if (response.getTotalHits() == 0) {
                zeroResultSearches.incrementAndGet();
            }

            // Track query performance
            updateQueryPerformance(query, searchTime, response.getTotalHits(), response.getResults().size());

            // Track search metrics
            if (options.isTrackMetrics()) {
                trackSearchMetrics(response, options);
            }

            return response;

        } catch (Exception e) {
            long searchTime = System.currentTimeMillis() - startTime;
            totalSearchTime.addAndGet(searchTime);
            failedSearches.incrementAndGet();

            log.error("Search failed for query: {}", query, e);

            return SearchResponse.<T>builder()
                    .results(Collections.emptyList())
                    .totalHits(0)
                    .searchTimeMs(searchTime)
                    .query(query)
                    .entityType(entityType)
                    .options(options)
                    .filters(filters)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public List<String> getSuggestions(String partialQuery, int maxSuggestions) {
        // Implementation for search suggestions
        // This would typically use Lucene's SuggestIndexSearcher
        return Collections.emptyList();
    }

    @Override
    public Optional<SearchMetrics> getSearchMetrics(String query) {
        return Optional.ofNullable(metricsTracker.getMetrics(query));
    }

    @Override
    public List<SearchMetrics> getSearchMetrics(String entityType, LocalDateTime from, LocalDateTime to) {
        return metricsTracker.getMetrics(entityType, from, to);
    }

    @Override
    public List<QueryPerformance> getTopQueries(String entityType, int limit) {
        return queryPerformanceMap.values().stream()
                .filter(qp -> qp.getEntityType().equals(entityType))
                .sorted(Comparator.comparing(QueryPerformance::getExecutionCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getZeroResultQueries(String entityType, int limit) {
        return queryPerformanceMap.values().stream()
                .filter(qp -> qp.getEntityType().equals(entityType) && qp.getZeroResultCount() > 0)
                .sorted(Comparator.comparing(QueryPerformance::getZeroResultCount).reversed())
                .limit(limit)
                .map(QueryPerformance::getQuery)
                .collect(Collectors.toList());
    }

    @Override
    public SearchPerformanceStats getPerformanceStats() {
        long totalSearchesCount = totalSearches.get();
        long successfulSearchesCount = successfulSearches.get();

        return SearchPerformanceStats.builder()
                .totalSearches(totalSearchesCount)
                .successfulSearches(successfulSearchesCount)
                .failedSearches(failedSearches.get())
                .averageSearchTimeMs(totalSearchesCount > 0 ? (double) totalSearchTime.get() / totalSearchesCount : 0)
                .totalResultsReturned(totalResultsReturned.get())
                .averageResultsPerSearch(
                        totalSearchesCount > 0 ? (double) totalResultsReturned.get() / totalSearchesCount : 0)
                .zeroResultSearches(zeroResultSearches.get())
                .successRate(totalSearchesCount > 0 ? (double) successfulSearchesCount / totalSearchesCount * 100 : 0)
                .indexedDocuments(indexedDocuments.get())
                .deletedDocuments(deletedDocuments.get())
                .updatedDocuments(updatedDocuments.get())
                .indexOptimizations(indexOptimizations.get())
                .lastOptimization(lastOptimization)
                .uptimeMs(System.currentTimeMillis() - startTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                .startTime(startTime)
                .lastActivity(LocalDateTime.now())
                .build();
    }

    @Override
    public void clearIndex() {
        try (IndexWriter writer = createIndexWriter()) {
            writer.deleteAll();
            writer.commit();
            log.info("Cleared entire index for entity type: {}", entityType);
        } catch (IOException e) {
            log.error("Failed to clear index", e);
            throw new RuntimeException("Failed to clear index", e);
        }
    }

    @Override
    public IndexStats getIndexStats() {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            return IndexStats.builder()
                    .totalDocuments(reader.numDocs())
                    .deletedDocuments(reader.numDeletedDocs())
                    .segmentCount(reader.leaves().size())
                    .version(1) // TODO: update version
                    .lastCommit(LocalDateTime.now())
                    .lastOptimization(lastOptimization)
                    .optimized(true)
                    .health(IndexStats.IndexHealth.HEALTHY)
                    .build();
        } catch (IOException e) {
            log.error("Failed to get index stats", e);
            throw new RuntimeException("Failed to get index stats", e);
        }
    }

    @Override
    public void optimizeIndex() {
        try (IndexWriter writer = createIndexWriter()) {
            writer.forceMerge(1);
            writer.commit();
            indexOptimizations.incrementAndGet();
            lastOptimization = LocalDateTime.now();
            log.info("Optimized index for entity type: {}", entityType);
        } catch (IOException e) {
            log.error("Failed to optimize index", e);
            throw new RuntimeException("Failed to optimize index", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            return reader.numDocs() >= 0;
        } catch (IOException e) {
            log.error("Index health check failed", e);
            return false;
        }
    }

    // Private helper methods

    private void createIndex() throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.commit();
            log.info("Created new index for entity type: {}", entityType);
        }
    }

    private IndexWriter createIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory, config);
    }

    private Document createDocument(T entity) {
        Document doc = new Document();
        doc.add(new StringField("id", entity.getId(), Field.Store.YES));
        doc.add(new StringField("entityType", entity.getEntityType(), Field.Store.YES));
        doc.add(new TextField("content", entity.getSearchableContent(), Field.Store.YES));
        doc.add(new FloatPoint("relevanceScore", (float) entity.getRelevanceScore()));
        doc.add(new LongPoint("createdAt", entity.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()));
        doc.add(new LongPoint("updatedAt", entity.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()));
        doc.add(new StringField("active", String.valueOf(entity.isActive()), Field.Store.YES));

        // Add additional searchable fields
        for (Map.Entry<String, String> field : entity.getSearchableFields().entrySet()) {
            doc.add(new TextField(field.getKey(), field.getValue(), Field.Store.YES));
        }

        return doc;
    }

    private Query buildQuery(String queryString, SearchOptions options) {
        // Basic implementation - can be enhanced with more sophisticated query building
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        // Main content query
        Query contentQuery = new FuzzyQuery(new Term("content", queryString.toLowerCase()));
        contentQuery = new BoostQuery(contentQuery, options.getExactMatchBoost());
        queryBuilder.add(contentQuery, BooleanClause.Occur.SHOULD);

        // Add field-specific queries if specified
        if (options.getSearchFields() != null) {
            for (String field : options.getSearchFields()) {
                Query fieldQuery = new FuzzyQuery(new Term(field, queryString.toLowerCase()));
                queryBuilder.add(fieldQuery, BooleanClause.Occur.SHOULD);
            }
        }

        return queryBuilder.build();
    }

//    private Filter buildFilter(Map<String, Object> filters) {
//        // Implementation for building Lucene filters from the filter map
//        return null;
//    }

    private SearchResponse<T> executeSearch(Query query, SearchOptions options) throws IOException {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs topDocs = searcher.search(query, options.getMaxResults());

            List<SearchResult<T>> results = new ArrayList<>();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.storedFields().document(scoreDoc.doc);

                // Create search result (entity reconstruction would be needed here)
                SearchResult<T> result = SearchResult.<T>builder()
                        .score(scoreDoc.score)
                        .rank(i + 1)
                        .inTop10(i < 10)
                        .inTop5(i < 5)
                        .inTop3(i < 3)
                        .isFirst(i == 0)
                        .timestamp(LocalDateTime.now())
                        .build();

                results.add(result);
            }

            return SearchResponse.<T>builder()
                    .results(results)
                    .totalHits(topDocs.totalHits.value())
                    .build();
        }
    }

    private void updateQueryPerformance(String query, long searchTime, long totalHits, int resultsReturned) {
        queryPerformanceMap.compute(query, (q, existing) -> {
            QueryPerformance qp = existing != null ? existing
                    : QueryPerformance.builder()
                            .query(query)
                            .entityType(entityType)
                            .firstExecuted(LocalDateTime.now())
                            .build();

            qp.setExecutionCount(qp.getExecutionCount() + 1);
            qp.setTotalExecutionTimeMs(qp.getTotalExecutionTimeMs() + searchTime);
            qp.setAverageExecutionTimeMs((double) qp.getTotalExecutionTimeMs() / qp.getExecutionCount());
            qp.setTotalResultsReturned(qp.getTotalResultsReturned() + resultsReturned);
            qp.setAverageResultsReturned((double) qp.getTotalResultsReturned() / qp.getExecutionCount());

            if (totalHits == 0) {
                qp.setZeroResultCount(qp.getZeroResultCount() + 1);
            }

            qp.setLastExecuted(LocalDateTime.now());
            return qp;
        });
    }

    private void trackSearchMetrics(SearchResponse<T> response, SearchOptions options) {
        SearchMetrics metrics = SearchMetrics.builder()
                .query(response.getQuery())
                .entityType(response.getEntityType())
                .totalHits(response.getTotalHits())
                .resultsReturned(response.getResults().size())
                .searchTimeMs(response.getSearchTimeMs())
                .timestamp(response.getTimestamp())
                .sessionId(options.getSessionId())
                .userId(options.getUserId())
                .filters(response.getFilters())
                .zeroResults(response.getTotalHits() == 0)
                .build();

        metricsTracker.trackMetrics(metrics);
    }
}
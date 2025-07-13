package com.h12.seekly.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.h12.seekly.core.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * PostgreSQL-based implementation of the Seekly search engine.
 * Stores documents in PostgreSQL database while using Lucene for search
 * capabilities.
 * Provides persistence, scalability, and better data management.
 */
@Slf4j
public class LucenePostgresSearchEngine<T extends SearchableEntity> implements SearchEngine<T> {

    private final Directory luceneDirectory;
    private final Analyzer analyzer;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final String entityType;
    private final String tableName;
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

    public LucenePostgresSearchEngine(String luceneIndexPath, String entityType,
            String dbUrl, String dbUsername, String dbPassword) throws IOException {
        this.luceneDirectory = FSDirectory.open(Paths.get(luceneIndexPath));
        this.analyzer = new StandardAnalyzer();
        this.entityType = entityType;
        this.tableName = "seekly_" + entityType.toLowerCase() + "_documents";
        this.objectMapper = new ObjectMapper();
        this.metricsTracker = new MetricsTracker();

        // Initialize PostgreSQL connection pool
        this.dataSource = createDataSource(dbUrl, dbUsername, dbPassword);

        // Initialize database schema
        initializeDatabase();

        // Create Lucene index if it doesn't exist
        if (!DirectoryReader.indexExists(luceneDirectory)) {
            createLuceneIndex();
        }

        log.info("LucenePostgresSearchEngine initialized for entity type: {} with table: {}", entityType, tableName);
    }

    @Override
    public void index(T entity) {
        try {
            // Store in PostgreSQL
            storeInPostgres(entity);

            // Index in Lucene
            indexInLucene(entity);

            indexedDocuments.incrementAndGet();
            log.debug("Indexed entity: {} with ID: {}", entity.getEntityType(), entity.getId());
        } catch (Exception e) {
            log.error("Failed to index entity: {}", entity.getId(), e);
            throw new RuntimeException("Failed to index entity", e);
        }
    }

    @Override
    public void indexBatch(List<T> entities) {
        try {
            // Batch store in PostgreSQL
            batchStoreInPostgres(entities);

            // Batch index in Lucene
            batchIndexInLucene(entities);

            indexedDocuments.addAndGet(entities.size());
            log.debug("Indexed {} entities of type: {}", entities.size(), entityType);
        } catch (Exception e) {
            log.error("Failed to index batch of {} entities", entities.size(), e);
            throw new RuntimeException("Failed to index batch", e);
        }
    }

    @Override
    public void removeFromIndex(String entityId) {
        try {
            // Remove from PostgreSQL
            removeFromPostgres(entityId);

            // Remove from Lucene
            removeFromLucene(entityId);

            deletedDocuments.incrementAndGet();
            log.debug("Removed entity with ID: {} from index", entityId);
        } catch (Exception e) {
            log.error("Failed to remove entity: {}", entityId, e);
            throw new RuntimeException("Failed to remove entity", e);
        }
    }

    @Override
    public void updateIndex(T entity) {
        try {
            // Update in PostgreSQL
            updateInPostgres(entity);

            // Update in Lucene
            updateInLucene(entity);

            updatedDocuments.incrementAndGet();
            log.debug("Updated entity: {} with ID: {}", entity.getEntityType(), entity.getId());
        } catch (Exception e) {
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
        // Implementation for search suggestions using PostgreSQL
        String sql = "SELECT DISTINCT searchable_content FROM " + tableName +
                " WHERE searchable_content ILIKE ? LIMIT ?";

        List<String> suggestions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, partialQuery.toLowerCase() + "%");
            stmt.setInt(2, maxSuggestions);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(rs.getString("searchable_content"));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get suggestions for: {}", partialQuery, e);
        }

        return suggestions;
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
        try {
            // Clear PostgreSQL table
            clearPostgresTable();

            // Clear Lucene index
            clearLuceneIndex();

            log.info("Cleared entire index for entity type: {}", entityType);
        } catch (Exception e) {
            log.error("Failed to clear index", e);
            throw new RuntimeException("Failed to clear index", e);
        }
    }

    @Override
    public IndexStats getIndexStats() {
        try {
            long postgresCount = getPostgresDocumentCount();
            long luceneCount = getLuceneDocumentCount();

            return IndexStats.builder()
                    .totalDocuments(postgresCount)
                    .deletedDocuments(0) // PostgreSQL doesn't track deleted docs like Lucene
                    .segmentCount(1) // PostgreSQL is one "segment"
                    .version(1)
                    .lastCommit(LocalDateTime.now())
                    .lastOptimization(lastOptimization)
                    .optimized(true)
                    .health(IndexStats.IndexHealth.HEALTHY)
                    .build();
        } catch (Exception e) {
            log.error("Failed to get index stats", e);
            throw new RuntimeException("Failed to get index stats", e);
        }
    }

    @Override
    public void optimizeIndex() {
        try {
            // Optimize PostgreSQL (vacuum and analyze)
            optimizePostgres();

            // Optimize Lucene index
            optimizeLucene();

            indexOptimizations.incrementAndGet();
            lastOptimization = LocalDateTime.now();
            log.info("Optimized index for entity type: {}", entityType);
        } catch (Exception e) {
            log.error("Failed to optimize index", e);
            throw new RuntimeException("Failed to optimize index", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Check PostgreSQL connection
            try (Connection conn = dataSource.getConnection()) {
                if (!conn.isValid(5)) {
                    return false;
                }
            }

            // Check Lucene index
            try (IndexReader reader = DirectoryReader.open(luceneDirectory)) {
                return reader.numDocs() >= 0;
            } catch (Exception e) {
                log.error("Failed to check index health", e);
                throw new RuntimeException("Failed to check index health", e);
            }

//            return true;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }

    // Private helper methods

    private DataSource createDataSource(String dbUrl, String dbUsername, String dbPassword) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("SeeklySearchPool-" + entityType);

        return new HikariDataSource(config);
    }

    private void initializeDatabase() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id VARCHAR(255) PRIMARY KEY,
                    entity_type VARCHAR(100) NOT NULL,
                    searchable_content TEXT NOT NULL,
                    searchable_fields JSONB,
                    relevance_score DOUBLE PRECISION DEFAULT 1.0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    active BOOLEAN DEFAULT TRUE,
                    entity_data JSONB NOT NULL,
                    INDEX idx_searchable_content (searchable_content),
                    INDEX idx_entity_type (entity_type),
                    INDEX idx_relevance_score (relevance_score),
                    INDEX idx_created_at (created_at),
                    INDEX idx_updated_at (updated_at),
                    INDEX idx_active (active)
                )
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            log.info("Initialized database table: {}", tableName);
        } catch (SQLException e) {
            log.error("Failed to initialize database table: {}", tableName, e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void createLuceneIndex() throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.commit();
            log.info("Created new Lucene index for entity type: {}", entityType);
        }
    }

    private void storeInPostgres(T entity) throws SQLException, JsonProcessingException {
        String sql = """
                INSERT INTO %s (id, entity_type, searchable_content, searchable_fields,
                               relevance_score, created_at, updated_at, active, entity_data)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    entity_type = EXCLUDED.entity_type,
                    searchable_content = EXCLUDED.searchable_content,
                    searchable_fields = EXCLUDED.searchable_fields,
                    relevance_score = EXCLUDED.relevance_score,
                    updated_at = EXCLUDED.updated_at,
                    active = EXCLUDED.active,
                    entity_data = EXCLUDED.entity_data
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getId());
            stmt.setString(2, entity.getEntityType());
            stmt.setString(3, entity.getSearchableContent());
            stmt.setString(4, objectMapper.writeValueAsString(entity.getSearchableFields()));
            stmt.setDouble(5, entity.getRelevanceScore());
            stmt.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(entity.getUpdatedAt()));
            stmt.setBoolean(8, entity.isActive());
            stmt.setString(9, objectMapper.writeValueAsString(entity));

            stmt.executeUpdate();
        }
    }

    private void batchStoreInPostgres(List<T> entities) throws SQLException, JsonProcessingException {
        String sql = """
                INSERT INTO %s (id, entity_type, searchable_content, searchable_fields,
                               relevance_score, created_at, updated_at, active, entity_data)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    entity_type = EXCLUDED.entity_type,
                    searchable_content = EXCLUDED.searchable_content,
                    searchable_fields = EXCLUDED.searchable_fields,
                    relevance_score = EXCLUDED.relevance_score,
                    updated_at = EXCLUDED.updated_at,
                    active = EXCLUDED.active,
                    entity_data = EXCLUDED.entity_data
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (T entity : entities) {
                stmt.setString(1, entity.getId());
                stmt.setString(2, entity.getEntityType());
                stmt.setString(3, entity.getSearchableContent());
                stmt.setString(4, objectMapper.writeValueAsString(entity.getSearchableFields()));
                stmt.setDouble(5, entity.getRelevanceScore());
                stmt.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
                stmt.setTimestamp(7, Timestamp.valueOf(entity.getUpdatedAt()));
                stmt.setBoolean(8, entity.isActive());
                stmt.setString(9, objectMapper.writeValueAsString(entity));

                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
        }
    }

    private void indexInLucene(T entity) throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            Document doc = createDocument(entity);
            writer.addDocument(doc);
            writer.commit();
        }
    }

    private void batchIndexInLucene(List<T> entities) throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            for (T entity : entities) {
                Document doc = createDocument(entity);
                writer.addDocument(doc);
            }
            writer.commit();
        }
    }

    private void removeFromPostgres(String entityId) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entityId);
            stmt.executeUpdate();
        }
    }

    private void removeFromLucene(String entityId) throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.deleteDocuments(new Term("id", entityId));
            writer.commit();
        }
    }

    private void updateInPostgres(T entity) throws SQLException, JsonProcessingException {
        storeInPostgres(entity); // Uses UPSERT
    }

    private void updateInLucene(T entity) throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            Document doc = createDocument(entity);
            writer.updateDocument(new Term("id", entity.getId()), doc);
            writer.commit();
        }
    }

    private IndexWriter createIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(luceneDirectory, config);
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

    private SearchResponse<T> executeSearch(Query query, SearchOptions options) throws IOException {
        try (IndexReader reader = DirectoryReader.open(luceneDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs topDocs = searcher.search(query, options.getMaxResults());

            List<SearchResult<T>> results = new ArrayList<>();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.storedFields().document(scoreDoc.doc);

                // Retrieve full entity from PostgreSQL
                T entity = retrieveEntityFromPostgres(doc.get("id"));

                if (entity != null) {
                    SearchResult<T> result = SearchResult.<T>builder()
                            .entity(entity)
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
            }

            return SearchResponse.<T>builder()
                    .results(results)
                    .totalHits(topDocs.totalHits.value())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private T retrieveEntityFromPostgres(String entityId) {
        String sql = "SELECT entity_data FROM " + tableName + " WHERE id = ? AND active = TRUE";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entityId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String entityJson = rs.getString("entity_data");
                    return (T) objectMapper.readValue(entityJson, Object.class);
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve entity: {}", entityId, e);
        }

        return null;
    }

    private void clearPostgresTable() throws SQLException {
        String sql = "DELETE FROM " + tableName;

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void clearLuceneIndex() throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.deleteAll();
            writer.commit();
        }
    }

    private long getPostgresDocumentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE active = TRUE";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }

        return 0;
    }

    private long getLuceneDocumentCount() throws IOException {
        try (IndexReader reader = DirectoryReader.open(luceneDirectory)) {
            return reader.numDocs();
        }
    }

    private void optimizePostgres() throws SQLException {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("VACUUM ANALYZE " + tableName);
        }
    }

    private void optimizeLucene() throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.forceMerge(1);
            writer.commit();
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
# PostgreSQL Search Engine

The `LucenePostgresSearchEngine` is a hybrid search engine that combines the power of Apache Lucene for search capabilities with PostgreSQL for data persistence and management.

## Features

### 🔄 Hybrid Architecture

- **Lucene**: Handles full-text search, relevance scoring, and query processing
- **PostgreSQL**: Stores complete entity data, metadata, and provides ACID compliance
- **Best of both worlds**: Fast search + reliable data persistence

### 📊 Enhanced Data Management

- **Complete entity storage**: Full JSON serialization of entities
- **ACID compliance**: Transactional operations with rollback support
- **Data integrity**: Foreign key constraints and data validation
- **Backup and recovery**: Standard PostgreSQL backup mechanisms

### 🚀 Performance Optimizations

- **Connection pooling**: HikariCP for efficient database connections
- **Batch operations**: Bulk indexing and updates
- **Indexed queries**: PostgreSQL indexes on searchable fields
- **JSONB storage**: Efficient JSON storage and querying

### 🔍 Advanced Search Capabilities

- **Full-text search**: Lucene-powered search with relevance scoring
- **Field-specific search**: Search in specific entity fields
- **Fuzzy matching**: Typo-tolerant search
- **Suggestions**: Autocomplete from PostgreSQL data
- **Faceted search**: Aggregation and filtering capabilities

## Quick Start

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE seekly_search;

-- Create user (optional)
CREATE USER seekly_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE seekly_search TO seekly_user;
```

### 2. Basic Usage

```java
// Create PostgreSQL search engine
PostgresSearchConfig config = PostgresSearchConfig.builder()
    .luceneIndexPath("./index/products")
    .entityType("product")
    .dbUrl("jdbc:postgresql://localhost:5432/seekly_search")
    .dbUsername("seekly_user")
    .dbPassword("your_password")
    .maxPoolSize(20)
    .build();

SearchEngine<Product> searchEngine = SearchEngineFactory.createPostgresSearchEngine(config);

// Index products
Product product = Product.builder()
    .id("prod-001")
    .name("iPhone 15 Pro")
    .description("Latest iPhone with advanced camera")
    .category("Electronics")
    .price(new BigDecimal("999.99"))
    .build();

searchEngine.index(product);

// Search products
SearchResponse<Product> response = searchEngine.search("iPhone");
```

### 3. Batch Operations

```java
// Batch indexing
List<Product> products = loadProductsFromDatabase();
searchEngine.indexBatch(products);

// Batch updates
List<Product> updatedProducts = getUpdatedProducts();
for (Product product : updatedProducts) {
    searchEngine.updateIndex(product);
}
```

## Configuration

### PostgresSearchConfig Options

```java
PostgresSearchConfig config = PostgresSearchConfig.builder()
    // Database connection
    .dbUrl("jdbc:postgresql://localhost:5432/seekly_search")
    .dbUsername("username")
    .dbPassword("password")

    // Lucene index
    .luceneIndexPath("./index/products")
    .entityType("product")

    // Connection pool settings
    .maxPoolSize(20)           // Maximum connections
    .minIdle(5)                // Minimum idle connections
    .connectionTimeout(30000)  // Connection timeout (ms)
    .idleTimeout(600000)       // Idle timeout (ms)
    .maxLifetime(1800000)      // Max connection lifetime (ms)

    // Features
    .enableMetrics(true)       // Enable search metrics
    .enableQueryPerformance(true) // Enable query performance tracking
    .enableAutoOptimization(true) // Auto-optimize index
    .autoOptimizeThreshold(1000)  // Optimize after N operations
    .build();
```

### Environment Variables

```bash
# Set PostgreSQL connection details
export POSTGRES_URL="jdbc:postgresql://localhost:5432/seekly_search"
export POSTGRES_USERNAME="seekly_user"
export POSTGRES_PASSWORD="your_password"

# Run the application
./gradlew runApp
```

## Database Schema

The PostgreSQL search engine automatically creates the following table structure:

```sql
CREATE TABLE seekly_{entity_type}_documents (
    id VARCHAR(255) PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    searchable_content TEXT NOT NULL,
    searchable_fields JSONB,
    relevance_score DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    entity_data JSONB NOT NULL,

    -- Indexes for performance
    INDEX idx_searchable_content (searchable_content),
    INDEX idx_entity_type (entity_type),
    INDEX idx_relevance_score (relevance_score),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_active (active)
);
```

### Schema Details

- **id**: Unique entity identifier
- **entity_type**: Type of entity (e.g., "product", "seller")
- **searchable_content**: Full-text searchable content
- **searchable_fields**: JSON object with field-specific search data
- **relevance_score**: Custom relevance score for ranking
- **created_at/updated_at**: Timestamps for tracking
- **active**: Whether the entity is active/available
- **entity_data**: Complete JSON serialization of the entity

## Advanced Usage

### Custom Relevance Scoring

```java
@Override
public double getRelevanceScore() {
    double score = 1.0;

    // Boost by rating
    if (rating != null && rating > 0) {
        score += rating / 5.0 * 0.3;
    }

    // Boost by popularity
    if (reviewCount != null && reviewCount > 0) {
        score += Math.min(reviewCount / 100.0, 0.2);
    }

    // Penalize out-of-stock
    if (stockQuantity != null && stockQuantity == 0) {
        score -= 0.3;
    }

    return Math.max(0.1, Math.min(score, 2.0));
}
```

### Search with Filters

```java
// Search with PostgreSQL filters
Map<String, Object> filters = Map.of(
    "category", "Electronics",
    "price_min", 100.0,
    "price_max", 1000.0,
    "active", true
);

SearchResponse<Product> response = searchEngine.search("smartphone", filters);
```

### Advanced Search Options

```java
SearchOptions options = SearchOptions.builder()
    .maxResults(50)
    .includeHighlights(true)
    .fuzzyMatching(true)
    .fuzzyDistance(1)
    .wildcardMatching(true)
    .phraseMatching(true)
    .exactMatchBoost(2.0f)
    .phraseMatchBoost(1.5f)
    .includeFacets(true)
    .facetFields(Arrays.asList("category", "brand"))
    .includeSuggestions(true)
    .maxSuggestions(10)
    .trackMetrics(true)
    .sessionId("user-session-123")
    .userId("user-456")
    .build();

SearchResponse<Product> response = searchEngine.search("laptop", options);
```

### Database Queries

You can also perform direct database queries for analytics:

```sql
-- Get search statistics
SELECT
    entity_type,
    COUNT(*) as total_documents,
    AVG(relevance_score) as avg_relevance,
    MIN(created_at) as first_indexed,
    MAX(updated_at) as last_updated
FROM seekly_product_documents
WHERE active = TRUE
GROUP BY entity_type;

-- Get popular search terms
SELECT
    searchable_content,
    COUNT(*) as frequency
FROM seekly_product_documents
WHERE active = TRUE
GROUP BY searchable_content
ORDER BY frequency DESC
LIMIT 10;

-- Get recent additions
SELECT
    id,
    searchable_content,
    created_at
FROM seekly_product_documents
WHERE active = TRUE
ORDER BY created_at DESC
LIMIT 20;
```

## Performance Considerations

### Connection Pooling

- Configure appropriate pool size based on your workload
- Monitor connection usage and adjust settings
- Use connection timeouts to prevent hanging connections

### Indexing Performance

- Use batch operations for bulk indexing
- Consider background indexing for large datasets
- Monitor PostgreSQL performance during indexing

### Search Performance

- Lucene handles the search complexity
- PostgreSQL provides fast data retrieval
- Consider read replicas for high-traffic scenarios

### Memory Usage

- Lucene index size depends on data volume
- PostgreSQL memory usage for connection pool
- Monitor both Lucene and PostgreSQL memory usage

## Monitoring and Maintenance

### Health Checks

```java
// Check if search engine is healthy
boolean isHealthy = searchEngine.isHealthy();

// Get index statistics
IndexStats stats = searchEngine.getIndexStats();
System.out.println("Total documents: " + stats.getTotalDocuments());
System.out.println("Index health: " + stats.getHealth());
```

### Performance Monitoring

```java
// Get performance statistics
SearchPerformanceStats stats = searchEngine.getPerformanceStats();
System.out.println("Total searches: " + stats.getTotalSearches());
System.out.println("Average search time: " + stats.getAverageSearchTimeMs() + "ms");
System.out.println("Success rate: " + stats.getSuccessRate() + "%");
```

### Index Optimization

```java
// Manual optimization
searchEngine.optimizeIndex();

// Auto-optimization (configured in PostgresSearchConfig)
PostgresSearchConfig config = PostgresSearchConfig.builder()
    .enableAutoOptimization(true)
    .autoOptimizeThreshold(1000) // Optimize after 1000 operations
    .build();
```

### Database Maintenance

```sql
-- Regular PostgreSQL maintenance
VACUUM ANALYZE seekly_product_documents;

-- Check table size
SELECT
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats
WHERE tablename = 'seekly_product_documents';

-- Monitor index usage
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE tablename = 'seekly_product_documents';
```

## Troubleshooting

### Common Issues

1. **Connection Pool Exhaustion**

   - Increase `maxPoolSize` in configuration
   - Check for connection leaks
   - Monitor connection usage

2. **Slow Search Performance**

   - Check Lucene index size
   - Verify PostgreSQL indexes
   - Monitor query execution plans

3. **Data Synchronization Issues**

   - Ensure both Lucene and PostgreSQL are updated
   - Check for transaction rollbacks
   - Verify entity serialization

4. **Memory Issues**
   - Monitor Lucene memory usage
   - Check PostgreSQL memory settings
   - Adjust connection pool size

### Debugging

```java
// Enable debug logging
logging.level.com.h12.seekly=DEBUG
logging.level.org.apache.lucene=DEBUG
logging.level.com.zaxxer.hikari=DEBUG

// Check database connectivity
try (Connection conn = dataSource.getConnection()) {
    System.out.println("Database connected: " + conn.getMetaData().getDatabaseProductName());
}
```

## Migration from LuceneSearchEngine

To migrate from the file-based Lucene search engine to PostgreSQL:

```java
// Old implementation
SearchEngine<Product> oldEngine = new LuceneSearchEngine<>("./index/products", "product");

// New implementation
PostgresSearchConfig config = PostgresSearchConfig.builder()
    .luceneIndexPath("./index/products") // Keep same Lucene index
    .entityType("product")
    .dbUrl("jdbc:postgresql://localhost:5432/seekly_search")
    .dbUsername("username")
    .dbPassword("password")
    .build();

SearchEngine<Product> newEngine = SearchEngineFactory.createPostgresSearchEngine(config);

// Re-index all data
List<Product> allProducts = loadAllProducts();
newEngine.indexBatch(allProducts);
```

## Best Practices

1. **Use batch operations** for bulk indexing
2. **Configure appropriate connection pool size**
3. **Monitor performance metrics** regularly
4. **Implement proper error handling**
5. **Use transactions** for critical operations
6. **Backup both Lucene index and PostgreSQL data**
7. **Test with realistic data volumes**
8. **Implement health checks** in your application

## Comparison with LuceneSearchEngine

| Feature              | LuceneSearchEngine | LucenePostgresSearchEngine |
| -------------------- | ------------------ | -------------------------- |
| **Storage**          | File-based         | PostgreSQL + Lucene        |
| **Persistence**      | File system        | ACID compliant             |
| **Scalability**      | Limited            | High                       |
| **Backup**           | File backup        | Standard DB backup         |
| **Transactions**     | No                 | Full ACID support          |
| **Complex Queries**  | Lucene only        | SQL + Lucene               |
| **Data Integrity**   | Basic              | High                       |
| **Setup Complexity** | Low                | Medium                     |
| **Resource Usage**   | Lower              | Higher                     |
| **Use Case**         | Simple search      | Enterprise search          |

Choose `LucenePostgresSearchEngine` when you need:

- Data persistence and ACID compliance
- Complex data relationships
- High availability and scalability
- Integration with existing PostgreSQL infrastructure
- Advanced analytics and reporting capabilities

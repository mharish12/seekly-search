# Seekly Search Framework

A comprehensive, enterprise-grade search engine framework for Java 21 applications. Built with Apache Lucene, Seekly provides advanced search capabilities with comprehensive metrics tracking, analytics, and performance monitoring.

## Features

### 🔍 Advanced Search Capabilities

- **Full-text search** with relevance scoring
- **Fuzzy matching** for typo tolerance
- **Phrase matching** for exact phrase queries
- **Wildcard matching** for pattern-based searches
- **Field-specific searching** and boosting
- **Faceted search** for filtering and categorization
- **Autocomplete suggestions** for better UX

### 📊 Comprehensive Metrics & Analytics

- **Search performance tracking** (response times, throughput)
- **Query analytics** (popular queries, zero-result queries)
- **Ranking metrics** (top 10, top 5, top 3, first position)
- **Click-through rate tracking**
- **User behavior analytics** (session tracking, time on results)
- **Index health monitoring** (size, optimization status)
- **Real-time performance statistics**

### 🏗️ Framework Architecture

- **Modular design** for easy integration
- **Entity-agnostic** - works with any searchable entity
- **Extensible** - add custom relevance scoring and filters
- **Thread-safe** implementation
- **Memory-efficient** indexing and searching
- **Publishable artifacts** for reuse across services

### 🚀 Performance & Scalability

- **High-performance** Lucene-based indexing
- **Optimized search algorithms** for fast response times
- **Batch operations** for bulk indexing
- **Index optimization** and maintenance
- **Memory and disk usage monitoring**

## Quick Start

### 1. Add Dependency

```gradle
dependencies {
    implementation 'com.h12.seekly:seekly-search:1.0.0'
}
```

### 2. Create Your Entity

```java
@Data
@Builder
public class Product implements SearchableEntity {
    private String id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;

    @Override
    public String getEntityType() {
        return "product";
    }

    @Override
    public String getSearchableContent() {
        return name + " " + description + " " + category;
    }

    @Override
    public Map<String, String> getSearchableFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("description", description);
        fields.put("category", category);
        fields.put("price", price.toString());
        return fields;
    }

    @Override
    public double getRelevanceScore() {
        // Custom relevance scoring logic
        return 1.0 + (price != null ? price.doubleValue() / 1000.0 : 0);
    }
}
```

### 3. Initialize Search Engine

```java
// Create search engine for products
SearchEngine<Product> productSearchEngine =
    new LuceneSearchEngine<>("./index/products", "product");

// Index some products
Product product = Product.builder()
    .id("prod-001")
    .name("iPhone 15 Pro")
    .description("Latest iPhone with advanced camera")
    .category("Electronics")
    .price(new BigDecimal("999.99"))
    .build();

productSearchEngine.index(product);
```

### 4. Perform Searches

```java
// Basic search
SearchResponse<Product> response = productSearchEngine.search("iPhone");

// Advanced search with options
SearchOptions options = SearchOptions.builder()
    .maxResults(20)
    .includeHighlights(true)
    .fuzzyMatching(true)
    .trackMetrics(true)
    .build();

SearchResponse<Product> advancedResponse =
    productSearchEngine.search("smartphone", options);

// Search with filters
Map<String, Object> filters = Map.of("category", "Electronics");
SearchResponse<Product> filteredResponse =
    productSearchEngine.search("phone", filters);
```

### 5. Access Metrics & Analytics

```java
// Get performance statistics
SearchPerformanceStats stats = productSearchEngine.getPerformanceStats();
System.out.println("Total searches: " + stats.getTotalSearches());
System.out.println("Average search time: " + stats.getAverageSearchTimeMs() + "ms");

// Get top performing queries
List<QueryPerformance> topQueries = productSearchEngine.getTopQueries("product", 10);

// Get search metrics for a specific query
Optional<SearchMetrics> metrics = productSearchEngine.getSearchMetrics("iPhone");
```

## Architecture

### Core Components

1. **SearchableEntity Interface** - Base interface for all searchable entities
2. **SearchEngine Interface** - Main search engine contract
3. **LuceneSearchEngine** - Apache Lucene-based implementation
4. **SearchResponse** - Wrapper for search results with metadata
5. **SearchMetrics** - Comprehensive search analytics
6. **MetricsTracker** - In-memory metrics storage and analysis

### Key Classes

- `SearchableEntity` - Interface for searchable entities
- `SearchEngine<T>` - Main search engine interface
- `LuceneSearchEngine<T>` - Lucene-based implementation
- `SearchResponse<T>` - Search results wrapper
- `SearchOptions` - Search configuration options
- `SearchMetrics` - Search analytics data
- `QueryPerformance` - Query-specific performance metrics
- `SearchPerformanceStats` - Overall engine statistics
- `IndexStats` - Index health and statistics

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

    // Boost by popularity (review count)
    if (reviewCount != null && reviewCount > 0) {
        score += Math.min(reviewCount / 100.0, 0.2);
    }

    // Penalize out-of-stock items
    if (stockQuantity != null && stockQuantity == 0) {
        score -= 0.3;
    }

    return Math.max(0.1, Math.min(score, 2.0));
}
```

### Batch Operations

```java
// Index multiple entities efficiently
List<Product> products = loadProductsFromDatabase();
productSearchEngine.indexBatch(products);

// Update entities
productSearchEngine.updateIndex(updatedProduct);

// Remove entities
productSearchEngine.removeFromIndex("prod-001");
```

### Search Options Configuration

```java
SearchOptions options = SearchOptions.builder()
    .maxResults(50)                    // Maximum results to return
    .offset(20)                        // Pagination offset
    .includeHighlights(true)           // Include highlighted snippets
    .maxHighlights(3)                  // Max highlights per result
    .searchFields(Arrays.asList("name", "description")) // Specific fields to search
    .minScore(0.5)                     // Minimum relevance score
    .fuzzyMatching(true)               // Enable fuzzy matching
    .fuzzyDistance(1)                  // Edit distance for fuzzy matching
    .wildcardMatching(true)            // Enable wildcard matching
    .phraseMatching(true)              // Enable phrase matching
    .exactMatchBoost(2.0f)             // Boost for exact matches
    .phraseMatchBoost(1.5f)            // Boost for phrase matches
    .includeFacets(true)               // Include faceted search results
    .facetFields(Arrays.asList("category", "brand")) // Facet fields
    .includeSuggestions(true)          // Include search suggestions
    .maxSuggestions(5)                 // Max suggestions to return
    .trackMetrics(true)                // Track search metrics
    .sessionId("user-session-123")     // User session for analytics
    .userId("user-456")                // User ID for analytics
    .build();
```

## Metrics & Analytics

### Search Performance Metrics

- **Total searches executed**
- **Success/failure rates**
- **Average, median, 95th/99th percentile response times**
- **Results per search statistics**
- **Zero-result query analysis**

### Query Analytics

- **Most popular queries**
- **Query frequency tracking**
- **Click-through rates**
- **Time spent on results**
- **Query performance trends**

### Index Health

- **Index size and document count**
- **Memory and disk usage**
- **Optimization status**
- **Segment count and health**
- **Index version tracking**

## Publishing Artifacts

The framework is designed to be published as Maven artifacts for reuse across services:

```gradle
publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            pom {
                name = 'Seekly Search Framework'
                description = 'A comprehensive search engine framework'
                url = 'https://github.com/your-username/seekly-search'

                licenses {
                    license {
                        name = 'MIT License'
                        url = 'https://opensource.org/licenses/MIT'
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = 'GitHubPackages'
            url = uri('https://maven.pkg.github.com/your-username/seekly-search')
            credentials {
                username = project.findProperty('gpr.user')
                password = project.findProperty('gpr.key')
            }
        }
    }
}
```

## Building & Testing

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the demo application
./gradlew runApp

# Generate test coverage report
./gradlew jacocoTestReport

# Publish to Maven repository
./gradlew publish
```

## Requirements

- **Java 21** or higher
- **Apache Lucene 9.8.0**
- **Gradle 8.14.3** or higher

## Dependencies

- **Apache Lucene** - Core search engine
- **Jackson** - JSON processing
- **Lombok** - Code generation
- **SLF4J + Logback** - Logging
- **Micrometer** - Metrics and monitoring
- **JUnit 5** - Testing
- **Mockito** - Test mocking
- **AssertJ** - Test assertions

## License

MIT License - see LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## Support

For questions, issues, or contributions, please open an issue on GitHub or contact the maintainers.

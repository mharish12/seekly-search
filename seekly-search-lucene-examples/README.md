# Seekly Search Examples

This module contains example implementations and demonstrations of the Seekly Search Framework. It shows how to use the framework with different entity types and search engines.

## 🚀 Features

- **Example Entities**: Product and Seller entities with comprehensive search capabilities
- **Demo Applications**: Complete examples showing both PostgreSQL and Lucene search engines
- **Sample Data**: Realistic sample data for testing and demonstration
- **Spring Boot Integration**: Full Spring Boot application with REST API and metrics

## 📁 Structure

```
seekly-search-examples/
├── src/main/java/com/h12/seekly/examples/
│   ├── entities/
│   │   ├── Product.java          # Example product entity
│   │   └── Seller.java           # Example seller entity
│   └── SeeklySearchExamplesApplication.java  # Main application
├── src/main/resources/
│   └── application.yml           # Configuration
└── README.md                     # This file
```

## 🏃‍♂️ Quick Start

### 1. **Run with PostgreSQL**

```bash
# Set environment variables
export POSTGRES_URL="jdbc:postgresql://localhost:5432/seekly_search"
export POSTGRES_USERNAME="postgres"
export POSTGRES_PASSWORD="postgres"

# Run the examples application
./gradlew :seekly-search-examples:bootRun
```

### 2. **Run with Lucene (No Database Required)**

```bash
# Run without PostgreSQL environment variables
./gradlew :seekly-search-examples:bootRun
```

### 3. **Build and Run JAR**

```bash
# Build the examples module
./gradlew :seekly-search-examples:build

# Run the JAR
java -jar seekly-search-examples/build/libs/seekly-search-examples-1.0.0.jar
```

## 📊 Example Entities

### **Product Entity**

The Product entity demonstrates a comprehensive e-commerce product with:

- **Basic Information**: ID, name, description, category, brand
- **Pricing**: Price, currency
- **Inventory**: Stock quantity, SKU
- **Ratings**: Rating, review count
- **Metadata**: Tags, attributes, images
- **Relationships**: Seller ID
- **Timestamps**: Created/updated dates

```java
Product product = Product.builder()
    .id("prod-001")
    .name("iPhone 15 Pro")
    .description("Latest iPhone with advanced camera system")
    .category("Electronics")
    .brand("Apple")
    .price(new BigDecimal("999.99"))
    .tags(Arrays.asList("smartphone", "camera", "5G"))
    .sellerId("seller-001")
    .rating(4.8)
    .reviewCount(1250)
    .build();
```

### **Seller Entity**

The Seller entity represents a marketplace seller with:

- **Business Information**: Name, description, contact details
- **Location**: Address, city, state, country
- **Categories**: Business categories and specialties
- **Performance**: Rating, review count, total sales
- **Status**: Verification, premium status
- **Media**: Logo, banner, social media links

```java
Seller seller = Seller.builder()
    .id("seller-001")
    .name("TechStore Pro")
    .description("Premium electronics store")
    .email("contact@techstorepro.com")
    .city("San Francisco")
    .state("CA")
    .country("USA")
    .categories(Arrays.asList("Electronics", "Computers"))
    .rating(4.8)
    .isVerified(true)
    .isPremium(true)
    .build();
```

## 🔍 Search Capabilities

### **Product Searches**

The examples demonstrate various product search scenarios:

```java
// Basic search
SearchResponse<Product> response = engine.search("iPhone",
    SearchOptions.builder().maxResults(10).build());

// Search with filters
SearchResponse<Product> response = engine.search("smartphone",
    Map.of("category", "Electronics", "brand", "Apple"),
    SearchOptions.builder().maxResults(5).build());

// Search by brand
SearchResponse<Product> response = engine.search("Apple",
    SearchOptions.builder().maxResults(10).build());

// Search by category
SearchResponse<Product> response = engine.search("laptop",
    SearchOptions.builder().maxResults(5).build());
```

### **Seller Searches**

The examples show seller search capabilities:

```java
// Basic search
SearchResponse<Seller> response = engine.search("TechStore",
    SearchOptions.builder().maxResults(10).build());

// Search with location filters
SearchResponse<Seller> response = engine.search("electronics",
    Map.of("city", "San Francisco", "is_verified", true),
    SearchOptions.builder().maxResults(5).build());

// Search by location
SearchResponse<Seller> response = engine.search("California",
    SearchOptions.builder().maxResults(10).build());

// Search by specialty
SearchResponse<Seller> response = engine.search("sports",
    SearchOptions.builder().maxResults(5).build());
```

## 📈 Sample Data

The examples include realistic sample data:

### **Products**

- iPhone 15 Pro (Electronics, Apple)
- Samsung Galaxy S24 (Electronics, Samsung)
- MacBook Pro 16-inch (Computers, Apple)
- Nike Air Max 270 (Sports, Nike)
- Sony WH-1000XM5 (Electronics, Sony)

### **Sellers**

- TechStore Pro (San Francisco, Electronics)
- Mobile World (New York, Mobile)
- Sports Gear Plus (Los Angeles, Sports)

## 🔧 Configuration

The examples use a separate configuration file (`application.yml`) with:

```yaml
spring:
  application:
    name: seekly-search-examples

  datasource:
    url: ${POSTGRES_URL:jdbc:postgresql://localhost:5432/seekly_search}
    username: ${POSTGRES_USERNAME:postgres}
    password: ${POSTGRES_PASSWORD:postgres}

seekly:
  search:
    postgres:
      enabled: true
      entity-type: product
      lucene-index-path: ./index/products
      enable-metrics: true

      prometheus:
        enabled: true
        metrics-prefix: seekly_search_examples

server:
  port: 8081
```

## 📊 Monitoring

The examples application includes comprehensive monitoring:

### **Health Checks**

```bash
curl "http://localhost:8081/actuator/health"
```

### **Metrics**

```bash
curl "http://localhost:8081/actuator/prometheus"
```

### **Application Info**

```bash
curl "http://localhost:8081/actuator/info"
```

## 🧪 Testing

### **Run Tests**

```bash
./gradlew :seekly-search-examples:test
```

### **Integration Tests**

```bash
./gradlew :seekly-search-examples:integrationTest
```

## 🚀 Deployment

### **Docker**

```dockerfile
FROM openjdk:21-jre-slim

WORKDIR /app
COPY build/libs/seekly-search-examples-1.0.0.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
```

### **Docker Compose**

```yaml
version: '3.8'
services:
  seekly-examples:
    build: .
    ports:
      - '8081:8081'
    environment:
      - POSTGRES_URL=jdbc:postgresql://postgres:5432/seekly_search
      - POSTGRES_USERNAME=postgres
      - POSTGRES_PASSWORD=postgres
    depends_on:
      - postgres
```

## 📚 Usage Examples

### **1. Basic Search Demo**

The application automatically runs a demo when started:

```bash
./gradlew :seekly-search-examples:bootRun
```

This will:

1. Create sample products and sellers
2. Index them in the search engine
3. Perform various search operations
4. Display results and metrics

### **2. Custom Entity Integration**

To use your own entities:

1. **Create your entity class**:

```java
public class MyEntity implements SearchableEntity {
    // Implement required methods
}
```

2. **Update the application**:

```java
SearchEngine<MyEntity> engine = SearchEngineFactory.createPostgresSearchEngine(config);
engine.indexBatch(myEntities);
SearchResponse<MyEntity> response = engine.search("query", options);
```

### **3. Multiple Entity Types**

The examples show how to work with multiple entity types:

```java
// Product search engine
SearchEngine<Product> productEngine = SearchEngineFactory.createPostgresSearchEngine(
    config.toBuilder().entityType("product").build()
);

// Seller search engine
SearchEngine<Seller> sellerEngine = SearchEngineFactory.createPostgresSearchEngine(
    config.toBuilder().entityType("seller").build()
);
```

## 🔍 Search Features Demonstrated

- **Full-text search** across multiple fields
- **Filtering** by category, brand, location, etc.
- **Sorting** by price, rating, date
- **Faceting** for category breakdowns
- **Fuzzy matching** for typos
- **Wildcard matching** for partial terms
- **Phrase matching** for exact phrases
- **Performance tracking** and metrics
- **Health monitoring** and alerts

## 📈 Performance Considerations

The examples demonstrate:

- **Batch indexing** for bulk operations
- **Connection pooling** for database efficiency
- **Index optimization** for search performance
- **Metrics collection** for monitoring
- **Error handling** and recovery

## 🛠️ Troubleshooting

### **Common Issues**

1. **Database Connection**

   ```bash
   # Check if PostgreSQL is running
   curl "http://localhost:8081/actuator/health"
   ```

2. **Index Issues**

   ```bash
   # Check index stats
   curl "http://localhost:8081/api/search/stats/index"
   ```

3. **Memory Usage**
   ```bash
   # Monitor JVM metrics
   curl "http://localhost:8081/actuator/metrics/jvm.memory.used"
   ```

### **Debug Logging**

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.h12.seekly: DEBUG
    org.apache.lucene: DEBUG
```

## 📖 Next Steps

1. **Customize entities** for your use case
2. **Add more search features** like suggestions, autocomplete
3. **Implement advanced filtering** with range queries
4. **Add authentication** and authorization
5. **Scale horizontally** with multiple instances
6. **Set up monitoring** with Grafana dashboards

The examples provide a solid foundation for building production-ready search applications with the Seekly Search Framework.

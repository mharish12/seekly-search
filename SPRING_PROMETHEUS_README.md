# Spring Boot + Prometheus Integration

The Seekly search framework now includes full Spring Boot integration with comprehensive Prometheus metrics monitoring. This provides enterprise-grade features like dependency injection, configuration management, REST APIs, and observability.

## 🚀 Features

### **Spring Boot Integration**

- **Dependency Injection**: Automatic bean management and lifecycle
- **Configuration Management**: Externalized configuration via YAML/properties
- **REST API**: Complete HTTP endpoints for search operations
- **Health Checks**: Built-in health monitoring endpoints
- **Actuator**: Spring Boot Actuator for monitoring and management

### **Prometheus Metrics**

- **Comprehensive Metrics**: Search performance, indexing, and system metrics
- **Custom Counters**: Total searches, successful/failed operations
- **Timers**: Search duration, indexing time, optimization time
- **Gauges**: Document counts, index size, memory usage
- **Histograms**: Result distribution, score distribution
- **Labels/Tags**: Entity type, query, and operation-specific metrics

### **Enterprise Features**

- **Connection Pooling**: HikariCP for efficient database connections
- **Health Monitoring**: Real-time health checks and status reporting
- **Logging**: Structured logging with configurable levels
- **Error Handling**: Comprehensive error handling and reporting
- **Security**: Ready for authentication and authorization

## Quick Start

### 1. **Configuration**

Create `application.yml`:

```yaml
spring:
  application:
    name: seekly-search

  datasource:
    url: jdbc:postgresql://localhost:5432/seekly_search
    username: seekly_user
    password: password

seekly:
  search:
    postgres:
      enabled: true
      entity-type: product
      lucene-index-path: ./index/products
      max-pool-size: 20
      enable-metrics: true

      prometheus:
        enabled: true
        metrics-prefix: seekly_search
        include-jvm-metrics: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 2. **Run the Application**

```bash
# Set environment variables
export POSTGRES_URL="jdbc:postgresql://localhost:5432/seekly_search"
export POSTGRES_USERNAME="seekly_user"
export POSTGRES_PASSWORD="password"

# Run with Spring Boot
./gradlew bootRun

# Or build and run JAR
./gradlew build
java -jar build/libs/seekly-search-1.0.0.jar
```

### 3. **Use the REST API**

```bash
# Search products
curl "http://localhost:8080/api/search/products?query=iPhone&maxResults=10"

# Advanced search
curl -X POST "http://localhost:8080/api/search/products/advanced" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "smartphone",
    "filters": {"category": "Electronics"},
    "maxResults": 20,
    "fuzzyMatching": true
  }'

# Index a product
curl -X POST "http://localhost:8080/api/search/products" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "prod-001",
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone",
    "category": "Electronics",
    "price": 999.99
  }'

# Get performance stats
curl "http://localhost:8080/api/search/stats/performance"

# Health check
curl "http://localhost:8080/api/search/health"
```

## REST API Endpoints

### **Search Operations**

| Method | Endpoint                           | Description                  |
| ------ | ---------------------------------- | ---------------------------- |
| `GET`  | `/api/search/products`             | Basic product search         |
| `POST` | `/api/search/products/advanced`    | Advanced search with filters |
| `GET`  | `/api/search/products/suggestions` | Get search suggestions       |

### **Indexing Operations**

| Method   | Endpoint                     | Description            |
| -------- | ---------------------------- | ---------------------- |
| `POST`   | `/api/search/products`       | Index a single product |
| `POST`   | `/api/search/products/batch` | Batch index products   |
| `PUT`    | `/api/search/products/{id}`  | Update a product       |
| `DELETE` | `/api/search/products/{id}`  | Delete a product       |

### **Monitoring & Statistics**

| Method | Endpoint                                | Description            |
| ------ | --------------------------------------- | ---------------------- |
| `GET`  | `/api/search/stats/performance`         | Performance statistics |
| `GET`  | `/api/search/stats/index`               | Index statistics       |
| `GET`  | `/api/search/stats/top-queries`         | Top performing queries |
| `GET`  | `/api/search/stats/zero-result-queries` | Zero-result queries    |
| `GET`  | `/api/search/health`                    | Health check           |
| `GET`  | `/api/search/metrics`                   | Metrics snapshot       |

### **Maintenance**

| Method | Endpoint               | Description           |
| ------ | ---------------------- | --------------------- |
| `POST` | `/api/search/optimize` | Optimize search index |

## Prometheus Metrics

### **Available Metrics**

The application exposes comprehensive Prometheus metrics:

#### **Counters**

- `seekly_search_searches_total` - Total searches executed
- `seekly_search_searches_successful_total` - Successful searches
- `seekly_search_searches_failed_total` - Failed searches
- `seekly_search_searches_zero_results_total` - Zero-result searches
- `seekly_search_documents_indexed_total` - Documents indexed
- `seekly_search_documents_deleted_total` - Documents deleted
- `seekly_search_documents_updated_total` - Documents updated

#### **Gauges**

- `seekly_search_documents_total` - Total documents in index
- `seekly_search_index_size_bytes` - Index size in bytes
- `seekly_search_memory_usage_bytes` - Memory usage
- `seekly_search_connection_pool_active` - Active connections

#### **Timers**

- `seekly_search_search_duration` - Search execution time
- `seekly_search_index_duration` - Indexing operation time
- `seekly_search_optimization_duration` - Index optimization time

#### **Histograms**

- `seekly_search_search_results` - Distribution of result counts
- `seekly_search_search_scores` - Distribution of relevance scores

#### **Distribution Summaries**

- `seekly_search_search_time_summary` - Search time summary
- `seekly_search_index_size_summary` - Index size summary

### **Accessing Metrics**

```bash
# Prometheus metrics endpoint
curl "http://localhost:8080/actuator/prometheus"

# Spring Boot Actuator metrics
curl "http://localhost:8080/actuator/metrics"

# Custom metrics snapshot
curl "http://localhost:8080/api/search/metrics"
```

### **Prometheus Configuration**

Add to your `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'seekly-search'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

### **Grafana Dashboard**

Create a Grafana dashboard with these key metrics:

```json
{
  "dashboard": {
    "title": "Seekly Search Engine",
    "panels": [
      {
        "title": "Search Rate",
        "targets": [
          {
            "expr": "rate(seekly_search_searches_total[5m])",
            "legendFormat": "Searches/sec"
          }
        ]
      },
      {
        "title": "Search Duration",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(seekly_search_search_duration_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "Total Documents",
        "targets": [
          {
            "expr": "seekly_search_documents_total",
            "legendFormat": "Documents"
          }
        ]
      },
      {
        "title": "Success Rate",
        "targets": [
          {
            "expr": "rate(seekly_search_searches_successful_total[5m]) / rate(seekly_search_searches_total[5m]) * 100",
            "legendFormat": "Success %"
          }
        ]
      }
    ]
  }
}
```

## Configuration Options

### **Spring Boot Configuration**

```yaml
seekly:
  search:
    postgres:
      # Database connection
      db-url: jdbc:postgresql://localhost:5432/seekly_search
      db-username: seekly_user
      db-password: password

      # Entity configuration
      entity-type: product
      lucene-index-path: ./index/products

      # Connection pool
      max-pool-size: 20
      min-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

      # Features
      enable-metrics: true
      enable-query-performance: true
      auto-optimize-threshold: 1000
      enable-auto-optimization: true

      # Prometheus configuration
      prometheus:
        enabled: true
        metrics-prefix: seekly_search
        include-jvm-metrics: true
        include-system-metrics: true
        collection-interval-seconds: 15

      # Health checks
      health-check:
        enabled: true
        timeout-ms: 5000
        interval-ms: 30000
```

### **Environment Variables**

```bash
# Database
export POSTGRES_URL="jdbc:postgresql://localhost:5432/seekly_search"
export POSTGRES_USERNAME="seekly_user"
export POSTGRES_PASSWORD="password"

# Application
export ENVIRONMENT="production"
export SERVER_PORT="8080"

# Logging
export LOGGING_LEVEL_COM_H12_SEEKLY="INFO"
```

## Monitoring & Alerting

### **Health Checks**

```bash
# Application health
curl "http://localhost:8080/actuator/health"

# Search engine health
curl "http://localhost:8080/api/search/health"
```

### **Alerting Rules**

Add to your Prometheus alerting rules:

```yaml
groups:
  - name: seekly-search
    rules:
      - alert: SearchEngineDown
        expr: up{job="seekly-search"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: 'Search engine is down'

      - alert: HighSearchLatency
        expr: histogram_quantile(0.95, rate(seekly_search_search_duration_bucket[5m])) > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: 'High search latency detected'

      - alert: LowSuccessRate
        expr: rate(seekly_search_searches_successful_total[5m]) / rate(seekly_search_searches_total[5m]) < 0.95
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: 'Low search success rate'

      - alert: HighErrorRate
        expr: rate(seekly_search_searches_failed_total[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: 'High search error rate'
```

## Performance Tuning

### **Database Optimization**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### **JVM Tuning**

```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseStringDeduplication \
     -jar seekly-search.jar
```

### **Lucene Optimization**

```yaml
seekly:
  search:
    postgres:
      auto-optimize-threshold: 1000
      enable-auto-optimization: true
```

## Deployment

### **Docker Deployment**

```dockerfile
FROM openjdk:21-jre-slim

WORKDIR /app
COPY build/libs/seekly-search-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  seekly-search:
    build: .
    ports:
      - '8080:8080'
    environment:
      - POSTGRES_URL=jdbc:postgresql://postgres:5432/seekly_search
      - POSTGRES_USERNAME=seekly_user
      - POSTGRES_PASSWORD=password
    depends_on:
      - postgres
    volumes:
      - ./index:/app/index

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: seekly_search
      POSTGRES_USER: seekly_user
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  prometheus:
    image: prom/prometheus
    ports:
      - '9090:9090'
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - '3000:3000'
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin

volumes:
  postgres_data:
```

### **Kubernetes Deployment**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: seekly-search
spec:
  replicas: 3
  selector:
    matchLabels:
      app: seekly-search
  template:
    metadata:
      labels:
        app: seekly-search
    spec:
      containers:
        - name: seekly-search
          image: seekly-search:latest
          ports:
            - containerPort: 8080
          env:
            - name: POSTGRES_URL
              value: 'jdbc:postgresql://postgres:5432/seekly_search'
            - name: POSTGRES_USERNAME
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: username
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: password
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
          readinessProbe:
            httpGet:
              path: /api/search/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
```

## Troubleshooting

### **Common Issues**

1. **Database Connection Issues**

   ```bash
   # Check database connectivity
   curl "http://localhost:8080/actuator/health"

   # Check connection pool
   curl "http://localhost:8080/api/search/metrics" | grep connection
   ```

2. **High Memory Usage**

   ```bash
   # Monitor JVM metrics
   curl "http://localhost:8080/actuator/metrics/jvm.memory.used"

   # Check Lucene index size
   curl "http://localhost:8080/api/search/stats/index"
   ```

3. **Slow Search Performance**

   ```bash
   # Check search duration metrics
   curl "http://localhost:8080/actuator/metrics/seekly_search_search_duration"

   # Optimize index
   curl -X POST "http://localhost:8080/api/search/optimize"
   ```

### **Debug Logging**

```yaml
logging:
  level:
    com.h12.seekly: DEBUG
    org.apache.lucene: DEBUG
    com.zaxxer.hikari: DEBUG
    org.springframework.web: DEBUG
```

## Best Practices

1. **Use connection pooling** with appropriate pool sizes
2. **Monitor key metrics** regularly (search rate, latency, success rate)
3. **Set up alerting** for critical issues
4. **Optimize indexes** periodically
5. **Use batch operations** for bulk indexing
6. **Implement proper error handling** in your applications
7. **Backup both Lucene index and PostgreSQL data**
8. **Use health checks** for load balancers and orchestration
9. **Monitor resource usage** (CPU, memory, disk)
10. **Set up log aggregation** for centralized logging

## Migration from Standalone

To migrate from the standalone search engine to Spring Boot:

1. **Add Spring Boot dependencies** to your project
2. **Create configuration files** (`application.yml`)
3. **Replace direct instantiation** with dependency injection
4. **Use REST endpoints** instead of direct method calls
5. **Configure Prometheus monitoring**
6. **Set up health checks and alerting**

The Spring Boot integration provides a production-ready search service with comprehensive monitoring and management capabilities.

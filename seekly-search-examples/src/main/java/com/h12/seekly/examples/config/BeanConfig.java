package com.h12.seekly.examples.config;

import com.h12.seekly.core.SearchEngine;
import com.h12.seekly.examples.entities.Product;
import com.h12.seekly.examples.entities.Seller;
import com.h12.seekly.factory.SearchEngineFactory;
import com.h12.seekly.metrics.SearchMetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {

    private final Environment environment;


    @Bean
    public SearchEngine<Product> searchEngine() throws IOException {
        return SearchEngineFactory.createPostgresSearchEngine(
                com.h12.seekly.config.PostgresSearchConfig.builder()
                        .dbUrl(environment.getProperty("POSTGRES_URL"))
                        .dbUsername(environment.getProperty("POSTGRES_USERNAME"))
                        .dbPassword(environment.getProperty("POSTGRES_PASSWORD"))
                        .entityType("product")
                        .luceneIndexPath("./index/products")
                        .enableMetrics(true)
                        .enableQueryPerformance(true)
                        .build());
//        return SearchEngineFactory.createLuceneSearchEngine("./index/", "product");
    }

    @Bean
    public SearchEngine<Seller> sellerEngine() throws IOException {
        return SearchEngineFactory.createPostgresSearchEngine(
                com.h12.seekly.config.PostgresSearchConfig.builder()
                        .dbUrl(environment.getProperty("POSTGRES_URL"))
                        .dbUsername(environment.getProperty("POSTGRES_USERNAME"))
                        .dbPassword(environment.getProperty("POSTGRES_PASSWORD"))
                        .entityType("seller")
                        .luceneIndexPath("./index/sellers")
                        .enableMetrics(true)
                        .enableQueryPerformance(true)
                        .build());
//        return SearchEngineFactory.createLuceneSearchEngine(
//                "./index/lucene_sellers", "seller");
    }

    @Bean
    public SearchMetricsCollector searchMetricsCollector(MeterRegistry meterRegistry) throws IOException {
        return new SearchMetricsCollector(meterRegistry, null);
    }
}

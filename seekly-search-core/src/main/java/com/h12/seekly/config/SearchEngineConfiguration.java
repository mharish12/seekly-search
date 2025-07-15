package com.h12.seekly.config;

import com.h12.seekly.core.SearchEngine;
import com.h12.seekly.core.SearchableEntity;
import com.h12.seekly.factory.SearchEngineFactory;
import com.h12.seekly.metrics.SearchMetricsCollector;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Spring Boot configuration for the Seekly search engine.
 * Provides beans for search engines, metrics, and Prometheus integration.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SpringPostgresSearchConfig.class)
public class SearchEngineConfiguration {

    /**
     * Configure Prometheus meter registry
     */
    @Bean
    @ConditionalOnProperty(name = "seekly.search.postgres.prometheus.enabled", havingValue = "true", matchIfMissing = true)
    public MeterRegistry meterRegistry(MeterRegistry meterRegistry, SpringPostgresSearchConfig config) {

        if (config.getPrometheus().isEnabled()) {
            CollectorRegistry collectorRegistry = new CollectorRegistry();

            PrometheusMeterRegistry prometheusMeterRegistry = new PrometheusMeterRegistry(
                    PrometheusConfig.DEFAULT,
                    collectorRegistry,
                    Clock.SYSTEM);
            log.info("Prometheus meter registry configured");
            return prometheusMeterRegistry;
        }

        return meterRegistry;
    }

    /**
     * Configure search metrics collector
     */
    @Bean
    @ConditionalOnProperty(name = "seekly.search.postgres.enable-metrics", havingValue = "true", matchIfMissing = true)
    public SearchMetricsCollector searchMetricsCollector(MeterRegistry meterRegistry,
                                                         SpringPostgresSearchConfig config) {
        String metricsPrefix = config.getPrometheus().getMetricsPrefix();
        return new SearchMetricsCollector(meterRegistry, metricsPrefix);
    }

    /**
     * Configure PostgreSQL search engine
     */
    @Bean
    @ConditionalOnProperty(name = "seekly.search.postgres.enabled", havingValue = "true", matchIfMissing = true)
    public <T extends SearchableEntity> SearchEngine<T> postgresSearchEngine(
            SpringPostgresSearchConfig config) throws IOException {

        log.info("Creating PostgreSQL search engine for entity type: {}", config.getEntityType());

        PostgresSearchConfig postgresConfig = config.toPostgresSearchConfig();
        return SearchEngineFactory.createPostgresSearchEngine(postgresConfig);
    }

    /**
     * Configure Lucene search engine (fallback)
     */
    @Bean
    @ConditionalOnProperty(name = "seekly.search.lucene.enabled", havingValue = "true")
    public <T extends SearchableEntity> SearchEngine<T> luceneSearchEngine(
            SpringPostgresSearchConfig config) throws IOException {

        log.info("Creating Lucene search engine for entity type: {}", config.getEntityType());

        return SearchEngineFactory.createLuceneSearchEngine(
                config.getLuceneIndexPath(),
                config.getEntityType());
    }
}
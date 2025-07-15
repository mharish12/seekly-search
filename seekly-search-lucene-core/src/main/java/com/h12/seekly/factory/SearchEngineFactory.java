package com.h12.seekly.factory;

import com.h12.seekly.config.PostgresSearchConfig;
import com.h12.seekly.core.SearchEngine;
import com.h12.seekly.core.SearchableEntity;
import com.h12.seekly.engine.LucenePostgresSearchEngine;
import com.h12.seekly.engine.LuceneSearchEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Factory class for creating different types of search engines.
 */
@Slf4j
public class SearchEngineFactory {

    /**
     * Create a Lucene-based search engine with file-based storage.
     *
     * @param indexPath  Path to the Lucene index directory
     * @param entityType Type of entity being indexed
     * @param <T>        Type of searchable entity
     * @return Configured search engine
     * @throws IOException if index creation fails
     */
    public static <T extends SearchableEntity> SearchEngine<T> createLuceneSearchEngine(
            String indexPath, String entityType) throws IOException {

        log.info("Creating Lucene search engine for entity type: {} at path: {}", entityType, indexPath);
        return new LuceneSearchEngine<>(indexPath, entityType);
    }

    /**
     * Create a PostgreSQL-based search engine with Lucene for search capabilities.
     *
     * @param config PostgreSQL search configuration
     * @param <T>    Type of searchable entity
     * @return Configured search engine
     * @throws IOException if index creation fails
     */
    public static <T extends SearchableEntity> SearchEngine<T> createPostgresSearchEngine(
            PostgresSearchConfig config) throws IOException {

        log.info("Creating PostgreSQL search engine for entity type: {} with config: {}",
                config.getEntityType(), config);
        return new LucenePostgresSearchEngine<>(
                config.getLuceneIndexPath(),
                config.getEntityType(),
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword());
    }

    /**
     * Create a PostgreSQL-based search engine with default configuration.
     *
     * @param luceneIndexPath Path to the Lucene index directory
     * @param entityType      Type of entity being indexed
     * @param dbUrl           PostgreSQL database URL
     * @param dbUsername      Database username
     * @param dbPassword      Database password
     * @param <T>             Type of searchable entity
     * @return Configured search engine
     * @throws IOException if index creation fails
     */
    public static <T extends SearchableEntity> SearchEngine<T> createPostgresSearchEngine(
            String luceneIndexPath, String entityType, String dbUrl,
            String dbUsername, String dbPassword) throws IOException {

        PostgresSearchConfig config = PostgresSearchConfig.builder()
                .luceneIndexPath(luceneIndexPath)
                .entityType(entityType)
                .dbUrl(dbUrl)
                .dbUsername(dbUsername)
                .dbPassword(dbPassword)
                .build();

        return createPostgresSearchEngine(config);
    }

    /**
     * Create a PostgreSQL-based search engine with custom configuration.
     *
     * @param luceneIndexPath Path to the Lucene index directory
     * @param entityType      Type of entity being indexed
     * @param dbUrl           PostgreSQL database URL
     * @param dbUsername      Database username
     * @param dbPassword      Database password
     * @param maxPoolSize     Maximum connection pool size
     * @param <T>             Type of searchable entity
     * @return Configured search engine
     * @throws IOException if index creation fails
     */
    public static <T extends SearchableEntity> SearchEngine<T> createPostgresSearchEngine(
            String luceneIndexPath, String entityType, String dbUrl,
            String dbUsername, String dbPassword, int maxPoolSize) throws IOException {

        PostgresSearchConfig config = PostgresSearchConfig.builder()
                .luceneIndexPath(luceneIndexPath)
                .entityType(entityType)
                .dbUrl(dbUrl)
                .dbUsername(dbUsername)
                .dbPassword(dbPassword)
                .maxPoolSize(maxPoolSize)
                .build();

        return createPostgresSearchEngine(config);
    }
}
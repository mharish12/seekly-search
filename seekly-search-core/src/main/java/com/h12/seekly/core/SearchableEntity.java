package com.h12.seekly.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base interface for all searchable entities in the Seekly search framework.
 * All entities that need to be searchable should implement this interface.
 */
public interface SearchableEntity {

    /**
     * Unique identifier for the entity
     */
    String getId();

    /**
     * Type of the entity (e.g., "product", "seller", "buyer", "item")
     */
    String getEntityType();

    /**
     * Primary searchable text content
     */
    String getSearchableContent();

    /**
     * Additional searchable fields as key-value pairs
     */
    @JsonIgnore
    Map<String, String> getSearchableFields();

    /**
     * Relevance score for ranking (0.0 to 1.0)
     */
    @JsonIgnore
    default double getRelevanceScore() {
        return 1.0;
    }

    /**
     * Creation timestamp
     */
    @JsonIgnore
    default LocalDateTime getCreatedAt() {
        return LocalDateTime.now();
    }

    /**
     * Last update timestamp
     */
    @JsonIgnore
    default LocalDateTime getUpdatedAt() {
        return LocalDateTime.now();
    }

    /**
     * Whether the entity is active/available for search
     */
    @JsonIgnore
    default boolean isActive() {
        return true;
    }
}
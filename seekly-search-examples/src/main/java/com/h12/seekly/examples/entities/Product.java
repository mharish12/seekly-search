package com.h12.seekly.examples.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.h12.seekly.core.SearchableEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Example Product entity for demonstrating the search framework.
 * This is a sample entity that can be indexed and searched.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product implements SearchableEntity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    @JsonProperty("seller_id")
    private String sellerId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("rating")
    private Double rating;

    @JsonProperty("review_count")
    private Integer reviewCount;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("weight")
    private Double weight;

    @JsonProperty("dimensions")
    private Map<String, Double> dimensions;

    @Override
    public String getEntityType() {
        return "product";
    }

    @Override
    public String getSearchableContent() {
        return "";
    }

    @Override
    public Map<String, String> getSearchableFields() {
        return Map.of(
                "id", id,
                "name", name,
                "description", description,
                "category", category,
                "brand", brand,
                "tags", tags != null ? String.join(" ", tags) : "",
                "seller_id", sellerId,
                "sku", sku);
    }

    public Map<String, Object> getFilterableFields() {
        return Map.of(
                "category", category,
                "brand", brand,
                "seller_id", sellerId,
                "is_active", isActive,
                "price", price,
                "rating", rating,
                "stock_quantity", stockQuantity);
    }

    public Map<String, Object> getSortableFields() {
        return Map.of(
                "price", price,
                "rating", rating,
                "created_at", createdAt,
                "updated_at", updatedAt,
                "review_count", reviewCount);
    }

    public Map<String, Object> getFacetableFields() {
        return Map.of(
                "category", category,
                "brand", brand,
                "seller_id", sellerId,
                "is_active", isActive);
    }

    public String getSearchableText() {
        StringBuilder text = new StringBuilder();
        if (name != null)
            text.append(name).append(" ");
        if (description != null)
            text.append(description).append(" ");
        if (brand != null)
            text.append(brand).append(" ");
        if (category != null)
            text.append(category).append(" ");
        if (tags != null)
            text.append(String.join(" ", tags)).append(" ");
        if (sku != null)
            text.append(sku).append(" ");
        return text.toString().trim();
    }
}
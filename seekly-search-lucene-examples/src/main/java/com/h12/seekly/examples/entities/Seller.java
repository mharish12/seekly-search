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
 * Example Seller entity for demonstrating the search framework.
 * This is a sample entity that can be indexed and searched.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seller implements SearchableEntity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("website")
    private String website;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("country")
    private String country;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("categories")
    private List<String> categories;

    @JsonProperty("specialties")
    private List<String> specialties;

    @JsonProperty("rating")
    private Double rating;

    @JsonProperty("review_count")
    private Integer reviewCount;

    @JsonProperty("total_sales")
    private BigDecimal totalSales;

    @JsonProperty("products_count")
    private Integer productsCount;

    @JsonProperty("is_verified")
    private boolean isVerified;

    @JsonProperty("is_premium")
    private boolean isPremium;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("banner_url")
    private String bannerUrl;

    @JsonProperty("social_media")
    private Map<String, String> socialMedia;

    @JsonProperty("business_hours")
    private Map<String, String> businessHours;

    @JsonProperty("payment_methods")
    private List<String> paymentMethods;

    @JsonProperty("shipping_methods")
    private List<String> shippingMethods;


    @Override
    public String getEntityType() {
        return "seller";
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
                "email", email,
                "city", city,
                "state", state,
                "country", country,
                "categories", categories != null ? String.join(" ", categories) : "",
                "specialties", specialties != null ? String.join(" ", specialties) : "");
    }

    public Map<String, Object> getFilterableFields() {
        return Map.of(
                "city", city,
                "state", state,
                "country", country,
                "is_verified", isVerified,
                "is_premium", isPremium,
                "rating", rating,
                "products_count", productsCount);
    }

    public Map<String, Object> getSortableFields() {
        return Map.of(
                "rating", rating,
                "review_count", reviewCount,
                "total_sales", totalSales,
                "products_count", productsCount,
                "created_at", createdAt,
                "updated_at", updatedAt);
    }

    public Map<String, Object> getFacetableFields() {
        return Map.of(
                "city", city,
                "state", state,
                "country", country,
                "is_verified", isVerified,
                "is_premium", isPremium,
                "categories", categories);
    }

    public String getSearchableText() {
        StringBuilder text = new StringBuilder();
        if (name != null)
            text.append(name).append(" ");
        if (description != null)
            text.append(description).append(" ");
        if (city != null)
            text.append(city).append(" ");
        if (state != null)
            text.append(state).append(" ");
        if (country != null)
            text.append(country).append(" ");
        if (categories != null)
            text.append(String.join(" ", categories)).append(" ");
        if (specialties != null)
            text.append(String.join(" ", specialties)).append(" ");
        return text.toString().trim();
    }
}
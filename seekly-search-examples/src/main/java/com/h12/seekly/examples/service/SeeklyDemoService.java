package com.h12.seekly.examples.service;

import com.h12.seekly.core.SearchEngine;
import com.h12.seekly.core.SearchOptions;
import com.h12.seekly.core.SearchResponse;
import com.h12.seekly.examples.entities.Product;
import com.h12.seekly.examples.entities.Seller;
import com.h12.seekly.factory.SearchEngineFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeeklyDemoService {
    private final SearchEngine<Product> productEngine;
    private final SearchEngine<Seller> sellerEngine;

    public void runLuceneDemo() {
        log.info("Running Lucene Search Engine Demo...");
        try {
            // Create Lucene search engine for products
//            SearchEngine<Product> productEngine = SearchEngineFactory.createLuceneSearchEngine(
//                    "./index/lucene_products", "product");
            List<Product> products = createSampleProducts();
            log.info("Indexing {} products...", products.size());
            productEngine.indexBatch(products);
            performProductSearches(productEngine);

            // Create Lucene search engine for sellers
//            SearchEngine<Seller> sellerEngine = SearchEngineFactory.createLuceneSearchEngine(
//                    "./index/lucene_sellers", "seller");
            List<Seller> sellers = createSampleSellers();
            log.info("Indexing {} sellers...", sellers.size());
            sellerEngine.indexBatch(sellers);
            performSellerSearches(sellerEngine);
            log.info("Lucene demo completed successfully!");
        } catch (Exception e) {
            log.error("Lucene demo failed", e);
        }
    }

    private List<Product> createSampleProducts() {
        return Arrays.asList(
                Product.builder()
                        .id("prod-001")
                        .name("iPhone 15 Pro")
                        .description("Latest iPhone with advanced camera system and A17 Pro chip")
                        .category("Electronics")
                        .brand("Apple")
                        .price(new BigDecimal("999.99"))
                        .currency("USD")
                        .tags(Arrays.asList("smartphone", "camera", "5G", "wireless"))
                        .sellerId("seller-001")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .isActive(true)
                        .stockQuantity(50)
                        .rating(4.8)
                        .reviewCount(1250)
                        .sku("IPH15PRO-256")
                        .build(),
                Product.builder()
                        .id("prod-002")
                        .name("Samsung Galaxy S24")
                        .description("Premium Android smartphone with AI features")
                        .category("Electronics")
                        .brand("Samsung")
                        .price(new BigDecimal("899.99"))
                        .currency("USD")
                        .tags(Arrays.asList("smartphone", "android", "AI", "5G"))
                        .sellerId("seller-002")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .isActive(true)
                        .stockQuantity(75)
                        .rating(4.6)
                        .reviewCount(890)
                        .sku("SAMS24-128")
                        .build(),
                Product.builder()
                        .id("prod-003")
                        .name("MacBook Pro 16-inch")
                        .description("Professional laptop for developers and creatives")
                        .category("Computers")
                        .brand("Apple")
                        .price(new BigDecimal("2499.99"))
                        .currency("USD")
                        .tags(Arrays.asList("laptop", "macbook", "professional", "M3"))
                        .sellerId("seller-001")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .isActive(true)
                        .stockQuantity(25)
                        .rating(4.9)
                        .reviewCount(567)
                        .sku("MBP16-M3")
                        .build(),
                Product.builder()
                        .id("prod-004")
                        .name("Nike Air Max 270")
                        .description("Comfortable running shoes with Air Max technology")
                        .category("Sports")
                        .brand("Nike")
                        .price(new BigDecimal("150.00"))
                        .currency("USD")
                        .tags(Arrays.asList("shoes", "running", "comfortable", "athletic"))
                        .sellerId("seller-003")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .isActive(true)
                        .stockQuantity(200)
                        .rating(4.5)
                        .reviewCount(2340)
                        .sku("NIKE-AM270")
                        .build(),
                Product.builder()
                        .id("prod-005")
                        .name("Sony WH-1000XM5")
                        .description("Premium noise-cancelling wireless headphones")
                        .category("Electronics")
                        .brand("Sony")
                        .price(new BigDecimal("399.99"))
                        .currency("USD")
                        .tags(Arrays.asList("headphones", "wireless", "noise-cancelling", "bluetooth"))
                        .sellerId("seller-002")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .isActive(true)
                        .stockQuantity(100)
                        .rating(4.7)
                        .reviewCount(1890)
                        .sku("SONY-WH1000XM5")
                        .build());
    }

    private List<Seller> createSampleSellers() {
        return Arrays.asList(
                Seller.builder()
                        .id("seller-001")
                        .name("TechStore Pro")
                        .description("Premium electronics and gadgets store")
                        .email("contact@techstorepro.com")
                        .phone("+1-555-0123")
                        .website("https://techstorepro.com")
                        .address("123 Tech Street")
                        .city("San Francisco")
                        .state("CA")
                        .country("USA")
                        .postalCode("94105")
                        .categories(Arrays.asList("Electronics", "Computers", "Gadgets"))
                        .specialties(Arrays.asList("Apple Products", "Gaming", "Smart Home"))
                        .rating(4.8)
                        .reviewCount(1250)
                        .totalSales(new BigDecimal("2500000.00"))
                        .productsCount(1500)
                        .isVerified(true)
                        .isPremium(true)
                        .createdAt(LocalDateTime.now().minusYears(2))
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Seller.builder()
                        .id("seller-002")
                        .name("Mobile World")
                        .description("Your one-stop shop for all mobile devices")
                        .email("info@mobileworld.com")
                        .phone("+1-555-0456")
                        .website("https://mobileworld.com")
                        .address("456 Mobile Avenue")
                        .city("New York")
                        .state("NY")
                        .country("USA")
                        .postalCode("10001")
                        .categories(Arrays.asList("Electronics", "Mobile", "Accessories"))
                        .specialties(Arrays.asList("Smartphones", "Tablets", "Wearables"))
                        .rating(4.6)
                        .reviewCount(890)
                        .totalSales(new BigDecimal("1800000.00"))
                        .productsCount(1200)
                        .isVerified(true)
                        .isPremium(false)
                        .createdAt(LocalDateTime.now().minusYears(1))
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Seller.builder()
                        .id("seller-003")
                        .name("Sports Gear Plus")
                        .description("Premium sports equipment and athletic wear")
                        .email("sales@sportsgearplus.com")
                        .phone("+1-555-0789")
                        .website("https://sportsgearplus.com")
                        .address("789 Sports Boulevard")
                        .city("Los Angeles")
                        .state("CA")
                        .country("USA")
                        .postalCode("90210")
                        .categories(Arrays.asList("Sports", "Fitness", "Outdoor"))
                        .specialties(Arrays.asList("Running", "Basketball", "Soccer"))
                        .rating(4.7)
                        .reviewCount(2340)
                        .totalSales(new BigDecimal("3200000.00"))
                        .productsCount(2000)
                        .isVerified(true)
                        .isPremium(true)
                        .createdAt(LocalDateTime.now().minusYears(3))
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    private void performProductSearches(SearchEngine<Product> engine) {
        log.info("Performing product searches...");
        SearchResponse<Product> response1 = engine.search("iPhone", SearchOptions.builder().maxResults(10).build());
        log.info("Search for 'iPhone': Found {} results", response1.getTotalHits());
        SearchResponse<Product> response2 = engine.search("smartphone",
                Map.of("category", "Electronics", "brand", "Apple"), SearchOptions.builder().maxResults(5).build());
        log.info("Search for 'smartphone' with filters: Found {} results", response2.getTotalHits());
        SearchResponse<Product> response3 = engine.search("Apple", SearchOptions.builder().maxResults(10).build());
        log.info("Search for 'Apple': Found {} results", response3.getTotalHits());
        SearchResponse<Product> response4 = engine.search("laptop", SearchOptions.builder().maxResults(5).build());
        log.info("Search for 'laptop': Found {} results", response4.getTotalHits());
    }

    private void performSellerSearches(SearchEngine<Seller> engine) {
        log.info("Performing seller searches...");
        SearchResponse<Seller> response1 = engine.search("TechStore", SearchOptions.builder().maxResults(10).build());
        log.info("Search for 'TechStore': Found {} results", response1.getTotalHits());
        SearchResponse<Seller> response2 = engine.search("electronics",
                Map.of("city", "San Francisco", "is_verified", true), SearchOptions.builder().maxResults(5).build());
        log.info("Search for 'electronics' with filters: Found {} results", response2.getTotalHits());
        SearchResponse<Seller> response3 = engine.search("California", SearchOptions.builder().maxResults(10).build());
        log.info("Search for 'California': Found {} results", response3.getTotalHits());
        SearchResponse<Seller> response4 = engine.search("sports", SearchOptions.builder().maxResults(5).build());
        log.info("Search for 'sports': Found {} results", response4.getTotalHits());
    }
}
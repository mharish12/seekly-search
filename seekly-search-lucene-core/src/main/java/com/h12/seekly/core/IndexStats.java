package com.h12.seekly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Statistics and health information about the search index.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexStats {

    /**
     * Total number of documents in the index
     */
    private long totalDocuments;

    /**
     * Number of documents by entity type
     */
    private Map<String, Long> documentsByType;

    /**
     * Total size of the index in bytes
     */
    private long indexSizeBytes;

    /**
     * Number of segments in the index
     */
    private int segmentCount;

    /**
     * Number of deleted documents
     */
    private long deletedDocuments;

    /**
     * Index health status
     */
    private IndexHealth health;

    /**
     * Last commit time
     */
    private LocalDateTime lastCommit;

    /**
     * Last optimization time
     */
    private LocalDateTime lastOptimization;

    /**
     * Number of commits since last optimization
     */
    private long commitsSinceOptimization;

    /**
     * Whether the index is optimized
     */
    private boolean optimized;

    /**
     * Index version
     */
    private long version;

    /**
     * Index creation time
     */
    private LocalDateTime creationTime;

    /**
     * Index modification time
     */
    private LocalDateTime modificationTime;

    /**
     * Memory usage for the index in bytes
     */
    private long memoryUsageBytes;

    /**
     * Disk usage for the index in bytes
     */
    private long diskUsageBytes;

    /**
     * Number of terms in the index
     */
    private long termCount;

    /**
     * Number of fields in the index
     */
    private int fieldCount;

    /**
     * Index health enumeration
     */
    public enum IndexHealth {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        CORRUPTED
    }
}
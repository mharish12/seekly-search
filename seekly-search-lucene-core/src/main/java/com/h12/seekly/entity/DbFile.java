package com.h12.seekly.entity;

import com.h12.seekly.enums.StorageProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sleeky_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbFile {
    @Id
    @GeneratedValue(generator = "uuid2")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "is_folder", nullable = false)
    private Boolean isFolder = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_shared", nullable = false)
    private Boolean isShared = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "storage_provider")
    @Enumerated(EnumType.STRING)
    private StorageProvider storageProvider = StorageProvider.LOCAL;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "checksum")
    private long checksum;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_data_key")
    private DbFileData dbFileData;

}

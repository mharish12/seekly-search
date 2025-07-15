package com.h12.seekly.service;

import com.h12.seekly.enums.StorageProvider;
import lombok.Data;

@Data
public class FileMetadataDto {
    private String filename;
    private String contentType;
    private Long fileSize;
    private String ownerId;
    private Boolean isFolder = false;
    private Boolean isDeleted = false;
    private Boolean isShared = false;
    private Boolean isPublic = false;
    private StorageProvider storageProvider = StorageProvider.LOCAL;
}
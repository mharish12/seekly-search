package com.h12.seekly.service;

import com.h12.seekly.entity.DbFile;
import com.h12.seekly.entity.DbFileData;
import com.h12.seekly.enums.StorageProvider;
import com.h12.seekly.repo.DbFileDataRepo;
import com.h12.seekly.repo.DbFileRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final DbFileRepo dbFileRepo;

    private final DbFileDataRepo dbFileDataRepo;

    public DbFile saveFileWithData(MultipartFile file, String ownerId) throws IOException {
        DbFileData dbFileData = new DbFileData();
        dbFileData.setFileName(file.getOriginalFilename());
        dbFileData.setContentType(file.getContentType());
        dbFileData.setData(file.getBytes());
//        DbFileData savedDbFile = fileDataRepo.save(dbFileData);

        DbFile dbfile = new DbFile();
        dbfile.setName(file.getOriginalFilename());
        dbfile.setOwnerId(ownerId);
        dbfile.setMimeType(file.getContentType());
        dbfile.setFileSize(file.getSize());
        dbfile.setDbFileData(dbFileData);
        dbfile.setIsFolder(false);
        dbfile.setIsDeleted(false);
        dbfile.setIsShared(false);
        dbfile.setIsPublic(false);
        dbfile.setStorageProvider(StorageProvider.LOCAL);

        return dbFileRepo.save(dbfile);
    }

    public DbFile saveFileWithData(InputStream inputStream, FileMetadataDto metadata) throws IOException {
        byte[] data = inputStream.readAllBytes();
        DbFileData dbFileData = new DbFileData();
        dbFileData.setFileName(metadata.getFilename());
        dbFileData.setContentType(metadata.getContentType());
        dbFileData.setData(data);
//        DbFileData savedDbFile = fileDataRepo.save(dbFileData);

        DbFile dbfile = buildDbFile(metadata, dbFileData);

        return dbFileRepo.save(dbfile);
    }

    private static DbFile buildDbFile(FileMetadataDto metadata, DbFileData dbFileData) {
        DbFile dbfile = new DbFile();
        dbfile.setName(metadata.getFilename());
        dbfile.setOwnerId(metadata.getOwnerId());
        dbfile.setMimeType(metadata.getContentType());
        dbfile.setFileSize(metadata.getFileSize());
        dbfile.setDbFileData(dbFileData);
        dbfile.setIsFolder(metadata.getIsFolder());
        dbfile.setIsDeleted(metadata.getIsDeleted());
        dbfile.setIsShared(metadata.getIsShared());
        dbfile.setIsPublic(metadata.getIsPublic());
        dbfile.setStorageProvider(metadata.getStorageProvider());
        return dbfile;
    }
}

package com.h12.seekly.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "sleeky_files_data")
@Data
public class DbFileData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "filename", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Basic(fetch = LAZY)
    @Column(name = "data", nullable = false)
    private byte[] data;
}

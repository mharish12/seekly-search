package com.h12.seekly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "sleeky_index")
public class Index {
    @Id
    @GeneratedValue
    private String id;

    @Column(name = "index_name", unique = true, nullable = false)
    private String indexName;


}

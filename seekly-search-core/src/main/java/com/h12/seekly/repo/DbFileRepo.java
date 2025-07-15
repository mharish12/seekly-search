package com.h12.seekly.repo;

import com.h12.seekly.entity.DbFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DbFileRepo extends CrudRepository<DbFile, String> {
    void deleteByName(String name);
    Optional<DbFile> findByName(String name);
}

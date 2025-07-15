package com.h12.seekly.repo;

import com.h12.seekly.entity.DbFileData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbFileDataRepo extends CrudRepository<DbFileData, String> {

    void deleteByFileName(String name);
}

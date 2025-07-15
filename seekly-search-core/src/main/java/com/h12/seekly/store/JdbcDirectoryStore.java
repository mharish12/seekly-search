package com.h12.seekly.store;

import com.h12.seekly.entity.DbFile;
import com.h12.seekly.entity.Index;
import com.h12.seekly.repo.DbFileRepo;
import com.h12.seekly.repo.IndexRepository;
import org.apache.lucene.store.BaseDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JdbcDirectoryStore extends BaseDirectory {

    private final IndexRepository indexRepository;

    private final DbFileRepo dbFileRepo;

    protected JdbcDirectoryStore(IndexRepository indexRepository, DbFileRepo dbFileRepo, LockFactory lockFactory) {
        super(lockFactory);
        this.indexRepository = indexRepository;
        this.dbFileRepo = dbFileRepo;
    }

    @Override
    public String[] listAll() {
        Iterable<Index> indexes = indexRepository.findAll();
        List<String> indexNames = new ArrayList<>();
        for (Index index : indexes) {
            indexNames.add(index.getIndexName());
        }
        return indexNames.toArray(new String[0]);
    }

    @Override
    public void deleteFile(String name) throws IOException {
        dbFileRepo.deleteByName(name);
    }

    @Override
    public long fileLength(String name) throws IOException {
        Optional<DbFile> dbFile = dbFileRepo.findByName(name);
        if (dbFile.isPresent()) {
            return dbFile.get().getFileSize();
        }
        return 0L;
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        return null;
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        return null;
    }

    @Override
    public void sync(Collection<String> names) throws IOException {

    }

    @Override
    public void syncMetaData() throws IOException {

    }

    @Override
    public void rename(String source, String dest) throws IOException {

    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Set<String> getPendingDeletions() throws IOException {
        return Set.of();
    }
}

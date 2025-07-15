package com.h12.seekly.store;

import com.h12.seekly.entity.DbFile;
import com.h12.seekly.entity.DbFileData;
import com.h12.seekly.repo.DbFileDataRepo;
import com.h12.seekly.repo.DbFileRepo;
import org.apache.lucene.store.IndexOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class JdbcIndexOutput extends IndexOutput {
    private final DbFileDataRepo dbFileDataRepo;
    private final DbFileRepo dbFileRepo;
    private final ByteArrayOutputStream buffer;
    private boolean closed = false;

    protected JdbcIndexOutput(DbFileDataRepo dbFileDataRepo, DbFileRepo dbFileRepo, String name) {
        super(name, name);
        this.dbFileDataRepo = dbFileDataRepo;
        this.dbFileRepo = dbFileRepo;
        // Initialize buffer from existing DbFileData if present
        Optional<DbFile> dbFileOpt = dbFileRepo.findByName(name);
        if (dbFileOpt.isPresent() && dbFileOpt.get().getDbFileData() != null
                && dbFileOpt.get().getDbFileData().getData() != null) {
            this.buffer = new ByteArrayOutputStream();
            try {
                this.buffer.write(dbFileOpt.get().getDbFileData().getData());
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize buffer from DB", e);
            }
        } else {
            this.buffer = new ByteArrayOutputStream();
        }
    }

    @Override
    public void close() {
        if (closed)
            return;
        closed = true;
        byte[] fileData = buffer.toByteArray();
        String fileName = getName();

        Optional<DbFile> dbFileOpt = dbFileRepo.findByName(fileName);
        DbFileData dbFileData;
        if (dbFileOpt.isPresent()) {
            DbFile dbFile = dbFileOpt.get();
            dbFileData = dbFile.getDbFileData();
            if (dbFileData == null) {
                dbFileData = new DbFileData();
                dbFileData.setFileName(fileName);
            }
            dbFileData.setContentType("application/octet-stream");
            dbFileData.setData(fileData);
            dbFileData = dbFileDataRepo.save(dbFileData);
            dbFile.setDbFileData(dbFileData);
            dbFileRepo.save(dbFile);
        } else {
            dbFileData = new DbFileData();
            dbFileData.setFileName(fileName);
            dbFileData.setContentType("application/octet-stream");
            dbFileData.setData(fileData);
            dbFileData = dbFileDataRepo.save(dbFileData);
            // Optionally, create a new DbFile and link it to dbFileData
            // DbFile dbFile = new DbFile();
            // dbFile.setName(fileName);
            // dbFile.setDbFileData(dbFileData);
            // dbFileRepo.save(dbFile);
        }
        buffer.reset();
    }

    @Override
    public long getFilePointer() {
        return 0;
    }

    @Override
    public long getChecksum() {
        Optional<DbFile> dbFile = dbFileRepo.findByName(getName());
        return dbFile.map(DbFile::getChecksum).orElse(0L);
    }

    @Override
    public void writeByte(byte b) throws IOException {
        ensureOpen();
        buffer.write(b);
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        ensureOpen();
        buffer.write(b, offset, length);
    }

    private void ensureOpen() throws IOException {
        if (closed)
            throw new IOException("IndexOutput is closed");
    }
}

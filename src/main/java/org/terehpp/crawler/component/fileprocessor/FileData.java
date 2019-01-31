package org.terehpp.crawler.component.fileprocessor;

import org.terehpp.crawler.model.DbEntity;

/**
 * File data, to store state of state machine.
 */
public class FileData {
    private String file;
    private long id;
    private DbEntity entity;
    private String moveDirectory;

    public FileData(String file, long id, DbEntity entity, String moveDir) {
        this.file = file;
        this.id = id;
        this.entity = entity;
        this.moveDirectory = moveDir;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DbEntity getEntity() {
        return entity;
    }

    public void setEntity(DbEntity entity) {
        this.entity = entity;
    }

    public String getMoveDirectory() {
        return moveDirectory;
    }

    public void setMoveDirectory(String moveDirectory) {
        this.moveDirectory = moveDirectory;
    }
}

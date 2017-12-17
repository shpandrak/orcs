package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.blobstore.impl.TempFileSystemBlobStore;

import java.util.Date;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class RemoteShpanBlobInstance {
    private final String name;
    private final String originCopyId;
    private final Date creationTime;
    private final boolean readonly;
    private final TempFileSystemBlobStore store;
    private Long size = null;

    public RemoteShpanBlobInstance(
            String name,
            String originCopyId,
            Date creationTime,
            boolean readonly,
            TempFileSystemBlobStore store) {
        this.name = name;
        this.originCopyId = originCopyId;
        this.creationTime = creationTime;
        this.readonly = readonly;
        this.store = store;
    }

    public String getName() {
        return name;
    }

    public String getOriginCopyId() {
        return originCopyId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public TempFileSystemBlobStore getStore() {
        return store;
    }
}

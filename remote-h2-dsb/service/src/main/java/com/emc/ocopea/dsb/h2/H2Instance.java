package com.emc.ocopea.dsb.h2;

import java.util.Date;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class H2Instance {
    private final String name;
    private final String originCopyId;
    private final Date creationTime;
    private final boolean readonly;
    private Long size = null;

    public H2Instance(String name, String originCopyId, Date creationTime, boolean readonly) {
        this.name = name;
        this.originCopyId = originCopyId;
        this.creationTime = creationTime;
        this.readonly = readonly;
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
}

package com.emc.ocopea.docker;

/**
 * Created by liebea on 11/30/16.
 * Drink responsibly
 */
public class ShpanBlobKeyDTOLocalCopy {
    private final String namespace;
    private final String key;

    private ShpanBlobKeyDTOLocalCopy() {
        this(null, null);
    }

    public ShpanBlobKeyDTOLocalCopy(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "ShpanBlobKeyDTO{" +
                "namespace='" + namespace + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}

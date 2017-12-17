package com.emc.ocopea.demo.dsb.shpanblob;

/**
 * Created by liebea on 11/30/16.
 * Drink responsibly
 */
public class ShpanBlobKeyDTO {
    private final String namespace;
    private final String key;

    private ShpanBlobKeyDTO() {
        this(null, null);
    }

    public ShpanBlobKeyDTO(String namespace, String key) {
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

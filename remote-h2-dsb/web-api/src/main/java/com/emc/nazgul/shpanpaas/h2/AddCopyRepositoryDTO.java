package com.emc.nazgul.shpanpaas.h2;

/**
 * Created by liebea on 1/5/16.
 * Drink responsibly
 */
public class AddCopyRepositoryDTO {
    private final String url;

    private AddCopyRepositoryDTO() {
        this(null);
    }

    public AddCopyRepositoryDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

package com.emc.ocopea.dsb.h2;

/**
 * Created by liebea on 3/7/16.
 * Drink responsibly
 */
public class H2EndpointInfo {
    private final String user;
    private final String password;
    private final String url;

    private H2EndpointInfo() {
        this(null, null, null);
    }

    public H2EndpointInfo(String user, String password, String url) {
        this.user = user;
        this.password = password;
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }
}

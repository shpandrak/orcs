package com.emc.ocopea.demo.dsb.shpanblob;

/**
 * Created by liebea on 6/26/16.
 * Drink responsibly
 */
public class RemoteShpanblobEndPointInfo {
    private final String url;
    private final String serviceId;

    private RemoteShpanblobEndPointInfo() {
        this(null, null);
    }

    public RemoteShpanblobEndPointInfo(String url, String serviceId) {
        this.url = url;
        this.serviceId = serviceId;
    }

    public String getUrl() {
        return url;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String toString() {
        return "RemoteShpanblobEndPointInfo{" +
                "url='" + url + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}

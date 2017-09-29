package com.emc.ocopea.hub.repository;

public interface HubConfigRepository {

    void storeKey(String key, String value);

    String readKey(String key);
}

package com.emc.microservice.registry;

public interface RegistryClient {

    void registerService(String urn, String url);
}

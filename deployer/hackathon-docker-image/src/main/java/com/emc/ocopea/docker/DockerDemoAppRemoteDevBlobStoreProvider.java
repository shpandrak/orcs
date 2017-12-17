package com.emc.ocopea.docker;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreProvider;
import com.emc.microservice.blobstore.MicroServiceBlobStore;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.serialization.SerializationManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link BlobStoreProvider}.
 */
public class DockerDemoAppRemoteDevBlobStoreProvider
        implements BlobStoreProvider<DockerDemoAppRemoteDevBlobStoreConfiguration> {
    private final Map<String, MicroServiceBlobStore> blobStores = new ConcurrentHashMap<>();

    @Override
    public synchronized BlobStoreAPI getBlobStore(
            DockerDemoAppRemoteDevBlobStoreConfiguration configuration,
            SerializationManager serializationManager) {

        return blobStores.computeIfAbsent(
                configuration.getServiceId(),
                (s) ->
                        new MicroServiceBlobStore(
                                new DockerDemoAppRemoteBlobStore(
                                        ResourceProviderManager
                                                .getResourceProvider()
                                                .getWebAPIResolver()
                                                .getWebAPI(
                                                        configuration.getBlobStoreURL(),
                                                        ShpanBlobRemoteWebAPILocalCopy.class),
                                        s),
                                serializationManager)
        );
    }

    @Override
    public Class<DockerDemoAppRemoteDevBlobStoreConfiguration> getConfClass() {
        return DockerDemoAppRemoteDevBlobStoreConfiguration.class;
    }

}

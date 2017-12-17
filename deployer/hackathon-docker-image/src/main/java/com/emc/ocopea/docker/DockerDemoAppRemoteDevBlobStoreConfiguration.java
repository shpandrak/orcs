package com.emc.ocopea.docker;

import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DockerDemoAppRemoteDevBlobStoreConfiguration extends BlobStoreConfiguration {
    private static final ResourceConfigurationProperty PROPERTY_BLOBSTORE_SERVICE_ID =
            new ResourceConfigurationProperty(
                    "name",
                    ResourceConfigurationPropertyType.STRING,
                    "Blobstore name",
                    true,
                    false);
    private static final ResourceConfigurationProperty PROPERTY_BLOBSTORE_URL = new ResourceConfigurationProperty(
            "blobStoreURN",
            ResourceConfigurationPropertyType.STRING,
            "Blobstore Service URN",
            true,
            false);

    public DockerDemoAppRemoteDevBlobStoreConfiguration() {
        super("Remote Blobstore", Arrays.asList(PROPERTY_BLOBSTORE_URL));
    }

    public DockerDemoAppRemoteDevBlobStoreConfiguration(String serviceId, String url) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_BLOBSTORE_SERVICE_ID.getName(), serviceId,
                PROPERTY_BLOBSTORE_URL.getName(), url
        }));
    }

    public String getBlobStoreURL() {
        return getProperty(PROPERTY_BLOBSTORE_URL.getName());
    }

    public String getServiceId() {
        return getProperty(PROPERTY_BLOBSTORE_SERVICE_ID.getName());
    }

}

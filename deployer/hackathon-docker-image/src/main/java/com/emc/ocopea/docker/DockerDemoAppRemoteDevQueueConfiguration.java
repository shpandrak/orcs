package com.emc.ocopea.docker;

import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class DockerDemoAppRemoteDevQueueConfiguration extends QueueConfiguration {
    private static final ResourceConfigurationProperty PROPERTY_DESTINATION_TYPE = new ResourceConfigurationProperty(
            "destinationType",
            ResourceConfigurationPropertyType.ENUM,
            "destination object type",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_BLOBSTORE_NAME = new ResourceConfigurationProperty(
            "blobstoreName",
            ResourceConfigurationPropertyType.STRING,
            "Blobstore to use for large messages",
            false,
            false);
    private static final ResourceConfigurationProperty PROPERTY_BLOB_NAMESPACE = new ResourceConfigurationProperty(
            "blobstoreNameSpace",
            ResourceConfigurationPropertyType.STRING,
            "Blobstore namespace to use",
            false,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES = Arrays.asList(
            PROPERTY_DESTINATION_TYPE, PROPERTY_BLOBSTORE_NAME, PROPERTY_BLOB_NAMESPACE);

    public DockerDemoAppRemoteDevQueueConfiguration() {
        super("Dev Destination Messaging", PROPERTIES);
    }

    public DockerDemoAppRemoteDevQueueConfiguration(MessageDestinationType messageDestinationType) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DESTINATION_TYPE.getName(), messageDestinationType.name()}));

    }

    @Override
    public String getBlobstoreName() {
        return getProperty(PROPERTY_BLOBSTORE_NAME.getName());
    }

    @Override
    public MessageDestinationType getMessageDestinationType() {
        return MessageDestinationType.valueOf(getProperty(PROPERTY_DESTINATION_TYPE.getName()));
    }

    @Override
    public boolean isGzip() {
        return false;
    }

}

package com.emc.ocopea.docker.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.DestinationConfiguration;
import com.emc.microservice.messaging.InputQueueConfiguration;
import com.emc.microservice.messaging.ManagedMessageListener;
import com.emc.microservice.messaging.MessagingProvider;
import com.emc.microservice.messaging.QueueReceiverImpl;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.ocopea.docker.DockerDemoAppRemoteDevQueueConfiguration;

import java.util.Map;

/**
 * Unsupported operations only.
 */
public class DockerDemoAppMessagingProvider
        implements MessagingProvider<DockerDemoAppRemoteDevQueueConfiguration, CFDemoAppMessagingProviderConfiguration> {
    @Override
    public RuntimeMessageSender getMessageSender(
            CFDemoAppMessagingProviderConfiguration messageConfiguration,
            DestinationConfiguration destinationConfiguration,
            DockerDemoAppRemoteDevQueueConfiguration queueConfiguration,
            Context context) {
        throw new UnsupportedOperationException("No!");
    }

    @Override
    public void createQueue(
            CFDemoAppMessagingProviderConfiguration messagingConfiguration,
            DockerDemoAppRemoteDevQueueConfiguration devQueueConfiguration) {
        throw new UnsupportedOperationException("No!");
    }

    @Override
    public QueueReceiverImpl createQueueReceiver(
            CFDemoAppMessagingProviderConfiguration configuration,
            InputQueueConfiguration configuration2,
            DockerDemoAppRemoteDevQueueConfiguration t1,
            Map<String, DockerDemoAppRemoteDevQueueConfiguration> map,
            ManagedMessageListener listener,
            Context context,
            String s) {
        throw new UnsupportedOperationException("No!");
    }

    @Override
    public Class<DockerDemoAppRemoteDevQueueConfiguration> getQueueConfClass() {
        return DockerDemoAppRemoteDevQueueConfiguration.class;
    }

    @Override
    public Class<CFDemoAppMessagingProviderConfiguration> getMessageConfClass() {
        return CFDemoAppMessagingProviderConfiguration.class;
    }
}
